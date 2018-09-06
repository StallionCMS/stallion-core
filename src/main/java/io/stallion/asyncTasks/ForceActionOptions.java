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

package io.stallion.asyncTasks;

import io.stallion.boot.CommandOptionsBase;
import org.kohsuke.args4j.Option;


public class ForceActionOptions extends CommandOptionsBase {

    @Option(name="-jobName", usage="The job to run")
    private String jobName = "";

    @Option(name="-taskId", usage="The task to run")
    private Long taskId = 0L;

    @Option(name="-force", usage="Force to run, even if it is locked")
    private Boolean force = false;




    public String getJobName() {
        return jobName;
    }

    public ForceActionOptions setJobName(String jobName) {
        this.jobName = jobName;
        return this;
    }

    public Long getTaskId() {
        return taskId;
    }

    public ForceActionOptions setTaskId(Long taskId) {
        this.taskId = taskId;
        return this;
    }

    public Boolean getForce() {
        return force;
    }

    public ForceActionOptions setForce(Boolean force) {
        this.force = force;
        return this;
    }
}
