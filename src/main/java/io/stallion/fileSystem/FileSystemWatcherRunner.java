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

package io.stallion.fileSystem;

import com.sun.nio.file.SensitivityWatchEventModifier;
import io.stallion.services.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.list;
import static io.stallion.utils.Literals.safeLoop;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * A side thread that watches the file system, and responds to file change
 * events, and calls the registered watch event handler.
 *
 *
 */
public class FileSystemWatcherRunner implements Runnable {
    private WatchService watcher;
    private Boolean shouldRun = true;
    private Map<String, IWatchEventHandler> watchedByPath = new HashMap<>();


    public FileSystemWatcherRunner() {
        this(false);
    }

    public FileSystemWatcherRunner(boolean isCodeWatcher) {

        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception exc) {
            System.err.print(exc);
        }
        Log.info("FileSystemWatcher run method is complete.");
    }

    public void registerWatcher(IWatchEventHandler handler) {

        Log.fine("Watch folder {0} handler={1}", handler.getWatchedFolder(), handler.getClass().getSimpleName());
        registerWatcherForFolder(handler, handler.getWatchedFolder());
        if (handler.getWatchTree()) {
            List<File> directories = list(new File(handler.getWatchedFolder()));
            for(int x: safeLoop(100000)) {
                if (directories.size() == 0) {
                    break;
                }
                File directory = directories.remove(0);
                //Collection<File> subdirectories = FileUtils.listFiles(
                //        directory,
                //        DirectoryFileFilter.DIRECTORY,
                //        DirectoryFileFilter.DIRECTORY
                //);
                File[] subdirectories = directory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
                for (File dir : subdirectories) {
                    Log.finer("Register recursive watcher: " + dir.getAbsolutePath());
                    directories.add(dir);
                    registerWatcherForFolder(handler, dir.getAbsolutePath());
                }
            }
        }
        watchedByPath.put(handler.getWatchedFolder(), handler);
    }

    private void registerWatcherForFolder(IWatchEventHandler handler, String folder) {
        Path itemsDir = FileSystems.getDefault().getPath(folder);
        try {
            if (new File(itemsDir.toString()).isDirectory()) {
                itemsDir.register(watcher, new WatchEvent.Kind[]{
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE
                }, SensitivityWatchEventModifier.HIGH);
                Log.fine("Folder registered with watcher {0}", folder);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doRun() {
        while (shouldRun) {
            Log.fine("Running the file system watcher.");
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                Log.warn("Interuppted the watcher!!!");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.info("Exit watcher run method.");
                    return;
                }
                continue;
            }
            Log.fine("Watch event key taken. Runner instance is {0}", this.hashCode());

            for (WatchEvent<?> event : key.pollEvents()) {

                WatchEvent.Kind<?> kind = event.kind();
                Log.fine("Event is " + kind);
                // This key is registered only
                // for ENTRY_CREATE events,
                // but an OVERFLOW event can
                // occur regardless if events
                // are lost or discarded.
                if (kind == OVERFLOW) {
                    continue;
                }

                // The filename is the
                // context of the event.
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                // Ignore emacs autosave files
                if (filename.toString().contains(".#")) {
                    continue;
                }
                Log.finer("Changed file is {0}", filename);
                Path directory = (Path)key.watchable();
                Log.finer("Changed directory is {0}", directory);
                Path fullPath = directory.resolve(filename);
                Log.fine("Changed path is {0}", fullPath);
                Boolean handlerFound = false;
                for (IWatchEventHandler handler: watchedByPath.values()) {
                    Log.finer("Checking matching handler {0} {1}", handler.getInternalHandlerLabel(), handler.getWatchedFolder());
                    // Ignore private files
                    if (filename.getFileName().startsWith(".")) {
                        continue;
                    }
                    if ((handler.getWatchedFolder().equals(directory.toAbsolutePath().toString())
                           || (handler.getWatchTree() && directory.startsWith(handler.getWatchedFolder())))
                        && (StringUtils.isEmpty(handler.getExtension()) || fullPath.toString().endsWith(handler.getExtension()))
                    ) {
                        String relativePath = filename.getFileName().toString();
                        Log.info("Handling {0} with watcher {1} for folder {2}", filename, handler.getClass().getName(), handler.getWatchedFolder());
                        try {
                            handler.handle(relativePath, fullPath.toString(), kind, event);
                            handlerFound = true;
                        } catch(Exception e) {
                            Log.exception(e, "Exception processing path={0} handler={1}", relativePath, handler.getClass().getName());
                        }
                    }
                }
                if (!handlerFound) {
                    Log.info("No handler found for {0}", fullPath);
                }
            }
            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                Log.warn("Key invalid! Exit watch.");
                break;
            }
        }
    }

    public void shutdown() {
        setShouldRun(false);
        try {
            if (watcher != null) {
                watcher.close();
            }
            watcher = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        watchedByPath = new HashMap<>();
    }
    public Boolean getShouldRun() {
        return shouldRun;
    }

    public void setShouldRun(Boolean shouldRun) {
        this.shouldRun = shouldRun;
    }

}
