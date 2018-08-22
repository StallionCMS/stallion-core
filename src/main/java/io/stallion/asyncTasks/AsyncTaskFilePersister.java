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

import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.file.ItemFileChangeEventHandler;
import io.stallion.dataAccess.file.JsonFilePersister;
import io.stallion.fileSystem.FileSystemWatcherService;
import io.stallion.services.Log;
import io.stallion.utils.DateUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class AsyncTaskFilePersister extends JsonFilePersister implements AsyncTaskPersister {

    @Override
    public List fetchAll() {
        File[] folders = new File[]{
                new File(getBucketFolderPath()),
                new File(getBucketFolderPath() + "/locked"),
                new File(getBucketFolderPath() + "/pending"),
                new File(getBucketFolderPath() + "/failed"),
                new File(getBucketFolderPath() + "/completed"),
                new File(getBucketFolderPath() + "/manualRetry")
        };
        for(File folder: folders) {
            if (!folder.isDirectory()) {
                folder.mkdirs();
            }
        }
        FileFilter fileFilter = new RegexFileFilter("\\d+\\d+\\d+\\d+.*\\.json");
        File root = new File(getBucketFolderPath() + "/pending");
        File[] files = root.listFiles(fileFilter);
        List items = new ArrayList<>();
        for (File file : files) {
            items.add(fetchOne(file.getAbsolutePath()));
        }
        return items;
    }


    /**
     * This callback occurs when the watcher finds a new file added to manual retry
     * We load the task, delete the original file, then re-enqueue the task
     * @param relativePath
     */
    @Override
    public void watchEventCallback(String relativePath) {
        Log.fine("file changed: {0}", relativePath);
        String path = getBucketFolderPath() + "/manualRetry/" + relativePath;
        File file = new File(path);
        // If this is a result of a delete, event we return
        if (!file.isFile()) {
            return;
        }
        AsyncTask task = (AsyncTask)fetchOne(path);
        // Reset all the errors so we will reprocess
        task.setLockUuid("").setTryCount(0).setLockedAt(0).setFailedAt(0).setErrorMessage("");
        boolean deleted = new File(path).delete();
        if (deleted) {
            AsyncCoordinator.instance().enqueue(task);
        }
    }

    /**
     * The watcher for JsonTaskPersister only watches the manual entry folder.
     * If a task fails too many times, then we fix the problem, we need a way to force the
     * failed tasks to be retried. The way to do this is to manually edit the JSON, then move
     * the file into the manualRetry folder. The watcher will pick it up, and move it into the
     * pending queue
     */
    @Override
    public void attachWatcher()  {
        String folderToWatch = getBucketFolderPath() + "/manualRetry";
        if (!new File(folderToWatch).isDirectory()) {
            new File(folderToWatch).mkdirs();
        }
        FileSystemWatcherService.instance().registerWatcher(
                new ItemFileChangeEventHandler(this)
                        .setExtension(".json")
                        .setWatchedFolder(folderToWatch)
                        .setWatchTree(false));
    }


    public boolean lockForProcessing(AsyncTask task)  {
        Log.fine("Locking task for processing {0}", task.getId());
        //String errMessage = String.format("Failed to lock task id=%s class=%s customKey=%s", task.getId(), task.getHandlerName(), task.getCustomKey();
        if (task.getLockedAt() > 0) {
            return false;
        }
        File file = new File(pathForPending(task));
        File dest = new File(pathForLocked(task));
        boolean succeeded = file.renameTo(dest);
        if (!succeeded) {
            return false;
        }
        task.setLockedAt(DateUtils.mils());
        task.setLockUuid("thread-" + Thread.currentThread().getId());
        persist(task);
        return true;
    }

    public boolean markComplete(AsyncTask task) {
        File file = new File(pathForLocked(task));
        File dest = new File(pathForCompleted(task));
        boolean succeeded = file.renameTo(dest);
        if (!succeeded) {
            return false;
        }
        task.setCompletedAt(DateUtils.mils());
        persist(task);
        return true;
    }

    public boolean markFailed(AsyncTask task, Throwable e) {
        File file = new File(fullFilePathForObj(task));
        task.setTryCount(task.getTryCount() + 1);

        task.setErrorMessage(e.toString() + ExceptionUtils.getStackTrace(e));
        if (task.getTryCount() >= 5) {
            task.setFailedAt(DateUtils.mils());
        } else {
            task.setExecuteAt(DateUtils.mils() + ((2^task.getTryCount())*1000));
            task.setLockedAt(0);
            task.setLockUuid("");
        }
        File dest = new File(fullFilePathForObj(task));
        boolean succeeded = file.renameTo(dest);
        if (!succeeded) {
            return false;
        }
        persist(task);
        return true;
    }

    @Override
    public String fullFilePathForObj(Model model) {
        AsyncTask task = (AsyncTask)model;
        String path;
        if (task.getCompletedAt() > 0) {
            path = pathForCompleted(task);
        } else if (task.getFailedAt() > 0) {
            path = pathForFailed(task);
        } else if (task.getLockedAt() > 0) {
            path = pathForLocked(task);
        } else {
            path = pathForPending(task);
        }
        return path;
    }

    @Override
    public void deleteOldTasks() {
        File dir = new File(getBucketFolderPath() + "/completed");
        Long before = DateUtils.utcNow().minusDays(40).toInstant().toEpochMilli();
        for(File f: dir.listFiles()) {
            if (!f.isFile() || f.isHidden()) {
                 continue;
            }
            if (f.getName().startsWith(".") || f.getName().startsWith("~") || f.getName().startsWith("#")) {
                continue;
            }
            if (f.getName().endsWith(".json")) {
                continue;
            }
            if (f.lastModified() < before) {
                f.delete();
            }
        }

    }

    @Override
    public String relativeFilePathForObj(Model model) {
        AsyncTask task = (AsyncTask)model;
        String path = fullFilePathForObj(model);
        return path.replace(getBucketFolderPath(), "");
    }

    private String pathForCompleted(AsyncTask task) {
        String path = getBucketFolderPath() + "/completed/";
        path += task.getId() + ".json";
        return path;
    }

    private String pathForPending(AsyncTask task) {
        String path = getBucketFolderPath() + "/pending/";
        path += task.getId() + ".json";
        return path;
    }

    private String pathForLocked(AsyncTask task) {
        String path = getBucketFolderPath() + "/locked/";
        path += task.getId() + ".json";
        return path;
    }

    private String pathForFailed(AsyncTask task) {
        String path = getBucketFolderPath() + "/failed/";
        path += task.getId() + ".json";
        return path;
    }


}
