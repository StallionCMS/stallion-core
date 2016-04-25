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

package io.stallion.slowTests;

import io.stallion.asyncTasks.AsyncTaskHandlerBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExampleTaskHandler extends AsyncTaskHandlerBase {

    public static AtomicInteger handledCount = new AtomicInteger(0);
    public static List<String> handledTasks = new ArrayList<>();
    public static List<Integer> handledTaskNumbers = new ArrayList<>();
    public static List<String> handledMyKeys = new ArrayList<>();

    private String myKey = "";
    private Integer taskNumber = 0;

    @Override
    public void process() {
        handledCount.incrementAndGet();
        handledTasks.add(getTask().getId().toString());
        handledMyKeys.add(getMyKey());
        handledTaskNumbers.add(getTaskNumber());
    }


    public String getMyKey() {
        return myKey;
    }

    public ExampleTaskHandler setMyKey(String myKey) {
        this.myKey = myKey;
        return this;
    }

    public Integer getTaskNumber() {
        return taskNumber;
    }

    public ExampleTaskHandler setTaskNumber(Integer taskNumber) {
        this.taskNumber = taskNumber;
        return this;
    }
}
