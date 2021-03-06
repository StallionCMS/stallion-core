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

package io.stallion.jobs;

import io.stallion.jobs.Schedule;
import org.junit.Assert;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ScheduleTests  {

    @Test
    public void testHourly() {
        ZonedDateTime now;
        ZonedDateTime expected;
        ZonedDateTime actual;

        {
            /* With time zone */
            Schedule schedule = new Schedule()
                    .minutes(1, 31)
                    .hours(9, 10, 11, 12, 13, 14, 15, 16, 17, 18)
                    .timezone("America/New_York")
                    .everyDay()
                    .everyMonth()
                    .verify();
            // 11:12 UTC/7:12AM New York, should schedule for 9:01AM New York, 13:01 UTC
            now = ZonedDateTime.of(2014, 5, 1, 11, 12, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            expected = ZonedDateTime.of(2014, 5, 1, 13, 1, 0, 0, ZoneId.of("UTC"));
            Assert.assertEquals(expected, actual);

            // 17:12 UTC/1:12PM New York, should schedule for 1:31PM New York, 17:31 UTC
            now = ZonedDateTime.of(2014, 5, 1, 17, 12, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            expected = ZonedDateTime.of(2014, 5, 1, 17, 31, 0, 0, ZoneId.of("UTC"));
            Assert.assertEquals(expected, actual);

        }

        /* Every hour on the 17th minute */
        {
            Schedule schedule = new Schedule()
                    .minutes(17)
                    .everyHour()
                    .everyDay()
                    .everyMonth()
                    .verify();
            now = ZonedDateTime.of(2014, 4, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            expected = ZonedDateTime.of(2014, 4, 1, 12, 17, 0, 0, ZoneId.of("UTC"));
            Assert.assertEquals(expected, actual);
        }

        /* Every hour on the 25th minute, test not running at 5:30 AM Bug*/
        {
            Schedule schedule = new Schedule()
                    .minutes(25)
                    .everyHour()
                    .everyDay()
                    .everyMonth()
                    .verify();
            now = ZonedDateTime.of(2016, 6, 24, 4, 26, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            expected = ZonedDateTime.of(2016, 6, 24, 5, 25, 0, 0, ZoneId.of("UTC"));
            Assert.assertEquals(expected, actual);
        }

        /* Every hour on the 21st and 37th minute */
        {
            Schedule schedule = new Schedule()
                    .minutes(21, 37)
                    .everyHour()
                    .everyDay()
                    .everyMonth()
                    .verify();
            now = ZonedDateTime.of(2014, 4, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
            expected = ZonedDateTime.of(2014, 4, 1, 12, 21, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2014, 4, 1, 12, 37, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);

        }

    }

    @Test
    public void testDaily() {
        ZonedDateTime now;
        ZonedDateTime expected;
        ZonedDateTime actual;


        /* Every day at 2, 10, 20 */
        {
            Schedule schedule = new Schedule()
                    .minutes(30)
                    .hours(0, 10, 23)
                    .everyDay()
                    .everyMonth()
                    .verify();
            now = ZonedDateTime.of(2014, 4, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
            expected = ZonedDateTime.of(2014, 4, 1, 0, 30, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2014, 4, 1, 10, 30, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);


            expected = ZonedDateTime.of(2014, 4, 1, 23, 30, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);


        }

    }

    @Test
    public void testWeekly() {
        ZonedDateTime now;
        ZonedDateTime expected;
        ZonedDateTime actual;


        /* Every tuesday and thursday 5:30AM */
        {
            Schedule schedule = new Schedule()
                    .minutes(30)
                    .hours(5)
                    .daysOfWeek(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
                    .everyMonth()
                    .verify();
            now = ZonedDateTime.of(2015, 1, 7, 12, 21, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            expected = ZonedDateTime.of(2015, 1, 8, 5, 30, 0, 0, ZoneId.of("UTC"));
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2015, 1, 13, 5, 30, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);


        }


        /* The second and fourth Friday of the month at 5PM  */
        {
            Schedule schedule = new Schedule()
                    .minutes(0)
                    .hours(17)
                    .daysOfWeekMonth(DayOfWeek.FRIDAY, 2, 4)
                    .everyMonth()
                    .verify();

            now = ZonedDateTime.of(2015, 1, 7, 12, 21, 0, 0, ZoneId.of("UTC"));

            expected = ZonedDateTime.of(2015, 1, 9, 17, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2015, 1, 23, 17, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2015, 2, 13, 17, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);

        }



        /* Every other week on Wednesday at 3PM */
        {
            Schedule schedule = new Schedule()
                    .minutes(0)
                    .hours(15)
                    .daysBiweekly(1421463966210L, DayOfWeek.WEDNESDAY)
                    .everyMonth()
                    .verify();
            now = ZonedDateTime.of(2015, 1, 7, 12, 21, 0, 0, ZoneId.of("UTC"));

            expected = ZonedDateTime.of(2015, 1, 14, 15, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2015, 1, 28, 15, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2015, 2, 11, 15, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);

        }

    }

    @Test
    public void testMonthly() {
        ZonedDateTime now;
        ZonedDateTime expected;
        ZonedDateTime actual;


        /* The 15th of the month at 5PM */
        {
            Schedule schedule = new Schedule()
                    .minutes(0)
                    .hours(17)
                    .daysOfMonth(15)
                    .everyMonth()
                    .verify();
            now = ZonedDateTime.of(2015, 1, 7, 12, 21, 0, 0, ZoneId.of("UTC"));

            expected = ZonedDateTime.of(2015, 1, 15, 17, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2015, 2, 15, 17, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2015, 3, 15, 17, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);

        }


        /* The first day of each quarter at 12AM */
        {
            Schedule schedule = new Schedule()
                    .minutes(0)
                    .hours(0)
                    .daysOfMonth(1)
                    .months(1, 4, 7, 10)
                    .verify();
            now = ZonedDateTime.of(2015, 1, 7, 12, 21, 0, 0, ZoneId.of("UTC"));

            expected = ZonedDateTime.of(2015, 4, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(now);
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2015, 7, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);

            expected = ZonedDateTime.of(2015, 10, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
            actual = schedule.nextAt(actual.plusMinutes(1));
            Assert.assertEquals(expected, actual);

        }



    }
}
