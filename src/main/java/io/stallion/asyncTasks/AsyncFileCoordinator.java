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

import io.stallion.Context;
import io.stallion.services.Log;
import io.stallion.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.ServerErrorException;
import java.util.HashSet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.or;


public class AsyncFileCoordinator extends AsyncCoordinator {

    private AtomicLong counter = new AtomicLong(1);
    private PriorityBlockingQueue<AsyncTask> taskQueue = new PriorityBlockingQueue<>();
    private HashSet<Object> seenTaskIds = new HashSet<>();




    @Override
    public AsyncTask enqueue(AsyncTaskHandler handler) {
        return enqueue(handler, null, 0);
    }

    @Override
    public AsyncTask enqueue(AsyncTaskHandler handler, String customKey, long executeAt) {

        AsyncTask task = new AsyncTask(handler, customKey, executeAt);
        enqueue(task);
        return task;
    }

    @Override
    public void enqueue(AsyncTask task) {
        if (StringUtils.isEmpty(task.getHandlerName())) {
            throw new ServerErrorException("A task was enqueued, but getHandlerName() was blank", 500);
        }

        //TODO: If the task handler is not registered, then add it
        //TODO: If it does not exist, raise a ConfigException

        if (task.getOriginallyScheduledFor() > 0) {
            task.setExecuteAt(task.getOriginallyScheduledFor());
        } else if (!empty(task.getExecuteAt()) && empty(task.getOriginallyScheduledFor())) {
            task.setOriginallyScheduledFor(task.getExecuteAt());
        }
        if (!StringUtils.isEmpty(task.getCustomKey())) {
            try {
                AsyncTask existing = AsyncTaskController.instance().forUniqueKey("customKey", task.getCustomKey());
                if (existing != null && existing.getCompletedAt() > 0) {
                    Log.info("Existing task already ran with customKey={0}", task.getCustomKey());
                    return;
                } else if (existing != null) {
                    existing.setDataJson(task.getDataJson());
                    Boolean executeAtChanged = false;
                    if (task.getExecuteAt() > 0 && existing.getTryCount() == 0 && existing.getExecuteAt() != task.getExecuteAt()) {
                        existing.setExecuteAt(task.getExecuteAt());
                        existing.setOriginallyScheduledFor(task.getOriginallyScheduledFor());
                        executeAtChanged = true;
                    }
                    existing.setHandlerName(task.getHandlerName());
                    updateTask(existing, executeAtChanged);
                    Log.info("Updating existing task with customKey={0}", task.getCustomKey());
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (task.getExecuteAt() == 0) {
            // Set to the past, so will execute immediately even if picked up by another server with a slower clock
            task.setExecuteAt(DateUtils.mils() - 15000);
        }
        String[] parts = task.getHandlerName().split("\\.");
        String simpleName = parts[parts.length-1];
        Long i = counter.getAndIncrement();
        if (task.getId() == null || StringUtils.isEmpty(task.getId().toString())) {
            String keyPart = empty(task.getCustomKey()) ? "" : "-" + task.getCustomKey();
            task.setId(Context.dal().getTickets().nextId());
        }
        saveNewTask(task);
        if (AsyncCoordinator.instance() != null
                && AsyncCoordinator.instance().isSynchronousMode()
                // We only want to execute tasks synchronously that are supposed to happen basically
                // immediately -- not tasks that are scheduled for the far future.
                && (task.getExecuteAt() < (DateUtils.mils() + 60000))
                ) {

            AsyncCoordinator.instance().executeNext();
        }
    }

    @Override
    public void updateTask(AsyncTask task, boolean executeAtChanged) {
        AsyncTaskController.instance().save(task);
        if (executeAtChanged) {
            // Need to remove and add back in order to re-sort
            // This could get expensive if the Queue is big
            taskQueue.remove(task);
            taskQueue.add(task);
        }
    }

    @Override
    public void saveNewTask(AsyncTask task) {
        AsyncTaskController.instance().save(task);
        Log.info("Adding task to the queue: id={0} handler={1} customKey={2}", task.getId(), task.getHandlerName(), task.getCustomKey());
        taskQueue.add(task);

    }

    @Override
    public AsyncTask findAndLockNextTask(Long now) {
        now = or(now, DateUtils.mils());
        if (getTaskQueue().size() == 0) {
            return null;
        }
        AsyncTask task = getTaskQueue().poll();
        if (task == null) {
            return null;
        }
        if (task.getExecuteAt() > now) {
            getTaskQueue().put(task);
            return null;
        }
        if (task.getLockedAt() > 0) {
            // Locked by someone else, carry on
            Log.warn("Task is already locked {0} {1} currentThread={2} lockUid={3}", task.getId(), task.getLockedAt(), Thread.currentThread().getId(), task.getLockUuid());
            return null;
        }


        Log.finer("Queue size is {0}", getTaskQueue().size());
        boolean locked = lockTaskForExecution(task);
        if (!locked) {
            Log.warn("Unable to lock task! {0}", task.getId());
            return null;
        }
        return task;
    }


    protected boolean lockTaskForExecution(AsyncTask task) {
        if (seenTaskIds.contains(task.getId())) {
            Log.warn("Trying to lock a task with an id that has already been seen! {0}", task.getId());
            return false;
        }
        seenTaskIds.add(task.getId());
        return getTaskPersister().lockForProcessing(task);
    }

    @Override
    public boolean markCompleted(AsyncTask task) {
        return getTaskPersister().markComplete(task);
    }

    @Override
    public boolean markFailed(AsyncTask task, Throwable throwable) {
        if (seenTaskIds.contains(task.getId())) {
            seenTaskIds.remove(task.getId());
        }
        return getTaskPersister().markFailed(task, throwable);
    }

    @Override
    public AsyncTaskPersister getTaskPersister() {
        return (AsyncTaskPersister) AsyncTaskController.instance().getPersister();
    }

    public boolean hasSeenTask(String id) {
        return seenTaskIds.contains(id);
    }

    public boolean hasTaskWithId(Long id) {
        AsyncTask mirror = new AsyncTask();
        mirror.setId(id);
        return taskQueue.contains(mirror);
    }

    @Override
    public boolean hasPendingTaskWithCustomKey(String key) {
        AsyncTask task = AsyncTaskController.instance().forUniqueKey("customKey", key);
        if (task == null) {
            return false;
        }
        if ((task.getExecuteAt() == 0 || task.getExecuteAt() > DateUtils.mils()) && task.getCompletedAt() < 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasTaskWithCustomKey(String key) {
        AsyncTask task = AsyncTaskController.instance().forUniqueKey("customKey", key);
        if (task == null) {
            return false;
        }
        return true;
    }


    public PriorityBlockingQueue<AsyncTask> getTaskQueue() {
        return taskQueue;
    }

    public void onLoadTaskOnBoot(AsyncTask task) {
        if (!StringUtils.isEmpty(task.getId().toString()) && task.getCompletedAt() == 0 && task.getLockedAt() == 0) {
            taskQueue.add(task);
        }
    }

    @Override
    public int getPendingTaskCount() {
        return taskQueue.size();
    }

}
