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
import io.stallion.asyncTasks.AsyncCoordinator;
import io.stallion.asyncTasks.AsyncFileCoordinator;
import io.stallion.asyncTasks.AsyncTask;
import io.stallion.asyncTasks.AsyncTaskController;
import io.stallion.fileSystem.FileSystemWatcherService;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.utils.DateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Date;


public class AsyncTaskSlowTests extends AppIntegrationCaseBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/a_minimal_site", true);
        AsyncCoordinator.shutDownForTests();
        File tasksDir = new File(Context.getSettings().getDataDirectory() + "/async-tasks");
        if (tasksDir.isDirectory()) {
            FileUtils.deleteDirectory(tasksDir);
        }
        FileSystemWatcherService.start();
        AsyncCoordinator.initAndStart();
        AsyncCoordinator.instance().registerClassLoader(AsyncTaskSlowTests.class.getClassLoader());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        AsyncCoordinator.gracefulShutdown();
        cleanUpClass();
    }



    public void testLoadFromManualEntries() throws IOException, InterruptedException {
        AsyncFileCoordinator coordinator = (AsyncFileCoordinator)AsyncCoordinator.instance();
        // Leave time for the watcher to start running
        Thread.sleep(200);
        String taskJson = "{\n" +
                "  \"deleted\" : false,\n" +
                "  \"isNewInsert\" : false,\n" +
                "  \"createdAt\" : 0,\n" +
                "  \"updatedAt\" : 0,\n" +
                "  \"handlerName\" : \"io.stallion.slowTests.ExampleTaskHandler\",\n" +
                "  \"customKey\" : \"\",\n" +
                "  \"lockedAt\" : 1421371349577,\n" +
                "  \"failedAt\" : 1421371349577,\n" +
                "  \"completedAt\" : 0,\n" +
                "  \"originallyScheduledFor\" : 0,\n" +
                "  \"executeAt\" : 1921371332731,\n" +
                "  \"neverRetry\" : false,\n" +
                "  \"lockUuid\" : null,\n" +
                "  \"secret\" : null,\n" +
                "  \"tryCount\" : 5,\n" +
                "  \"errorMessage\" : null,\n" +
                "  \"dataJson\" : \"{\\n  \\\"myKey\\\" : \\\"myVal\\\",\\n  \\\"taskNumber\\\" : 0\\n}\",\n" +
                "  \"id\" : 1001\n" +
                "}";
        File taskFile = new File(Context.getSettings().getDataDirectory() + "/async-tasks/manualRetry/2015-01-16-0122-1273-ExampleTaskHandler-1421371347569-937506272.json");
        FileUtils.write(taskFile, taskJson);
        for (int t=0; t<100;t++) {
            if (!taskFile.isFile() && coordinator.hasTaskWithId(1001L)) {
                break;
            }
            Thread.sleep(100);
        }
        Assert.assertTrue(!taskFile.isFile());
        Assert.assertTrue(coordinator.hasTaskWithId(1001L));

    }

    @Test
    public void testLoadAfterRestart() throws InterruptedException {
        AsyncFileCoordinator coordinator = (AsyncFileCoordinator)AsyncCoordinator.instance();
        String myKey = "futureKey-" + new Date().getTime();
        AsyncTask task = AsyncCoordinator.instance().enqueue(new ExampleTaskHandler().setMyKey(myKey), "custom-keyz-future", 1921371347569L);
        Assert.assertTrue(coordinator.hasTaskWithId(task.getId()));

        coordinator.getTaskQueue().clear();
        Assert.assertFalse(coordinator.instance().hasTaskWithId(task.getId()));

        AsyncTaskController.instance().reset();

        for (int x=0; x<20; x++) {
            if (coordinator.instance().hasTaskWithId(task.getId())) {
                break;
            }
            Thread.sleep(10);
        }
        Assert.assertTrue(coordinator.instance().hasTaskWithId(task.getId()));

    }


    public void testFlow() throws Exception {
        AsyncCoordinator coordinator = AsyncCoordinator.instance();

        // Create 20 tasks, the first 10 to be executed now, 9 to be executed within the next 2 seconds, 1 to be executed far in the future
        for (int x=1; x<21; x++) {
            long executeAt = 0;
            String customKey = "";
            if (x % 2 == 0) {
                executeAt = DateUtils.mils() + (x * 100);
                customKey = "custom-key-" + x;
            }
            if (x == 18) {
                executeAt = DateUtils.mils() + 99999999;
            }
            AsyncTask task = new AsyncTask(
                    new ExampleTaskHandler().setMyKey("myVal").setTaskNumber(x),
                    customKey,
                    executeAt);
            task.enqueue();
        }

        // Reschedule task 0 for the far future
        Log.info("Reschedule 6 for the far future");
        AsyncTask futureDated = new AsyncTask(
                new ExampleTaskHandler().setMyKey("myVal").setTaskNumber(0),
                "custom-key-6",
                DateUtils.mils() + 1111112500
                );
        futureDated.enqueue();


        // Update task 17 with a new myKey
        Log.info("Update task 2 with a new myKey value");
        AsyncTask updated = new AsyncTask(
                new ExampleTaskHandler().setMyKey("anUpdatedValueFor2").setTaskNumber(2),
                "custom-key-2",
                DateUtils.mils() + 800
        );
        updated.enqueue();

        Assert.assertEquals(20, coordinator.getPendingTaskCount());

        Log.info("Sleep for 3 seconds, all tasks should complete, except for 18 and 19 and, because those were future dated");
        Thread.sleep(5000);

        // Was always scheduled for the far future
        Assert.assertTrue(!ExampleTaskHandler.handledTaskNumbers.contains(18));
        // Was rescheduled for the future
        Assert.assertTrue(!ExampleTaskHandler.handledTaskNumbers.contains(0));
        // Got an updated key
        Assert.assertTrue(ExampleTaskHandler.handledMyKeys.contains("anUpdatedValueFor2"));
        // 20 tasks, minus the two that were way future dated
        Assert.assertEquals(18, ExampleTaskHandler.handledCount.get());

        //// PART II ///////////////////////

        // Duplicate customKey, this task will be ignored
        AsyncTask duplicate = new AsyncTask(
                new ExampleTaskHandler().setMyKey("myVal14Duplicate").setTaskNumber(14),
                "custom-key-14",
                DateUtils.mils()
        );
        duplicate.enqueue();

        // Lock checking task that then fails
        AsyncTask checkingTask = new AsyncTask(
                new LockAndErrorCheckingTaskHandler()
        );
        checkingTask.setTryCount(99); // Won't retry when fails
        checkingTask.enqueue();


        // Still should be 18 tasks, the duplicate custom key task should have been ignored
        Thread.sleep(2000);
        Assert.assertEquals(18, ExampleTaskHandler.handledCount.get());
        Assert.assertTrue(!ExampleTaskHandler.handledMyKeys.contains("myVal15Duplicate"));

        // Locked file was found while running tasks
        Assert.assertTrue(LockAndErrorCheckingTaskHandler.lockFileFound);


        File completedFolder = new File(Context.getSettings().getDataDirectory() + "/async-tasks/completed");
        File lockedFolder = new File(Context.getSettings().getDataDirectory() + "/async-tasks/locked");
        File failedFolder = new File(Context.getSettings().getDataDirectory() + "/async-tasks/failed");
        File pendingFolder = new File(Context.getSettings().getDataDirectory() + "/async-tasks/pending");

        // 18 completed tasks should be in the completed folder
        FileFilter fileFilter = new RegexFileFilter("\\d+.*\\.json");
        Assert.assertEquals(18, completedFolder.listFiles(fileFilter).length);

        // 2 tasks should be in the pending folder
        Assert.assertEquals(2, pendingFolder.listFiles(fileFilter).length);

        // 0 tasks in the locked folder
        Assert.assertEquals(0, lockedFolder.listFiles(fileFilter).length);

        // 1 tasks in the failed folder
        Assert.assertEquals(1, failedFolder.listFiles(fileFilter).length);




    }

}
