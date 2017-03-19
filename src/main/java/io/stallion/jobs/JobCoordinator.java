/*
 * Stallion Core: A Modern Web Framework
 *
 * Copyright (C) 2015 - 2016 Stallion Software LLC.
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
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import static io.stallion.utils.Literals.*;

import java.time.ZonedDateTime;
import java.util.*;
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
            _instance.setName("stallion-job-coordinator");
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
        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern("stallion-job-execution-thread-%d")
                .build();
        // Create an executor service for single-threaded execution
        pool = Executors.newFixedThreadPool(25, factory);
        registeredJobs = new HashSet<>();
    };

    private Executor pool;
    private boolean shouldShutDown = false;
    private PriorityBlockingQueue<JobDefinition> queue;
    private Set<String> registeredJobs;
    private Map<String, Long> lastRanAtByJobName = new HashMap<>();
    private Map<String, JobDefinition> jobByName = map();
    private Boolean synchronousMode = false;

    @Override
    public void run() {

        if (Settings.instance().getLocalMode() && ("prod".equals(Settings.instance().getEnv()) || "qa".equals(Settings.instance().getEnv()))) {
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
        for (JobStatus jobStatus: JobStatusController.instance().findJobsForPeriod(now)) {
            JobDefinition definition = JobCoordinator.instance().getJobDefinition(jobStatus.getName());
            lastRanAtByJobName.put(definition.getName(), now.toInstant().toEpochMilli());
            Log.info("Dispatching job {0}", definition.getName());
            // Now start-up a thread to actually run the job
            Class<? extends Job> jobClass = definition.getJobClass();
            Job job = jobClass.newInstance();
            JobInstanceDispatcher dispatcher = new JobInstanceDispatcher(jobStatus, definition, job, false, now);
            if (synchronousMode) {
                dispatcher.run();
            } else {
                pool.execute(dispatcher);
            }

        }
    }

    public void forceRunJob(String jobName, boolean forceEvenIfLocked) {
        JobDefinition jobDefinition = jobByName.getOrDefault(jobName, null);
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
        JobStatus status = JobStatusController.instance().forUniqueKey("name", jobName);
        JobInstanceDispatcher dispatcher = new JobInstanceDispatcher(status, jobDefinition, job, forceEvenIfLocked, utcNow());

        dispatcher.run();
    }

    public void registerJob(JobComplete jobComplete) {
        JobDefinition def = new JobDefinition()
                .setAlertThresholdMinutes(jobComplete.getAlertThresholdMinutes())
                .setJobClass(jobComplete.getClass())
                .setName(jobComplete.getName())
                .setSchedule(jobComplete.getSchedule());
        doRegisterJob(def, utcNow());
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
        jobByName.put(job.getName(), job);

        JobStatusController.instance().initializeJobStatus(job, now);
    }


    public JobDefinition getJobDefinition(String name) {
        return jobByName.getOrDefault(name, null);
    }

    /**
     * Get a list of the health of all jobs.
     * @return
     */
    public List<JobHealthInfo> buildJobHealthInfos() {
        List<JobHealthInfo> infos = list();
        for(JobDefinition job: jobByName.values()) {
            if (job == null || empty(job.getName())) {
                continue;
            }
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
            health.setNextExecuteMinuteStamp(status.getNextExecuteMinuteStamp());
        }
        return infos;
    }

}
