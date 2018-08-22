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

import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.Log;
import io.stallion.utils.json.JSON;

import java.util.HashMap;
import java.util.Map;

public class AsyncTaskExecuteRunnable implements Runnable {

    private Boolean triggerShutdown = false;
    private AsyncTask task;
    private static final Map<String, Class> classCache = new HashMap<>();

    public AsyncTaskExecuteRunnable(AsyncTask task) {
        this.task = task;
    }

    public static void registerClass(String name, Class<? extends AsyncTaskHandler> cls) {
        classCache.put(name, cls);
    }

    @Override
    public void run() {
        run(false);
    }



    public void run(boolean synchronousMode) {
        try {
            Log.info("Executing task: {0}", task.getId());

            Class cls = lookupClass(task.getHandlerName());
            Log.info("Loaded async handler class: {0}", cls.getName());
            AsyncTaskHandler handler = (AsyncTaskHandler)JSON.parse(task.getDataJson(), cls);

            handler.setTask(task);
            handler.process();
            AsyncCoordinator.instance().markCompleted(task);
        } catch(Exception e) {
            if (synchronousMode) {
                throw new RuntimeException(e);
            }
            String dump = "";
            try {
                dump = JSON.stringify(task);
            } catch (Exception e1) {
            }
            Log.exception(e, "Exception processing task id:{0} handler:{1} customKey:{2} dump:{3}",
                    task.getId(), task.getHandlerName(), task.getCustomKey(), dump);
            try {
                AsyncCoordinator.instance().markFailed(task, e);
                if (task.getTryCount() < 5) {
                    Log.info("Adding task back into the queue {0}", task.getId());
                    ((AsyncFileCoordinator)AsyncCoordinator.instance()).getTaskQueue().add(task);
                }
            } catch (Exception persistException) {
                Log.exception(persistException, "Exception persisting task changes for id:{0}", task.getId());
            }
        }
    }

    private Class lookupClass(String className) throws ClassNotFoundException {
        if (classCache.containsKey(className)) {
            return classCache.get(className);
        }
        Class cls = null;

        try {
            cls = getClass().getClassLoader().loadClass(task.getHandlerName());
        } catch (ClassNotFoundException e) {

        }
        if (cls != null) {
            classCache.put(className, cls);
        }
        for (StallionJavaPlugin booter: PluginRegistry.instance().getJavaPluginByName().values()) {
            try {
                cls = booter.getClass().getClassLoader().loadClass(task.getHandlerName());
                if (cls != null) {
                    classCache.put(className, cls);
                    return cls;
                }
            } catch (ClassNotFoundException e) {

            }
        }
        for (ClassLoader loader: AsyncCoordinator.instance().getExtraClassLoaders()) {
            try {
                cls = loader.loadClass(task.getHandlerName());
                if (cls != null) {
                    classCache.put(className, cls);
                    return cls;
                }
            } catch (ClassNotFoundException e) {

            }
        }
        throw new ClassNotFoundException("Class not found: " + className);
    }


    public Boolean getTriggerShutdown() {
        return triggerShutdown;
    }

    public void setTriggerShutdown(Boolean triggerShutdown) {
        this.triggerShutdown = triggerShutdown;
    }

}
