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

package io.stallion.monitoring;

import com.sun.management.UnixOperatingSystemMXBean;
import io.stallion.asyncTasks.TaskHealthInfo;
import io.stallion.jobs.JobHealthInfo;
import io.stallion.settings.Settings;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;

import static io.stallion.utils.Literals.*;

public class HealthInfo {
    private HttpHealthInfo http = null;
    private List<JobHealthInfo> jobs = list();
    private TaskHealthInfo tasks = null;
    private List<EndpointHealthInfo> endpoints = null;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = list();
    private SystemHealth system = null;
    private int httpStatusCode = 200;

    private static final long MIN_DISK_SPACE = 1024 * 1024 * 1024; // 1 GB Min disk space
    private static final long MAX_MEM_USAGE = 500 * 1024 * 1024; // 500MB max memory
    private static final long MAX_FILE_HANDLES = 4000;

    public HealthInfo hydrateSystemHealth() {
        Runtime rt = Runtime.getRuntime();
        long memUsage = (rt.totalMemory() - rt.freeMemory());
        long usedMB = memUsage / 1024 / 1024;
        system.setJvmMemoryUsage(memUsage);
        system.setJvmMemoryUsageMb(usedMB);
        system.setDiskFreeAppDirectory(new File(Settings.instance().getTargetFolder()).getUsableSpace());
        system.setDiskFreeDataDirectory(new File(Settings.instance().getDataDirectory()).getUsableSpace());
        system.setDiskFreeDataDirectoryMb(new File(Settings.instance().getDataDirectory()).getUsableSpace() / 1024 / 1024);
        system.setDiskFreeLogDirectory(new File("/tmp").getUsableSpace());
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if(os instanceof UnixOperatingSystemMXBean){
            UnixOperatingSystemMXBean unixBean = (UnixOperatingSystemMXBean) os;
            //unixBean.
            // get system load
            // get process CPU load

            system.setFileHandlesOpen(unixBean.getOpenFileDescriptorCount());
        }
        return this;
    }

    public HealthInfo hydrateErrors() {
        if (http != null) {
            if (http.getError500s() > 0 && ((http.getError500s() / http.getRequestCount()) > .002)){
                warnings.add("Had " + http.getError500s() + " 500 errors");
            }
            if (http.getError500s() > 0 && ((http.getError500s() / http.getRequestCount()) > .05)){
                errors.add("Had " + http.getError500s() + " 500 errors");
            }

            if (http.getError400s() > 10 && http.getRequestCount() > 100 &&
                    ((http.getError400s() / http.getRequestCount()) > .1)) {
                warnings.add("Too many 400 errors: " + http.getError400s() + " errors out of " + http.getRequestCount() + " requests");
            }
        }
        if (jobs != null) {
            for (JobHealthInfo info : getJobs()) {
                if (info.getExpectCompleteBy() < mils()) {
                    warnings.add("Job " + info.getJobName() + " has not completed in the expected time. ");
                }
                if (!empty(info.getError())) {
                    warnings.add("Job " + info.getJobName() + " finished last run with errors.");
                }
                // Job has been overdue for half-a-day, give an error
                if (info.getExpectCompleteBy() < (mils() - (42000 * 1000))) {
                    errors.add("Job " + info.getJobName() + " has been overdue for more than a day and a half. ");
                }
                if (info.getFailCount() > 2) {
                    warnings.add("Job " + info.getJobName() + " has failed more than three times in a row.");
                }

            }
        }
        if (tasks != null) {
            if (tasks.getStuckTasks() > 0) {
                warnings.add("Stuck async tasks were found.");
            }
        }
        if (endpoints != null) {
            for (EndpointHealthInfo info : endpoints) {
                if (info.getStatusCode() >= 400) {
                    errors.add("Endpoint " + info.getUrl() + " had a bad status: " + info.getStatusCode());
                }
                if (!info.isFoundString()) {
                    errors.add("Check string not found for endpoint " + info.getUrl());
                }
            }
        }
        if (system != null) {
            if (system.getDiskFreeAppDirectory() < MIN_DISK_SPACE) {
                errors.add("App directory is below the minimum disk space. ");
            }
            if (system.getDiskFreeDataDirectory() < MIN_DISK_SPACE) {
                errors.add("Data directory is below the minimum disk space. ");
            }

            if (system.getDiskFreeLogDirectory() < MIN_DISK_SPACE) {
                errors.add("Log directory is below the minimum disk space. ");
            }
            if (system.getJvmMemoryUsage() > MAX_MEM_USAGE) {
                warnings.add("JVM is using more than allowed memory.");
            }
            long maxFileHandles = MAX_FILE_HANDLES;
            if (system.getFileHandlesMax() > 0) {
                maxFileHandles = system.getFileHandlesMax();
            }
            if ((new Double(system.getFileHandlesOpen()) / new Double(maxFileHandles)) > .8 || system.getFileHandlesAvailable() < 750) {
                errors.add("Running out of available file handles open on your system!");
            }

            if (system.getMemoryPercentFree() < .2) {
                warnings.add("Using more than 80% of total memory, physical and swap.");
            }
            if (system.getSwapPagingRate() > 25) {
                warnings.add("Swapping rate is over 25 pages.");
            }

            if (system.isSslExpiresWithin21Days()) {
                warnings.add("SSL certificate expires within 21 days. Make sure to update it!");
            }
            if (system.isSslExpiresWithin7Days()) {
                errors.add("SSL certificate expires within 7 days. Make sure to update it!");
            }
        }
        return this;
    }


    public HttpHealthInfo getHttp() {
        return http;
    }

    public void setHttp(HttpHealthInfo http) {
        this.http = http;
    }



    public TaskHealthInfo getTasks() {
        return tasks;
    }

    public void setTasks(TaskHealthInfo tasks) {
        this.tasks = tasks;
    }

    public List<JobHealthInfo> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobHealthInfo> jobs) {
        this.jobs = jobs;
    }

    public List<EndpointHealthInfo> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<EndpointHealthInfo> endpoints) {
        this.endpoints = endpoints;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public SystemHealth getSystem() {
        return system;
    }

    public void setSystem(SystemHealth system) {
        this.system = system;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}
