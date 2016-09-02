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

import io.stallion.Context;
import io.stallion.dataAccess.DataAccessRegistration;
import io.stallion.dataAccess.NoStash;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.DbPersister;
import io.stallion.dataAccess.file.JsonFilePersister;
import io.stallion.dataAccess.filtering.FilterOperator;
import io.stallion.services.Log;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.list;
import static io.stallion.utils.Literals.utcNow;

/**
 * Handles the storing and retrieval of JobStatus records in either the database
 * or file-system.
 */
public class JobStatusController extends StandardModelController<JobStatus> {
    public DateTimeFormatter TIME_STAMP_FORMAT = DateTimeFormatter.ofPattern("YYYYMMddHHmm");

    public static JobStatusController instance() {
        return (JobStatusController)Context.dal().get("st_job_status");
    }

    public static void selfRegister()  {
        DataAccessRegistration registration = new DataAccessRegistration()
                .setControllerClass(JobStatusController.class)
                .setModelClass(JobStatus.class)
                .setPath("job-status")
                .setBucket("st_job_status")
                .setPersisterClass(JsonFilePersister.class)
                .setShouldWatch(false)
                .setUseDataFolder(true)
                .setWritable(true);
        if (DB.instance() != null) {
            registration
                    .setBucket("st_job_status")
                    .setPersisterClass(DbPersister.class)
                    .setPath("")
                    .setTableName("stallion_job_status");
        }
        Context.dal().register(registration);

    }

    public List<JobStatus> findJobsForPeriod() {
        return findJobsForPeriod(DateUtils.utcNow());
    }
    public List<JobStatus> findJobsForPeriod(ZonedDateTime now) {
        List<JobStatus> returnJobs = list();
        now = now.withSecond(0).withNano(0);
        String nowStamp = TIME_STAMP_FORMAT.format(now);
        List<JobStatus> allJobs;
        if (!getPersister().isDbBacked()) {
            allJobs = filterBy("nextExecuteMinuteStamp", nowStamp, FilterOperator.LESS_THAN_OR_EQUAL).all();

        } else {
            allJobs = DB.instance().query(JobStatus.class, "SELECT * FROM stallion_job_status WHERE nextexecuteminutestamp<=? AND lockedAt=0 AND lockGuid=''", nowStamp);
        }
        String firstJobName = "";
        if (allJobs.size() > 0) {
            firstJobName = ", first job name is " + allJobs.get(0).getName();
        }
        Log.info("JobCoordinator: Found {0} jobs for period {1} {2}", allJobs.size(), nowStamp, firstJobName);
        for(JobStatus job: allJobs) {
            Log.fine("Job found for period {0} {1} {2}", job.getName(), nowStamp, job.getNextExecuteMinuteStamp());
            JobDefinition definition = JobCoordinator.instance().getJobDefinition(job.getName());
            if (definition == null) {
                Log.warn("No job found for jobStatus with name {0}", job.getName());
                continue;
            }
            if (empty(job.getNextExecuteMinuteStamp()) || empty(job.getNextExecuteAt())) {
                Log.warn("Job nextExecute is empty {0}", job.getName());
                resetNextRunTime(job, now.plusMinutes(1));
                continue;
            }
            Long milsAgo = now.toInstant().toEpochMilli() - job.getNextExecuteAt().toInstant().toEpochMilli();
            if (!empty(job.getLockedAt())) {
                Log.info("Job is locked: {0} {1}", job.getName(), job.getLockedAt());
                if ((now.toInstant().getEpochSecond() - job.getLockedAt()) > (2 * 60 * 60 * 1000)) {
                    Log.warn("Job locked for more than 120 minutes! Resetting the lock.");
                    resetLockAndNextRunAt(job, now.plusMinutes(1));
                }
                continue;
            }
            // If the job is more than five minutes stale, we recalculate it and save it
            if (!nowStamp.equals(job.getNextExecuteMinuteStamp()) && milsAgo > (5 * 60 * 1000)) {
                Log.warn("Job stamp is too far behind {0} {1}", job.getName(), job.getNextExecuteMinuteStamp());
                resetNextRunTime(job, now.plusMinutes(1));
            } else {
                returnJobs.add(job);
            }
        }
        return returnJobs;
    }

