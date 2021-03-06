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

import io.stallion.dataAccess.AlternativeKey;
import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;

import javax.persistence.Column;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Table(name="stallion_job_status")
public class JobStatus extends ModelBase {
    private String name = "";
    private long startedAt = 0;
    private long completedAt = 0;
    private long failedAt = 0;
    private String error = "";
    private long shouldSucceedBy = 0;
    private long lastDurationSeconds = 0;
    private int failCount = 0;
    private long lockedAt = 0;
    private String lockGuid = "";
    private String nextExecuteMinuteStamp = "";
    private ZonedDateTime nextExecuteAt;


    @UniqueKey
    @Column
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    @Column
    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }


    @Column
    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }


    @Column
    public long getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(long failedAt) {
        this.failedAt = failedAt;
    }


    @Column
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }



    @Column
    public long getShouldSucceedBy() {
        return shouldSucceedBy;
    }

    public void setShouldSucceedBy(long shouldSucceedBy) {
        this.shouldSucceedBy = shouldSucceedBy;
    }



    @Column
    public long getLastDurationSeconds() {
        return lastDurationSeconds;
    }

    public void setLastDurationSeconds(long lastDurationSeconds) {
        this.lastDurationSeconds = lastDurationSeconds;
    }

    @Column
    public int getFailCount() {
        return failCount;
    }

    public JobStatus setFailCount(int failCount) {
        this.failCount = failCount;
        return this;
    }

    @Column(nullable = false)
    public long getLockedAt() {
        return lockedAt;
    }

    public JobStatus setLockedAt(long lockedAt) {
        this.lockedAt = lockedAt;
        return this;
    }

    @Column(nullable = false, length=50)
    public String getLockGuid() {
        return lockGuid;
    }

    public JobStatus setLockGuid(String lockGuid) {
        this.lockGuid = lockGuid;
        return this;
    }

    @AlternativeKey
    @Column(nullable = false, length=12)
    public String getNextExecuteMinuteStamp() {
        return nextExecuteMinuteStamp;
    }

    public JobStatus setNextExecuteMinuteStamp(String nextExecuteMinuteStamp) {
        this.nextExecuteMinuteStamp = nextExecuteMinuteStamp;
        return this;
    }

    @Column(nullable = false)
    public ZonedDateTime getNextExecuteAt() {
        return nextExecuteAt;
    }

    public JobStatus setNextExecuteAt(ZonedDateTime nextExecuteAt) {
        this.nextExecuteAt = nextExecuteAt;
        return this;
    }
}
