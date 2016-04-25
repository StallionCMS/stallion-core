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

import io.stallion.dal.DalRegistry;
import io.stallion.dal.db.DB;
import io.stallion.exceptions.UsageException;
import io.stallion.plugins.javascript.JsAsyncTaskHandler;
import io.stallion.services.Log;
import static io.stallion.utils.Literals.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The AsyncCoordinator handles the running of asychronous tasks. The coordinator
 * continuously polls for new tasks, and dispatches them to an executable pool. If
 * the task fails, it is marked for a later retry.
 *
 */
public abstract class AsyncCoordinator extends Thread {

    private static AsyncCoordinator INSTANCE;

    ExecutorService pool;
    List<AsyncTaskExecuteRunnable> threads;
    private boolean triggerShutDown = false;
    private boolean synchronousMode = false;
    private List<ClassLoader> extraClassLoaders = list();

    public static AsyncCoordinator instance() {
        return INSTANCE;
    }



    public static void initAndStart() {
        init();
        startup();
    }

    /**
     * Load the coordinator class so that tasks and handlers can be registered, but
     * don't actually execute any tasks.
     */
    public static void init() {
        if (INSTANCE != null) {
            throw new UsageException("You cannot init the AsyncCoordinator twice!");
        }
        if (DB.available()) {
            INSTANCE = new AsyncDbCoordinator();
            AsyncTaskController.registerDbBased();
        } else {
            INSTANCE = new AsyncFileCoordinator();
            AsyncTaskController.registerFileBased();
        }
    }

    public static void initEphemeralSynchronousForTests() {
        if (INSTANCE != null) {
            throw new RuntimeException("You cannot start the AsyncCoordinator twice!");
        }
        if (AsyncTaskController.instance() == null) {
            AsyncTaskController.registerEphemeral();
        }
        AsyncCoordinator thread;

        thread = new AsyncFileCoordinator();

        thread.synchronousMode = true;
        INSTANCE = thread;
    }

    public static void initForTests() {

        if (INSTANCE != null) {
            throw new RuntimeException("You cannot start the AsyncCoordinator twice!");
        }
        if (AsyncTaskController.instance() == null) {
            AsyncTaskController.registerEphemeral();
        }
        AsyncCoordinator thread = new AsyncFileCoordinator();
        thread.synchronousMode = true;
        INSTANCE = thread;

    }

    /**
     * Actually start the loop to poll for tasks and execute them.
     */
    public static void startup() {
        if (INSTANCE.synchronousMode) {
            return;
        }
        if (INSTANCE.isAlive()) {
            throw new RuntimeException("You cannot start the AsyncCoordinator twice!");
        }
        Log.fine("Starting async coordinator.");
        INSTANCE.start();

    }




    protected AsyncCoordinator() {
        threads = new ArrayList<>();
        int poolSize = 4;
        pool = Executors
                .newFixedThreadPool(poolSize);
    }



