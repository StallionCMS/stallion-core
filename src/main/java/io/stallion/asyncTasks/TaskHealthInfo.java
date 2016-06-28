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

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class TaskHealthInfo {
    private int stuckTasks = 0;
    private int completedTasks = 0;
    private int pendingTasks = 0;

    /**
     * The count of tasks that should have been executed already, based on their
     * orginallyScheduledFor parameter, but have either been failing or were not run at all.
     *
     * @return
     */
    public int getStuckTasks() {
        return stuckTasks;
    }

    public void setStuckTasks(int stuckTasks) {
        this.stuckTasks = stuckTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
    }
}
