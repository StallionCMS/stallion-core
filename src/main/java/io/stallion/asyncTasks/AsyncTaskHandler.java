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

/**
 * This implements a handler for async tasks. The data your task needs for
 * execution should be added as bean properties to your implementing class.
 * So if you had a CalendarReminderHandler, you might have a property
 * userId and eventId. Then your "process" method would look up the user,
 * look up the event, and send the user an email about the event. Since
 * the entire class will be serialized, you prefer to use ids as properties,
 * rather than large objects. Store the eventId or userId, not the event or user.
 *
 */
public interface AsyncTaskHandler {
    /**
     * Override this to write your custom code for actually executing the task
     */
    public void process();

    /**
     * The internal AsyncTask object associated with this instance.
     * @return
     */
    public AsyncTask getTask();
    public AsyncTaskHandler setTask(AsyncTask task);
}
