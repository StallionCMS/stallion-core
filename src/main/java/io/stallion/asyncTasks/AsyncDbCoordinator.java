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

import io.stallion.dataAccess.db.DB;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.GeneralUtils;

import static io.stallion.utils.Literals.or;


public class AsyncDbCoordinator extends AsyncFileCoordinator {

    @Override
    public void updateTask(AsyncTask task, boolean executeAtChanged) {
        AsyncTaskController.instance().save(task);
    }

    @Override
    public void saveNewTask(AsyncTask task) {
        Log.info("Adding task to the database: id={0} handler={1} customKey={2}", task.getId(), task.getHandlerName(), task.getCustomKey());

        // If we are running in localMode, we only want to run tasks created in localMode, we don't want to run tasks created
        // on production on our local machine, or tasks created on our local machine on production
        String localMode = "";
        if (Settings.instance().getLocalMode()) {
            task.setLocalMode(or(System.getenv("USER"), GeneralUtils.slugify(Settings.instance().getTargetFolder())));
        }
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
