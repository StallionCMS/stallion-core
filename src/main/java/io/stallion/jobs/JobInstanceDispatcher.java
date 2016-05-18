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

import io.stallion.services.Log;
import io.stallion.utils.DateUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Handles the actually running of a Job on a different thread, with reporting
 * of errors and bookeeping.
 *  *
 */
class JobInstanceDispatcher implements Runnable {
    private Job job;
    private JobDefinition definition;

    public JobInstanceDispatcher(JobDefinition definition, Job job) {
        this.definition = definition;
        this.job = job;
    }

    @Override
    public void run() {
        JobStatus status = JobStatusController.instance().getOrCreateForName(definition.getName());
        status.setStartedAt(DateUtils.mils());
        JobStatusController.instance().save(status);

        try {
            // Run the job
            job.execute();
            status.setCompletedAt(DateUtils.mils());
            Long nextCompleteBy = DateUtils.mils() + (definition.getAlertThresholdMinutes() * 60 * 1000);
            Log.info("Threshold minutes: {0} next complete by: {1}", definition.getAlertThresholdMinutes(), nextCompleteBy);
            status.setShouldSucceedBy(nextCompleteBy);
        } catch (Exception e) {
            status.setError(e.toString() + ": " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
            status.setFailedAt(DateUtils.mils());
        }
        JobStatusController.instance().save(status);
    }
}
