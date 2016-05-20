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

import io.stallion.plugins.PluginRegistry;

import java.time.ZonedDateTime;

public class JobDefinition implements Comparable<JobDefinition> {
    private String name;
    private Schedule schedule;
    private int alertThresholdMinutes = 75;
    private String className;
    private Class<? extends Job> jobClass;

    private ZonedDateTime nextRunAt = null;

    public String getName() {
        return name;
    }

    public JobDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public JobDefinition setSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    /**
     * The number of minutes after which the jobs was supposed to run at which
     * we return errors in the healthcheck endpoint because the job has not finished
     * running. Default to 75 minutes.
     *
     * @return
     */
    public int getAlertThresholdMinutes() {
        return alertThresholdMinutes;
    }

    public JobDefinition setAlertThresholdMinutes(int alertThresholdMinutes) {
        this.alertThresholdMinutes = alertThresholdMinutes;
        return this;
    }

    public String getJobClassName() {
        return className;
    }

    public JobDefinition setJobClassName(String className) {
        this.className = className;
        return this;
    }

    public JobDefinition setJobClass(Class<? extends Job> cls) {
        setJobClassName(cls.getCanonicalName());
        this.jobClass = cls;
        return this;
    }



    public Class<? extends Job> getJobClass() {
        if (jobClass != null) {
            return jobClass;
        }
        try {
            return (Class<Job>)PluginRegistry.instance().getClassLoader().loadClass(getJobClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public int compareTo(JobDefinition o) {
        if (getNextRunAt() == null) {
            return 1;
        } else if (o.getNextRunAt() == null) {
            return -1;
        }
        return getNextRunAt().compareTo(o.getNextRunAt());
    }

    /**
     * It is set when we add a JobDefinition to the JobCoordinator, or when the job definition is
     * added back to the queue after it has been run.
     */
    public ZonedDateTime getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(ZonedDateTime nextRunAt) {
        nextRunAt = nextRunAt.withSecond(0).withNano(0);
        this.nextRunAt = nextRunAt;
    }
}
