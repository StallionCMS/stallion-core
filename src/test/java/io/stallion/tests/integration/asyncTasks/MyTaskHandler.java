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

package io.stallion.tests.integration.asyncTasks;

import io.stallion.asyncTasks.AsyncTaskHandlerBase;

import java.util.ArrayList;
import java.util.List;

public class MyTaskHandler extends AsyncTaskHandlerBase {

    public static int handledCount = 0;
    public static List<String> handledTasks = new ArrayList<>();
    public static List<String> handledSomethings = new ArrayList<>();

    private String someThing;

    @Override
    public void process() {
        handledCount++;
        handledTasks.add(getTask().getId().toString());
        handledSomethings.add(getSomeThing());

    }

    public String getSomeThing() {
        return someThing;
    }

    public MyTaskHandler setSomeThing(String someThing) {
        this.someThing = someThing;
        return this;
    }
}
