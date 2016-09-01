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

package io.stallion.tests.integration.jobs;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import io.stallion.Context;
import io.stallion.jobs.JobCoordinator;
import io.stallion.jobs.JobDefinition;
import io.stallion.jobs.Schedule;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import org.apache.commons.io.FileUtils;
import org.junit.*;


public class JobsSqlTests extends AppIntegrationCaseBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/mysql_site");
        JobCoordinator.initForTesting();
        //JobStatusController.selfRegister();
        //JobCoordinator.initForTesting();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        cleanUpClass();
    }

    @Before
    @After
    public void cleanup() {
        new JobsTests().deleteJobStatuses();
    }

    /**
     * Tests processing jobs with no threading involved for easy debugging.
     */
    @Test
    public void testJobProcessing() throws Exception {
        Log.info("sql test processing");
        new JobsTests().testJobProcessing();
    }
}
