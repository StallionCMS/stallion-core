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

import io.stallion.services.Log;

import java.util.concurrent.*;

/**
 * The SimpleAsyncRunner is an alternative way to run asynchronous tasks from AsyncCoordinator.
 * It is lighter-weight. All it does is accept a Runnable and execute it on a separate thread.
 * It does not handle failures, exceptions, scheduling, de-duping or anything else. This is
 * most useful for something like sending an exeception email, where it is more important
 * to be light-weight than it is important to have fail-safes.
 *
 */
public class SimpleAsyncRunner {
    private static SimpleAsyncRunner _instance;
    private static boolean syncMode = false;

    public static SimpleAsyncRunner instance() {
        return _instance;
    }

    public static void load(boolean testMode) {
        _instance = new SimpleAsyncRunner();
        if (testMode) {
            setSyncMode(true);
        }
    }

    public static void setSyncMode(boolean isSyncMode) {
        syncMode = isSyncMode;
    }

    public static void shutdown() {
        if (_instance == null) {
            return;
        }
        _instance.pool.shutdown();
        try {
            _instance.pool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        _instance = null;
    }

    private ThreadPoolExecutor pool;
//ThreadPoolExecutor
    public SimpleAsyncRunner() {
        pool = (ThreadPoolExecutor)Executors
                .newFixedThreadPool(5);
        pool.setThreadFactory(new ExceptionCatchingThreadFactory(pool.getThreadFactory()));
    }

    public SimpleAsyncRunner submit(Runnable runnable) {
        if (syncMode) {
            runnable.run();
        } else {
            pool.submit(runnable);
        }
        return this;
    }

    public static class ExceptionCatchingThreadFactory implements ThreadFactory {
        private final ThreadFactory delegate;

        private ExceptionCatchingThreadFactory(ThreadFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    Log.exception(e, "Error in SimpleAsyncRunner executing thread for {0}", r.getClass().getCanonicalName());
                }
            });
            return t;

            
        }
    }

}
