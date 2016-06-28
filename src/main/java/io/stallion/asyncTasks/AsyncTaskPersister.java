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


public interface AsyncTaskPersister {

    /**
     * Mark the last task execution as failed in the datastore.
     * Remove locks and increment the try count. Mark for retry with exponential back-off.
     * If the count is too high, do not mark for retry.
     * @param task
     * @param e
     * @return
     */
    public boolean markFailed(AsyncTask task, Throwable e);

    /**
     * Mark the task as completed.
     * @param task
     * @return
     */
    public boolean markComplete(AsyncTask task);

    /**
     * Lock the task in the datastore. Should update the database atomically to prevent
     * the same task being locked by different stallion instances.
     * @param task
     * @return
     */
    public boolean lockForProcessing(AsyncTask task);

    /**
     * Delete all tasks from the database that were completed more than 40 days ago
     */
    public void deleteOldTasks();

}
