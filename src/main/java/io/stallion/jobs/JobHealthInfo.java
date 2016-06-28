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

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class JobHealthInfo {
    private String jobName;

    private long lastStartedAt;
    private long lastFinishedAt;
    private long lastFailedAt;
    private long lastRunTime;
    private boolean isRunningNow;
    private String error;
    private boolean lastRunSucceeded;
    private long expectCompleteBy;
    private int failCount = 0;


    public long getLastStartedAt() {
        return lastStartedAt;
    }

    public void setLastStartedAt(long lastStartedAt) {
        this.lastStartedAt = lastStartedAt;
    }

    public long getLastFinishedAt() {
        return lastFinishedAt;
    }

    public void setLastFinishedAt(long lastFinishedAt) {
        this.lastFinishedAt = lastFinishedAt;
    }

    public long getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(long lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public boolean isRunningNow() {
        return isRunningNow;
    }

    public void setRunningNow(boolean isRunningNow) {
        this.isRunningNow = isRunningNow;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isLastRunSucceeded() {
        return lastRunSucceeded;
    }

    public void setLastRunSucceeded(boolean lastRunSucceeded) {
        this.lastRunSucceeded = lastRunSucceeded;
    }

    public long getExpectCompleteBy() {
        return expectCompleteBy;
    }

    public void setExpectCompleteBy(long expectCompleteBy) {
        this.expectCompleteBy = expectCompleteBy;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public long getLastFailedAt() {
        return lastFailedAt;
    }

    public JobHealthInfo setLastFailedAt(long lastFailedAt) {
        this.lastFailedAt = lastFailedAt;
        return this;
    }

    public int getFailCount() {
        return failCount;
    }

    public JobHealthInfo setFailCount(int failCount) {
        this.failCount = failCount;
        return this;
    }
}
