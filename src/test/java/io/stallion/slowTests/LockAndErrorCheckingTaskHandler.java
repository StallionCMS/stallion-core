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

package io.stallion.slowTests;

import io.stallion.Context;
import io.stallion.asyncTasks.AsyncTaskHandlerBase;
import io.stallion.exceptions.AppException;

import java.io.File;

public class LockAndErrorCheckingTaskHandler extends AsyncTaskHandlerBase {
    public static int handledCount = 0;
    public static boolean lockFileFound = false;

    @Override
    public void process() {
        handledCount++;
        String lockedFilePath = Context.getSettings().getDataDirectory() + "/async-tasks/locked/" + getTask().getId().toString() + ".json";
        if (new File(lockedFilePath).exists()) {
            lockFileFound = true;
        }
        throw new AppException(String.format("Intentionally fail to process task=%s", getTask().getId()));
    }
}
