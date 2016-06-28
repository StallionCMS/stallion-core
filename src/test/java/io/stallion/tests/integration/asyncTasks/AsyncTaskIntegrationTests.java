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

package io.stallion.tests.integration.asyncTasks;

import io.stallion.Context;
import io.stallion.asyncTasks.AsyncCoordinator;
import io.stallion.asyncTasks.AsyncTask;

import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.utils.DateUtils;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class AsyncTaskIntegrationTests extends AppIntegrationCaseBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/a_minimal_site");
        File tasksDir = new File(Context.getSettings().getDataDirectory() + "/st-async-async-tasks");
        if (tasksDir.isDirectory()) {
            FileUtils.deleteDirectory(tasksDir);
        }
        //AsyncCoordinator.initForTests();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        AsyncCoordinator.shutDownForTests();
        cleanUpClass();
    }

    /**
     * Tests processing tasks with no threading involved for easy debugging.
     */
    @Test
    public void testTaskProcessing() {
        AsyncCoordinator.instance().getExtraClassLoaders().add(MyTaskHandler.class.getClassLoader());

        AsyncTask task1 = new AsyncTask(new MyTaskHandler().setSomeThing("fooBar"));
        task1.enqueue();

        AsyncTask task2 = new AsyncTask(new MyTaskHandler().setSomeThing("whizbong"), "my-custom-key", DateUtils.mils()+200);
        task2.enqueue();


        AsyncCoordinator.instance().executeNext(DateUtils.mils());

        Assert.assertEquals(1, MyTaskHandler.handledCount);

        AsyncCoordinator.instance().executeNext(DateUtils.mils());
        // Still at 1
        Assert.assertEquals(1, MyTaskHandler.handledCount);

        // Now second task gets executed
        AsyncCoordinator.instance().executeNext(DateUtils.mils()+300);
        Assert.assertEquals(2, MyTaskHandler.handledCount);
        Assert.assertTrue(MyTaskHandler.handledSomethings.contains("fooBar"));
        Assert.assertTrue(MyTaskHandler.handledSomethings.contains("whizbong"));
    }

}

