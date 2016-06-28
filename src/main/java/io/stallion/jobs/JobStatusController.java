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
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.DbPersister;
import io.stallion.dataAccess.file.JsonFilePersister;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the storing and retrieval of JobStatus records in either the database
 * or file-system.
 */
public class JobStatusController extends StandardModelController<JobStatus> {

    public static JobStatusController instance() {
        return (JobStatusController)Context.dal().getNamespaced("st-jobs", "job-status");
    }

    public static void selfRegister()  {
        DataAccessRegistration registration = new DataAccessRegistration()
                .setControllerClass(JobStatusController.class)
                .setModelClass(JobStatus.class)
                .setPath("job-status")
                .setNameSpace("st-jobs")
                .setPersisterClass(JsonFilePersister.class)
                .setShouldWatch(false)
                .setUseDataFolder(true)
                .setWritable(true);
        if (DB.instance() != null) {
            registration
                    .setPersisterClass(DbPersister.class)
                    .setPath("")
                    .setTableName("stallion_job_status");
        }
        Context.dal().register(registration);

    }


    private static Set<String> _uniqueFields = new HashSet<String>(Arrays.asList(new String[]{"name"}));

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
