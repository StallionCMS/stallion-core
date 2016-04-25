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

package io.stallion.fileSystem;

import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * Starts and stops a separate thread that watches the file system
 * for changes.
 */
public class FileSystemWatcherService {


    private static Thread watchingThread;
    private static FileSystemWatcherRunner _runner;

    public static FileSystemWatcherRunner instance() {
        if (_runner == null) {
            if (_runner == null) {
                throw new UsageException("FileSystemWatcherRunner.load() must be called before can access instance.");
            }
        }
        return _runner;
    }

    public static FileSystemWatcherRunner load() {
        _runner = new FileSystemWatcherRunner();
        return _runner;
    }

    public static FileSystemWatcherRunner start() {
        if (watchingThread != null) {
            return _runner;
        }
        watchingThread = new Thread(instance());
        watchingThread.start();
        return _runner;
    }

    public static void shutdown() {
        if (_runner == null) {
            return;
        }
        _runner.setShouldRun(false);
        if (watchingThread != null) {
            watchingThread.interrupt();
            try {
                watchingThread.join();
            } catch (InterruptedException e) {
                Log.exception(e, "Exception joining watcher thread to shutdown");
            }
        }
        _runner.shutdown();
        _runner = null;
        watchingThread = null;
        Log.finer("File system watcher service is shut down");
    }

}
