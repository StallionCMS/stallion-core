/*
 * Stallion: A Modern Content Management System
 *
 * Copyright (C) 2015 - 2016 Patrick Fitzsimmons.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
 * License for more details. You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 *
 *
 *
 */

package io.stallion.jobs;

import io.stallion.exceptions.CommandException;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;

import static io.stallion.utils.Literals.*;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * A service that handles running recurring jobs in the background.
 */
public class JobCoordinator extends Thread {

    private static JobCoordinator _instance;

    public static JobCoordinator instance() {
        if (_instance == null) {
            _instance = new JobCoordinator();
            //throw new ConfigException("You tried to access the JobCoordinator.instance(), but startUp() method was never called, jobs are not running.");
        }
        return _instance;
    }

    public static void startUp() {
        instance().start();
    }


    /**
     * Creates an instance, but does not start it.
     * Used by unittests.
     */
    public static void initForTesting() {
        if (_instance != null) {
            throw new ConfigException("You cannot startup two job coordinators!");
        }
        _instance = new JobCoordinator();
        _instance.synchronousMode = true;
    }

    public static void shutdown() {
        if (_instance == null) {
            return;
        }
        _instance.shouldShutDown = true;
        if (_instance.isAlive()) {
            _instance.interrupt();
            try {
                _instance.join();
            } catch(InterruptedException e) {

            }
        }
        _instance = null;
    }

    /* Instance methods     */
    private JobCoordinator() {
        queue = new PriorityBlockingQueue<>();
        pool = Executors
                .newFixedThreadPool(25);
        registeredJobs = new HashSet<>();
    };

    private Executor pool;
    private boolean shouldShutDown = false;
    private PriorityBlockingQueue<JobDefinition> queue;
    private Set<String> registeredJobs;
    private Map<String, Long> lastRanAtByJobName = new HashMap<>();
    private Boolean synchronousMode = false;

    @Override
    public void run() {

        if (Settings.instance().getLocalMode() && "prod".equals(Settings.instance().getEnv()) || "qa".equals(Settings.instance().getEnv())) {
            Log.info("Running localMode, environment is QA or PROD, thus not running jobs. Do not want to run production jobs locally!");
            return;
        }

        while (!shouldShutDown) {
            try {
                executeJobsForCurrentTime(utcNow());
            } catch(Exception e) {
                Log.exception(e, "Error executing jobs in the main job coordinator loop!!!");
            }


            // Find the seconds until the next minute, sleep until 10 seconds into the next minute
            ZonedDateTime now = utcNow();
            ZonedDateTime nextMinute = now.withSecond(0).plusMinutes(1);
            Long waitSeconds = (nextMinute.toInstant().getEpochSecond() - now.toInstant().getEpochSecond()) + 10;
            try {
                Thread.sleep(waitSeconds*1000);
            } catch (InterruptedException e) {
                break;
            }

        }

        // Shut down the thread pool
        // Wait for everything to exit


    }

    /**
     * This really should not be a public method, but needs to be so unittests can access it.
     * This is called by the main run() loop to actually find jobs to run and trigger their running
     * @param now
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void executeJobsForCurrentTime(ZonedDateTime now) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        now = now.withSecond(0).withNano(0);
        Log.finest("Checking for jobs to execute this period");
        for (Object brake: safeLoop(queue.size()+10)) { // fake while-loop pattern
            if (queue.size() == 0) {
                Log.finest("No jobs in queue, returning");
                return;
            }
            JobDefinition definition = queue.poll();
            if (definition == null) {
                Log.finer("No job definition found");
                return;
            }
            // If a job is stale by more than five minutes, something is wonky, put it back into the queue and keep processing
            if (definition.getNextRunAt() == null || definition.getNextRunAt().plusMinutes(5).isBefore(now)) {
                Log.info("Job definition is null or a previous time: {0} {1}", definition.getName(), definition.getNextRunAt());
                definition.setNextRunAt(definition.getSchedule().nextAt(now));
                if (definition.getNextRunAt().isBefore(now)) {
                    Log.warn("Something is very wrong. Next run at is being calculated to be before the current time! {0} {1}", definition.getName(), definition.getNextRunAt());
                    putIntoQueue(definition, now);
                    continue;
                }
            }
            // Not ready to run until the future, keep going
            if (definition.getNextRunAt().isAfter(now)) {
                Log.finer("Job definition scheduled for later: {0} {1}", definition.getName(), definition.getNextRunAt());
                putIntoQueue(definition, now);
                return;
            }

            Long lastRanAt = lastRanAtByJobName.getOrDefault(definition.getName(), 0L);
            // If the job last ran within 90 seconds, then we skip the run, something must have gone wrong
            // This should never happen, but you can never have too many double-checks
            // We don't run a job twice in a row accidentally
            if ((now.toInstant().toEpochMilli() - lastRanAt) < 90000) {
                Log.warn("Job {0} was ran twice within 90 seconds!", definition.getName());
                putIntoQueue(definition, now);
                return;
            }
            lastRanAtByJobName.put(definition.getName(), now.toInstant().toEpochMilli());
            Log.info("Dispatching job {0}", definition.getName());
            // Now start-up a thread to actually run the job
            Class<? extends Job> jobClass = definition.getJobClass();
            Job job = jobClass.newInstance();
            JobInstanceDispatcher dispatcher = new JobInstanceDispatcher(definition, job);
            if (synchronousMode) {
                dispatcher.run();
            } else {
                pool.execute(dispatcher);
            }
            // Return the job to the queue so it will run again the future
            putIntoQueue(definition, now);
        }
    }

    public void forceRunJob(String jobName, boolean forceEvenIfLocked) {
        JobDefinition jobDefinition = null;
        for(JobDefinition def: queue) {
            if (jobName.equals(def.getName())) {
                jobDefinition = def;
                break;
            }
        }
        if (jobDefinition == null) {
            throw new CommandException("Job not found: " + jobName);
        }
        Class<? extends Job> jobClass = jobDefinition.getJobClass();
        Job job = null;
        try {
            job = jobClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        JobInstanceDispatcher dispatcher = new JobInstanceDispatcher(jobDefinition, job);

        dispatcher.run();
    }

    /**
     * Register the given job definition to be run as a recurring class
     *
     * @param job
     */
    public void registerJob(JobDefinition job) {
        doRegisterJob(job, utcNow());
    }

