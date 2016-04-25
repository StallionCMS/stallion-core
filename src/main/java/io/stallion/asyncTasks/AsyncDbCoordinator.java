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

package io.stallion.asyncTasks;

import io.stallion.dal.db.DB;
import io.stallion.services.Log;


public class AsyncDbCoordinator extends AsyncFileCoordinator {

    @Override
    public void updateTask(AsyncTask task, boolean executeAtChanged) {
        AsyncTaskController.instance().save(task);
    }

    @Override
    public void saveNewTask(AsyncTask task) {
        Log.info("Adding task to the database: id={0} handler={1} customKey={2}", task.getId(), task.getHandlerName(), task.getCustomKey());
        AsyncTaskController.instance().save(task);
    }

    @Override
    public AsyncTask findAndLockNextTask(Long now) {
        return ((AsyncTaskDbPersister)getTaskPersister()).findAndLockNextTask(now);
    }


    @Override
    public boolean markCompleted(AsyncTask task) {
        return getTaskPersister().markComplete(task);
    }

    @Override
    public boolean markFailed(AsyncTask task, Throwable throwable) {

        return getTaskPersister().markFailed(task, throwable);
    }

    @Override
    public boolean hasTaskWithId(Long taskId) {
        AsyncTask task = DB.instance().fetchOne(AsyncTask.class, taskId);
        if (task == null) {
            return false;
        }
        if (task.getCompletedAt() > 0) {
            return false;
        }
        return true;
    }
}
