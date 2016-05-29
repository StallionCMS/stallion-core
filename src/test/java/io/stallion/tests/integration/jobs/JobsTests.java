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

package io.stallion.tests.integration.jobs;

import io.stallion.Context;
import io.stallion.jobs.JobDefinition;
import io.stallion.jobs.Schedule;
import io.stallion.jobs.JobCoordinator;
import io.stallion.testing.AppIntegrationCaseBase;
import static org.junit.Assert.assertEquals;

import io.stallion.tests.integration.jobs.ExampleJobOne;
import io.stallion.tests.integration.jobs.ExampleJobThree;
import io.stallion.tests.integration.jobs.ExampleJobTwo;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class JobsTests extends AppIntegrationCaseBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/a_minimal_site");
        File tasksDir = new File(Context.getSettings().getDataDirectory() + "/st-jobs-job-status");
        if (tasksDir.isDirectory()) {
            FileUtils.deleteDirectory(tasksDir);
        }
        //JobStatusController.selfRegister();
        JobCoordinator.initForTesting();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        cleanUpClass();
    }

    /**
     * Tests processing jobs with no threading involved for easy debugging.
     */
    @Test
    public void testJobProcessing() throws Exception {

        // Define and load job 1, to run at 30 minutes after the hour
        JobDefinition job1 = new JobDefinition() {{
            setJobClass(ExampleJobOne.class);
            setAlertThresholdMinutes(150);
            setSchedule(new Schedule() {{
                minutes(30);
                everyHour();
                everyDay();
                everyMonth();
                verify();
            }});
        }};



        // Define and load job 2, to run at 12:30 every day
        JobDefinition job2 = new JobDefinition()
                .setJobClass(ExampleJobTwo.class)
                .setAlertThresholdMinutes(3000)
                .setSchedule(new Schedule()
                        .minutes(30)
                        .hours(12)
                        .everyDay()
                        .everyMonth()
                        .verify());
        // Define and load job 3, to run at 5PM on Tuesday
        JobDefinition job3 = new JobDefinition()
                .setJobClass(ExampleJobThree.class)
                .setAlertThresholdMinutes(3000)
                .setSchedule(new Schedule()
                        .minutes(0)
                        .hours(17)
                        .daysOfWeek(DayOfWeek.TUESDAY)
                        .everyMonth()
                        .verify());

        ZonedDateTime now = ZonedDateTime.of(2015, 1, 18, 10, 10, 12, 0, ZoneId.of("UTC"));
        JobCoordinator.instance().registerJobForTest(job1, now);
        JobCoordinator.instance().registerJobForTest(job2, now);
        JobCoordinator.instance().registerJobForTest(job3, now);


        // Run for time at 11:30 - Job 1 should run
        //now = ZonedDateTime.of(2015, 1, 18, 11, 30, 7, 121, ZoneId.of("UTC"));
        //JobCoordinator.instance().resetForDateTime(now.minusMinutes(1)).executeJobsForCurrentTime(now);
        JobCoordinator.instance().executeJobsForCurrentTime(
                ZonedDateTime.of(2015, 1, 18, 11, 30, 7, 121, ZoneId.of("UTC")));
        assertEquals(1, ExampleJobOne.RUN_COUNT);
        assertEquals(0, ExampleJobTwo.RUN_COUNT);
        assertEquals(0, ExampleJobThree.RUN_COUNT);

        // Run for time at 11:30 again - no additional runs should happen
        JobCoordinator.instance().executeJobsForCurrentTime(
                ZonedDateTime.of(2015, 1, 18, 11, 30, 7, 121, ZoneId.of("UTC")));
        //JobCoordinator.instance().resetForDateTime(now.minusMinutes(1)).executeJobsForCurrentTime(now);
        assertEquals(1, ExampleJobOne.RUN_COUNT);
        assertEquals(0, ExampleJobTwo.RUN_COUNT);
        assertEquals(0, ExampleJobThree.RUN_COUNT);

        // Run for time 12:30 - Job 1 and Job 2 should run
        JobCoordinator.instance().executeJobsForCurrentTime(
                ZonedDateTime.of(2015, 1, 18, 12, 30, 7, 121, ZoneId.of("UTC")));
        //JobCoordinator.instance().resetForDateTime(now.minusMinutes(1)).executeJobsForCurrentTime(now);
        assertEquals(2, ExampleJobOne.RUN_COUNT);
        assertEquals(1, ExampleJobTwo.RUN_COUNT);
        assertEquals(0, ExampleJobThree.RUN_COUNT);

        // Run for time 5PM monday - no jobs should run
        JobCoordinator.instance().executeJobsForCurrentTime(
                ZonedDateTime.of(2015, 1, 19, 5, 0, 7, 121, ZoneId.of("UTC")));
        //JobCoordinator.instance().resetForDateTime(now.minusMinutes(1)).executeJobsForCurrentTime(now);
        assertEquals(2, ExampleJobOne.RUN_COUNT);
        assertEquals(1, ExampleJobTwo.RUN_COUNT);
        assertEquals(0, ExampleJobThree.RUN_COUNT);

        // Run for 12:30 Tuesday - Job 1 and Job 2 should run
        JobCoordinator.instance().executeJobsForCurrentTime(
                ZonedDateTime.of(2015, 1, 20, 12, 30, 7, 121, ZoneId.of("UTC")));
        //JobCoordinator.instance().resetForDateTime(now.minusMinutes(1)).executeJobsForCurrentTime(now);
        assertEquals(3, ExampleJobOne.RUN_COUNT);
        assertEquals(2, ExampleJobTwo.RUN_COUNT);
        assertEquals(0, ExampleJobThree.RUN_COUNT);

        // Run for time 5PM Tuesday - job 3 should run
        JobCoordinator.instance().executeJobsForCurrentTime(
                ZonedDateTime.of(2015, 1, 20, 17, 0, 7, 121, ZoneId.of("UTC")));
        JobCoordinator.instance().executeJobsForCurrentTime(
                ZonedDateTime.of(2015, 1, 20, 17, 0, 7, 121, ZoneId.of("UTC")));
//        JobCoordinator.instance().executeJobsForCurrentTime(
//                ZonedDateTime.of(2015, 1, 20, 17, 0, 7, 121, ZoneId.of("UTC")));

        assertEquals(3, ExampleJobOne.RUN_COUNT);
        assertEquals(2, ExampleJobTwo.RUN_COUNT);
        assertEquals(1, ExampleJobThree.RUN_COUNT);


    }

}