    public void run() {
        while (!triggerShutDown) {
            boolean taskExecuted = false;
            try {
                taskExecuted = executeNext(mils());
            } catch (Exception e) {
                Log.exception(e, "Error in main sync loop");
            }
            if (!taskExecuted) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {

                }
            }
        }
        Log.finer("mark all async threads for shutdown");
        for (AsyncTaskExecuteRunnable thread: threads) {
            thread.setTriggerShutdown(true);
        }
        Log.finer("shutting down the pool");
        pool.shutdownNow();
        try {
            while (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                Log.finer("wating for all async threads to terminate");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Execute the text in the queue, if any exists.
     *
     * @return
     */
    public boolean executeNext() {
        return executeNext(mils());
    }

    /**
     * Execute the next task, passing in the current time. For purposes of running
     * tests, you can pass in any time you want.
     *
     * @param now - milliseconds since the epoch.
     * @return
     */
    public boolean executeNext(Long now) {
        AsyncTask task = findAndLockNextTask(now);
        if (task == null) {
            return false;
        }
        AsyncTaskExecuteRunnable runnable = new AsyncTaskExecuteRunnable(task);
        if (isSynchronousMode()) {
            runnable.run(true);
        } else {
            pool.submit(runnable);
        }
        return true;
    }


    /**
     * Register a handler that will run tasks of a given name.
     *
     * @param name
     * @param cls
     */
    public void registerHandler(String name, Class cls) {
        AsyncTaskExecuteRunnable.registerClass(name, cls);
    }

    /**
     * Register a handler that will run tasks of a given name.
     *
     * @param handler
     */
    public void registerHandler(Class<JsAsyncTaskHandler> handler) {
        JsAsyncTaskHandler instance = null;
        try {
            instance = handler.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        AsyncTaskExecuteRunnable.registerClass(instance.getHandlerClassName(), handler);
    }

    /**
     * Find the next task that is unlocked and ready for execution, lock it, and return it.
     *
     * @param now
     * @return
     */
    protected abstract AsyncTask findAndLockNextTask(Long now);

    /**
     * Save a new task to the data store
     *
     * @param task
     */
    protected abstract void saveNewTask(AsyncTask task);

    /**
     * Update a task with new information.
     *
     * @param task
     * @param executeAtChanged
     */
    public abstract void updateTask(AsyncTask task, boolean executeAtChanged);

    /**
     * Mark the task as completed.
     *
     * @param task
     * @return
     */
    public abstract boolean markCompleted(AsyncTask task);

    /**
     * Mark the task as failed.
     *
     * @param task
     * @param throwable
     * @return
     */
    public abstract boolean markFailed(AsyncTask task, Throwable throwable);

    /**
     * Enqueue a new task runner; a task object will automatically be created for
     * this handler.
     *
     * @param handler
     * @return
     */
    public abstract AsyncTask enqueue(AsyncTaskHandler handler);

    /**
     * Enque a new task handler. CustomKey should be a globally unique key. It can be later
     * used to update the task, or to prevent double enqueuing. For instance, if you were
     * writing a calendar application, you might create a task to notify people 1 hour
     * before each event. You could create a customKey with the user and event id in it.
     * Then if the user changes the event time, you can use the customKey to find the task,
     * and update it with a new executeAt time.
     *
     *
     * @param handler - the task handler that will be executed.
     * @param customKey - a user generated unique key that allows you to prevent dupes or later update the task
     * @param executeAt - when you want the task to execute, in epoch milliseconds
     * @return
     */
    public abstract AsyncTask enqueue(AsyncTaskHandler handler, String customKey, long executeAt);

    /**
     * Enqueue a task object, normally you enqueue using task handler, which will then
     * call this method.
     *
     * @param task
     */
    public abstract void enqueue(AsyncTask task);

    /**
     * Called when a task is loaded from the data store during the boot phase.
     *
     * @param task
     */
    public abstract void onLoadTaskOnBoot(AsyncTask task);

    /**
     * Get the number of tasks waiting to be executed.
     * @return
     */
    public abstract int getPendingTaskCount();

    public abstract AsyncTaskPersister getTaskPersister();

    /**
     * Check to see if the task with the given id exists in the queue
     * @param taskId
     * @return
     */
    public abstract boolean hasTaskWithId(Long taskId);


    public static void shutDownForTests() {
        for(AsyncTaskExecuteRunnable runnable: INSTANCE.threads) {
            runnable.setTriggerShutdown(true);
        }
        DalRegistry.instance().deregister("async_tasks");
        INSTANCE.pool.shutdown();
        INSTANCE = null;
    }

    /**
     * Shutdown, while waiting for all task handlers to finish executing.
     */
    public static void gracefulShutdown() {
        if (INSTANCE == null) {
            return;
        }
        INSTANCE.triggerShutDown = true;
        for(AsyncTaskExecuteRunnable runnable: INSTANCE.threads) {
            runnable.setTriggerShutdown(true);
        }
        INSTANCE.pool.shutdown();
        INSTANCE.interrupt();

        int maxMinutesToWait = 5;
        int xMax = maxMinutesToWait*60*10;
        Log.info("Waiting for all async stuff to terminate.");
        for (int x=0; x<(xMax+10);x++) {
            if (INSTANCE.pool.isTerminated()) {
                break;
            }
            if (x % 5000 == 0) {
                Log.info("Waiting for all async tasks to finish before shutting down.");
            }
            if (x > xMax) {
                Log.warn("Tasks still running waiting {0} minutes! Doing hard shutdown.", maxMinutesToWait);
                break;
            }
            try {
                Thread.sleep(100);
            } catch(InterruptedException e){

            }

        }
        INSTANCE = null;
    }


    /**
     * true if is in test mode, where we run tests synchronously rather than in a background thread
     * @return
     */
    public boolean isSynchronousMode() {
        return synchronousMode;
    }


    public void registerClassLoader(ClassLoader loader) {
        getExtraClassLoaders().add(loader);
    }

    public List<ClassLoader> getExtraClassLoaders() {
        return extraClassLoaders;
    }


}
