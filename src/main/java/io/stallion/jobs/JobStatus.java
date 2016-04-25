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

import io.stallion.dal.base.ModelBase;
import io.stallion.dal.base.UniqueKey;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="stallion_job_status")
public class JobStatus extends ModelBase {
    private String name = "";
    private long startedAt = 0;
    private long completedAt = 0;
    private long failedAt = 0;
    private String error = "";
    private long shouldSucceedBy = 0;
    private long lastDurationSeconds = 0;

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
}
