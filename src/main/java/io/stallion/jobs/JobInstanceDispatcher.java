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

import io.stallion.monitoring.HealthTracker;
import io.stallion.services.Log;
import io.stallion.utils.DateUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.ZonedDateTime;

/**
 * Handles the actually running of a Job on a different thread, with reporting
 * of errors and bookeeping.
 *  *
 */
class JobInstanceDispatcher implements Runnable {
    private Job job;
    private JobDefinition definition;
    private boolean forced;
    private JobStatus status;
    private ZonedDateTime now;

    public JobInstanceDispatcher(JobStatus status, JobDefinition definition, Job job, boolean forced, ZonedDateTime now) {
        this.status = status;
        this.definition = definition;
        this.job = job;
        this.forced = forced;
        this.now = now;
    }

    @Override
    public void run() {
        boolean locked = JobStatusController.instance().lockJob(definition.getName());
        if (!locked) {
            Log.warn("Job is locked, skipping run command. {0}", definition.getName());
            return;
        }

        status.setStartedAt(DateUtils.mils());
        JobStatusController.instance().save(status);

        try {
            // Run the job
            job.execute();
            status.setCompletedAt(DateUtils.mils());
            status.setFailedAt(0);
            status.setFailCount(0);
            status.setError("");
            ZonedDateTime nextRunAt = definition.getSchedule().nextAt(DateUtils.utcNow().plusMinutes(3));
            Long nextCompleteBy = nextRunAt.plusMinutes(definition.getAlertThresholdMinutes()).toInstant().toEpochMilli();
            Log.info("Threshold minutes: {0} next complete by: {1}", definition.getAlertThresholdMinutes(), nextCompleteBy);
            status.setShouldSucceedBy(nextCompleteBy);
            JobStatusController.instance().save(status);
        } catch (Exception e) {
            Log.exception(e, "Error running job " + definition.getName());
            status.setFailCount(status.getFailCount() + 1);
            status.setError(e.toString() + ": " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
            status.setFailedAt(DateUtils.mils());
            if (HealthTracker.instance() != null) {
                HealthTracker.instance().logException(e);
            }
            JobStatusController.instance().save(status);
        } finally {
            JobStatusController.instance().resetLockAndNextRunAt(status, now.plusMinutes(1));
        }
    }
}
