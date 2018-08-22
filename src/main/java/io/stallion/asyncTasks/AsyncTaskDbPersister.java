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
import io.stallion.dataAccess.db.DbPersister;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.UUID;

import static io.stallion.utils.Literals.mils;
import static io.stallion.utils.Literals.or;


public class AsyncTaskDbPersister extends DbPersister<AsyncTask> implements AsyncTaskPersister {

    public AsyncTask findAndLockNextTask(Long now) {
        return findAndLockNextTask(now, 0);
    }


    public AsyncTask findAndLockNextTask(Long now, int depth) {
        // Stop recursion at ten queries
        if (depth > 10) {
            return null;
        }
        String localMode = "";
        if (Settings.instance().getLocalMode()) {
            localMode = or(System.getenv("USER"), GeneralUtils.slugify(Settings.instance().getTargetFolder()));
        }
        // Do not execute tasks that are more than 2 days stale
        Long minTime = now - 86400 * 2 * 1000;
        AsyncTask task = DB.instance().queryForOne(
                AsyncTask.class,
                "SELECT * FROM stallion_async_tasks WHERE lockUuid='' AND executeAt<=? AND executeAt>? AND " +
                        " completedAt=0 AND localMode=? ORDER BY executeAt ASC",
                now, minTime, localMode);

        if (task == null) {
            return null;
        }
        String lockUuid = UUID.randomUUID().toString();
        Long lockedAt = mils();
        int affected = DB.instance().execute("UPDATE stallion_async_tasks SET lockedAt=?, lockUuid=? WHERE lockUuid='' AND id=?", lockedAt, lockUuid, task.getId());
        if (affected == 0) {
            // This will happen if another thread or process locks the row in between the SELECT and the UPDATE.
            // If this happens, we just run the whole method again to get another row.
            // We could have done a SELECT FOR UPDATE, but we want to minimize locking. So better to do optimisic
            // locking rather than overly lock and create problems for MySQL
            return findAndLockNextTask(now, depth + 1);
        } else {
            task.setLockedAt(lockedAt);
            task.setLockUuid(lockUuid);
            return task;
        }
    }

    @Override
    public boolean markFailed(AsyncTask task, Throwable e) {
        Log.info("Mark task failed: id={0} handler={1} customKey={2}", task.getId(), task.getHandlerName(), task.getCustomKey());
        task.setTryCount(task.getTryCount() + 1);
        task.setErrorMessage(e.toString() + ExceptionUtils.getStackTrace(e));
        if (task.getTryCount() >= 5) {
            task.setFailedAt(DateUtils.mils());
            Log.info("Mark task failed permanently: id={0} handler={1} customKey={2}", task.getId(), task.getHandlerName(), task.getCustomKey());
        } else {
            task.setExecuteAt(DateUtils.mils() + ((2^task.getTryCount())*1000));
            task.setLockedAt(0);
            task.setLockUuid("");
        }
        persist(task);
        return true;
    }

    @Override
    public boolean markComplete(AsyncTask task) {
        Log.info("Mark task complete: id={0} handler={1} customKey={2}", task.getId(), task.getHandlerName(), task.getCustomKey());
        task.setCompletedAt(mils());
        persist(task);
        return true;
    }


    public boolean lockForProcessing(AsyncTask task) {
        return false;
    }


    @Override
    public void deleteOldTasks() {
        Long before = DateUtils.utcNow().minusDays(40).toInstant().toEpochMilli();
        DB.instance().execute("DELETE FROM `stallion_async_tasks` WHERE completedAt > 0 AND completedAt<? ", before);
    }

}