    /**
     * Called by unittests to load the job, with dateTime passed in
     * to override the current time
     * @param job
     * @param now
     */
    public void registerJobForTest(JobDefinition job, ZonedDateTime now) {
        doRegisterJob(job, now);
    }

    /**
     * Called by unittests when we need to re-sort for a new time
     */
    public JobCoordinator resetForDateTime(ZonedDateTime now) {
        List<JobDefinition> definitions = list();
        for(Object brake: safeLoop(1000)) {
            if (queue.size() == 0) {
                break;
            }
            definitions.add(queue.poll());
        }
        for(JobDefinition definition: definitions) {
            putIntoQueue(definition, now);
        }
        return this;
    }


    private void doRegisterJob(JobDefinition job, ZonedDateTime now) {
        if (shouldShutDown) {
            Log.warn("Tried to add a job while the JobCoordinator was shutting down.");
            return;
        }
        // Verify the job class exists and extends IJob
        //Class cls;
        //try {
        //    cls = getClass().getClassLoader().loadClass(job.getJobClassName());
        //} catch (ClassNotFoundException e) {
        //    throw new RuntimeException("Could not find the job class: " + job.getJobClassName(), e);
        //}
        //if (!IJob.class.isAssignableFrom(cls)) {
        //    throw new UsageException("Job class " + job.getJobClassName() + " does not implement interface IJob");
        //}
        if (job.getJobClass() == null) {
            throw new UsageException("Missing an IJob class");
        }
        if (empty(job.getName())) {
            job.setName(job.getJobClassName());
        }
        if (registeredJobs.contains(job.getName())) {
            throw new ConfigException("You tried to load the same job twice! If you want to load multiple jobs with the same class, be sure to set the 'name' field of the job definition. Job name: " + job.getName());
        }
        registeredJobs.add(job.getName());
        putIntoQueue(job, now);
    }

    private void putIntoQueue(JobDefinition definition) {
        putIntoQueue(definition, utcNow());
    }

    private void putIntoQueue(JobDefinition definition, ZonedDateTime now) {
        if (shouldShutDown) {
            Log.warn("Tried to enqueue a job while the JobCoordinator was shutting down.");
            return;
        }
        ZonedDateTime nextMinute = null;
        if (now.getSecond() > 30) {
            nextMinute = now.withSecond(0).plusMinutes(2);
        } else {
            nextMinute = now.withSecond(0).plusMinutes(1);
        }
        definition.setNextRunAt(definition.getSchedule().nextAt(nextMinute));
        queue.add(definition);
    }

    /**
     * Get a list of the health of all jobs.
     * @return
     */
    public List<JobHealthInfo> buildJobHealthInfos() {
        List<JobHealthInfo> infos = list();
        for(JobDefinition job: queue) {
            JobHealthInfo health = new JobHealthInfo();
            infos.add(health);
            JobStatus status = JobStatusController.instance().getOrCreateForName(job.getName());
            // TODO match the names on all these fields;
            health.setRunningNow(status.getStartedAt() > status.getCompletedAt() && status.getStartedAt() > status.getFailedAt());
            health.setError(status.getError());
            health.setJobName(job.getName());
            health.setLastFailedAt(status.getFailedAt());
            health.setLastFinishedAt(status.getCompletedAt());
            health.setLastRunSucceeded(status.getFailedAt()==0);
            health.setExpectCompleteBy(status.getShouldSucceedBy());
            health.setLastFinishedAt(status.getCompletedAt());
            health.setLastRunTime(status.getLastDurationSeconds());
            health.setFailCount(status.getFailCount());
        }
        return infos;
    }

}
