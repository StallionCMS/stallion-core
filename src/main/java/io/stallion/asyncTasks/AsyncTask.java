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
import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;
import io.stallion.plugins.javascript.JsAsyncTaskHandler;
import io.stallion.services.Log;
import io.stallion.utils.json.JSON;

import javax.persistence.Column;
import javax.persistence.Table;

import static io.stallion.utils.Literals.empty;

@Table(name="stallion_async_tasks")
public class AsyncTask extends ModelBase implements Comparable<AsyncTask> {
    private long createdAt = 0;
    private long updatedAt = 0;
    private String handlerName;
    private String customKey = null;
    private long lockedAt = 0;
    private long failedAt = 0;
    private long completedAt = 0;
    private long originallyScheduledFor;
    private long executeAt = 0;
    private boolean neverRetry = false;
    private String lockUuid = "";
    private String secret = "";
    private int tryCount = 0;
    private String errorMessage = "";
    private String dataJson = "";
    private String localMode = "";


    public static AsyncTaskController controller() {
        return (AsyncTaskController)Context.dal().get("st-async-async-tasks");
    }

    public AsyncTask() {

    }

    public AsyncTask(AsyncTaskHandler handler) {
        this(handler, null, 0);

    }

    public AsyncTask(AsyncTaskHandler handler, String customKey, long executeAt) {
        setHandler(handler).setCustomKey(customKey).setExecuteAt(executeAt);
    }

    public AsyncTask enqueue() {
        AsyncCoordinator.instance().enqueue(this);
        return this;
    }

    /**
     * When the task was created, in epoch milliseconds
     * @return
     */
    @Column
    public long getCreatedAt() {
        return createdAt;
    }

    public AsyncTask setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * When the task was last updated, in epoch milliseconds
     * @return
     */
    @Column
    public long getUpdatedAt() {
        return updatedAt;
    }

    public AsyncTask setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * The name of the handler class. When loading a task after reboot, the handler class
     * will be looked up and loaded by using this name.
     *
     * @return
     */
    @Column
    public String getHandlerName() {
        return handlerName;
    }

    /**
     * Will use the handler class name, for later reloading, and then convert the handler to JSON
     * and assign the data to the dataJson field of this task.
     * @param handler
     * @return
     */
    public AsyncTask setHandler(AsyncTaskHandler handler) {

        if (handler instanceof JsAsyncTaskHandler) {
            setHandlerName(((JsAsyncTaskHandler) handler).getHandlerClassName());
            setDataJson(JSON.stringify(((JsAsyncTaskHandler) handler).getInternalMap()));
            Log.info("STRIFIGYD {0}", JSON.stringify(((JsAsyncTaskHandler) handler).getInternalMap()));
        } else {
            setHandlerName(handler.getClass().getCanonicalName());
            setDataJson(JSON.stringify(handler));
        }

        return this;
    }

    public AsyncTask setHandlerName(String handlerName) {
        this.handlerName = handlerName;
        return this;
    }

    /**
     * A user generated unique key for the task, used for updating the task or preventing duplicates.
     * @return
     */
    @Column
    @UniqueKey
    public String getCustomKey() {
        return customKey;
    }

    public AsyncTask setCustomKey(String customKey) {
        this.customKey = customKey;
        return this;
    }

    /**
     * When the task was locked, in epoch milliseconds
     * @return
     */
    @Column
    public long getLockedAt() {
        return lockedAt;
    }

    public AsyncTask setLockedAt(long lockedAt) {
        this.lockedAt = lockedAt;
        return this;
    }

    /**
     * When the task last failed, in epoch milliseconds
     * @return
     */
    @Column
    public long getFailedAt() {
        return failedAt;
    }

    public AsyncTask setFailedAt(long failedAt) {
        this.failedAt = failedAt;
        return this;
    }

    /**
     * When the task successfully completed, in epoch milliseconds
     * @return
     */
    @Column
    public long getCompletedAt() {
        return completedAt;
    }

    public AsyncTask setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    /**
     * When the task was scheduled for originally, before any failures that made the
     * coordinator reschedule it for a retried execution. For a task that has never been
     * run, this time will equal the executeAt time.
     *
     * @return
     */
    @Column
    public long getOriginallyScheduledFor() {
        return originallyScheduledFor;
    }

    public AsyncTask setOriginallyScheduledFor(long originallyScheduledFor) {
        this.originallyScheduledFor = originallyScheduledFor;
        return this;
    }

    /**
     * When the task should execute, in epoch milliseconds.
     *
     * @return
     */
    @Column
    public long getExecuteAt() {
        return executeAt;
    }

    public AsyncTask setExecuteAt(long executeAt) {
        this.executeAt = executeAt;
        return this;
    }

    /**
     * If true, the task should never be retried on failure.
     * @return
     */
    @Column
    public boolean isNeverRetry() {
        return neverRetry;
    }

    public AsyncTask setNeverRetry(boolean neverRetry) {
        this.neverRetry = neverRetry;
        return this;
    }

    /**
     * A unique lock key, generated by the async persister when locking a task.
     * @return
     */
    @Column
    public String getLockUuid() {
        return lockUuid;
    }

    public AsyncTask setLockUuid(String lockUuid) {
        this.lockUuid = lockUuid;
        return this;
    }

    /**
     * A secret key, generated by the async persister, that can be used for
     * looking up the task object.
     * @return
     */
    @Column
    @UniqueKey
    public String getSecret() {
        return secret;
    }

    public AsyncTask setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * How many times the task has been tried to execute, incremented every time the task fails.
     *
     * @return
     */
    @Column
    public int getTryCount() {
        return tryCount;
    }

    public AsyncTask setTryCount(int tryCount) {
        this.tryCount = tryCount;
        return this;
    }

    /**
     * The stack trace from the last failure.
     * @return
     */
    @Column
    public String getErrorMessage() {
        return errorMessage;
    }

    public AsyncTask setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * The async handler instance will be serialized to this field during task creation. When the task
     * is executed, the this field will be deserialized and used to hydrate the fields of the
     * handler class.
     * @return
     */
    @Column
    public String getDataJson() {
        return dataJson;
    }

    public AsyncTask setDataJson(String data) {
        this.dataJson = data;
        return this;
    }

    /**
     * Parses getDataJson() and returns the resulting class.
     *
     * @param cls
     * @param <V>
     * @return
     */
    public <V> V getData(Class<? extends V> cls) {
        if (!empty(this.dataJson )) {
            return JSON.parse(this.dataJson, cls);
        } else {
            return null;
        }
    }

    /**
     * JSON Stringifys the object and sets this.dataJson
     *
     * @param o
     */
    public void setData(Object o) {
        this.dataJson = JSON.stringify(o);
    }

    /**
     * Implements the comparable interface to see which task should be executed sooner.
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(AsyncTask o) {
        return Long.compare(this.getExecuteAt(), o.getExecuteAt());
    }

    public String getLocalMode() {
        return localMode;
    }

    public AsyncTask setLocalMode(String localMode) {
        this.localMode = localMode;
        return this;
    }
}
