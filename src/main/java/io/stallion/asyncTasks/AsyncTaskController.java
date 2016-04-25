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

import io.stallion.Context;
import io.stallion.dal.base.DalRegistration;
import io.stallion.dal.base.StandardModelController;
import io.stallion.dal.base.NoStash;
import io.stallion.dal.db.DB;

import java.util.HashSet;
import java.util.Set;

import static io.stallion.utils.Literals.*;


public class AsyncTaskController extends StandardModelController<AsyncTask> {
    protected static final String BUCKET_NAME = "async_tasks";
    protected static final String PATH_NAME = "async-tasks";
    protected static final String TABLE_NAME = "stallion_async_tasks";

    private static Set<String> _keyFields;

    static {
        _keyFields = new HashSet<>();
        _keyFields.add("customKey");
    }

    public static AsyncTaskController instance() {
        return (AsyncTaskController) Context.dal().get(BUCKET_NAME);
    }

    public static void register() {
        if (DB.available()) {
            registerDbBased();
        } else {
            registerFileBased();
        }
    }



    public static void registerDbBased() {
        DalRegistration registration = new DalRegistration()
                .setControllerClass(AsyncTaskController.class)
                .setModelClass(AsyncTask.class)
                .setBucket(BUCKET_NAME)
                .setTableName(TABLE_NAME)
                .setPersisterClass(AsyncTaskDbPersister.class)
                .setStashClass(NoStash.class)
                .setWritable(true);
        Context.dal().registerDal(registration);
    }


    public static void registerFileBased() {
        DalRegistration registration = new DalRegistration()
                .setControllerClass(AsyncTaskController.class)
                .setModelClass(AsyncTask.class)
                .setBucket(BUCKET_NAME)
                .setPath(PATH_NAME)
                .setPersisterClass(AsyncTaskFilePersister.class)
                .setWritable(true)
                .setShouldWatch(true)
                .setUseDataFolder(true);

        Context.dal().registerDal(registration);
    }

    /** Registers an ephemeral version of the controller that will not actually store tasks to files.
     * Used for unittests when we don't want to store the tasks
     */
    public static void registerEphemeral() {
        DalRegistration registration = new DalRegistration()
                .setControllerClass(AsyncTaskController.class)
                .setModelClass(AsyncTask.class)
                .setPath(PATH_NAME)
                .setBucket(BUCKET_NAME)
                .setPersisterClass(DummyTaskPersister.class)
                .setShouldWatch(false)
                .setUseDataFolder(false)
                .setWritable(true);
        Context.dal().registerDal(registration);
    }




    /**
     * This gets called when we restart the application and reload all the tasks from the /pending folder
     * @param task
     */
    public void onPostLoadItem(AsyncTask task) {
        AsyncCoordinator.instance().onLoadTaskOnBoot(task);
    }






    public TaskHealthInfo buildHealthInfo() {
        TaskHealthInfo info = new TaskHealthInfo();
        info.setPendingTasks(AsyncCoordinator.instance().getPendingTaskCount());
        //info.setCompletedTasks(seenTaskIds.size());
        Long thirtyAgo = mils() - 30 * 60 * 1000;
        info.setStuckTasks(filterChain().filter("originallyScheduledFor", thirtyAgo, "<").filter("completedAt", 0L).count());
        return info;
    }



    @Override
    public Set<String> getUniqueFields() {
        return _keyFields;
    }

    @Override
    public void reset() {
        super.reset();
    }



}