    public void resetLockAndNextRunAt(JobStatus jobStatus) {
        resetLockAndNextRunAt(jobStatus, DateUtils.utcNow());
    }

    public void resetLockAndNextRunAt(JobStatus jobStatus, ZonedDateTime now) {
        JobDefinition definition = JobCoordinator.instance().getJobDefinition(jobStatus.getName());
        jobStatus.setLockedAt(0L);
        jobStatus.setLockGuid("");
        ZonedDateTime next = definition.getSchedule().nextAt(now);
        jobStatus.setNextExecuteAt(next);
        jobStatus.setNextExecuteMinuteStamp(TIME_STAMP_FORMAT.format(next));
        Log.fine("Job {0} set to run next at {1}", jobStatus.getName(), jobStatus.getNextExecuteMinuteStamp());
        save(jobStatus);
    }

    public void resetNextRunTime(JobStatus jobStatus) {
        resetNextRunTime(jobStatus, DateUtils.utcNow());
    }

    public void resetNextRunTime(JobStatus jobStatus, ZonedDateTime now) {
        JobDefinition definition = JobCoordinator.instance().getJobDefinition(jobStatus.getName());
        ZonedDateTime next = definition.getSchedule().nextAt(now);
        jobStatus.setNextExecuteAt(next);
        jobStatus.setNextExecuteMinuteStamp(TIME_STAMP_FORMAT.format(next));
        Log.fine("Job {0} set to run next at {1}", jobStatus.getName(), jobStatus.getNextExecuteMinuteStamp());
        save(jobStatus);
    }

    public void initializeJobStatus(JobDefinition definition, ZonedDateTime now) {
        JobStatus job = forUniqueKey("name", definition.getName());
        if (job != null && job.getDeleted()) {
            job.setDeleted(false);
            job.setLockedAt(0L);
            job.setLockGuid("");
            job.setName(definition.getName());
            job.setNextExecuteMinuteStamp("");
            job.setNextExecuteAt(null);
            save(job);
        } else if (job == null) {
            job = new JobStatus();
            job.setName(definition.getName());
            job.setId(Context.dal().getTickets().nextId());
            save(job);
        }
        if (empty(job.getNextExecuteMinuteStamp())) {
            resetNextRunTime(job, now);
        }
    }

    private static Set<String> _uniqueFields = new HashSet<String>(Arrays.asList(new String[]{"name"}));

    public boolean lockJob(String name) {
        String lockId = UUID.randomUUID().toString();
        Long lockedAt = DateUtils.mils();
        if (!getPersister().isDbBacked()) {
            JobStatus job = forUniqueKey("name", name);
            if (empty(job.getLockGuid()) && empty(job.getLockedAt())) {
                job.setLockedAt(lockedAt);
                job.setLockGuid(lockId);
                return true;
            } else {
                return false;
            }
        } else {
            DB.instance().execute(
                    "UPDATE stallion_job_status SET lockedAt=?, lockGuid=? WHERE lockedAt = 0 AND lockGuid='' AND name=?",
                    lockedAt, lockId, name
            );
            Long count = DB.instance().queryScalar("SELECT COUNT(*) FROM stallion_job_status WHERE lockGuid=? AND name=?",
                    lockId, name
            );
            if (count == 0) {
                return false;
            }
            // Gotta do this so that the in memory version also has the lock
            JobStatus job = forUniqueKey("name", name);
            job.setLockGuid(lockId);
            job.setLockedAt(lockedAt);
            save(job);
            return true;
        }
    }

    public void unlockJob(String name) {
        JobStatus job = forUniqueKey("name", name);
        job.setLockGuid("");
        job.setLockedAt(0L);
        save(job);
    }

    public JobStatus getOrCreateForName(String jobName) {
        JobStatus job = forUniqueKey("name", jobName);
        if (job == null) {
            job = new JobStatus();
            job.setName(jobName);
            job.setId(Context.dal().getTickets().nextId());
            save(job);
        }
        return job;
    }

    @Override
    public Set<String> getUniqueFields() {
        return _uniqueFields;
    }
}
