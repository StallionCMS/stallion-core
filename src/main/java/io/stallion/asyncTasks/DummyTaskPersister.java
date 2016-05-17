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

import io.stallion.dataAccess.DummyPersister;


public class DummyTaskPersister extends DummyPersister implements AsyncTaskPersister {

    @Override
    public boolean markFailed(AsyncTask task, Throwable e) {
        return true;
    }

    @Override
    public boolean markComplete(AsyncTask task) {
        return true;
    }

    @Override
    public boolean lockForProcessing(AsyncTask task) {
        return true;
    }


    @Override
    public void deleteOldTasks() {

    }
}
