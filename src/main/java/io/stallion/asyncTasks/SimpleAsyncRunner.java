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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

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

    public static SimpleAsyncRunner instance() {
        return _instance;
    }

    public static void load() {
        _instance = new SimpleAsyncRunner();
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

    private ExecutorService pool;

    public SimpleAsyncRunner() {
        pool = Executors
                .newFixedThreadPool(5);
    }

    public SimpleAsyncRunner submit(Runnable runnable) {
        pool.submit(runnable);
        return this;
    }

}
