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

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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


public class AsyncMysqlSlowTests extends AppIntegrationCaseBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/mysql_site");
        //AsyncCoordinator.shutDownForTests();
        //AsyncCoordinator.initAndStart();
        //AsyncCoordinator.instance().registerClassLoader(AsyncTaskSlowTests.class.getClassLoader());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        AsyncCoordinator.gracefulShutdown();
        cleanUpClass();
    }


    @Test
    public void testLoadAfterRestart() throws InterruptedException {
         Log.warn("Implement me");

    }

    @Test
    public void testFlow() throws Exception {
        Log.warn("Implement me");



    }

}
