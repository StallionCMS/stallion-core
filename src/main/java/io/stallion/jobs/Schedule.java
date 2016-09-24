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

import io.stallion.Context;
import io.stallion.exceptions.AppException;
import io.stallion.exceptions.ConfigException;
import io.stallion.users.IUser;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A data structure representing the schedule for a recurring task,
 * with helper methods to find the next recurring time.
 */
public class Schedule {
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;
    public static final int SUNDAY = 7;

    private String timeZoneId = "";
    private Long timeZoneForUserId;

    private Minutes _minutes = new Minutes();
    private Hours _hours = new Hours();
    private Days _days = new Days();
    private Months _months = new Months();

    /**
     * Gets the next datetime matching the schedule, in the Users timezone
     *
     * @return
     */
    public ZonedDateTime nextAt() {
        ZoneId zoneId = null;
        if (!StringUtils.isEmpty(timeZoneId)) {
            zoneId = ZoneId.of(timeZoneId);
        } else if (timeZoneForUserId != null) {
            IUser user = UserController.instance().forId(timeZoneForUserId);
            if (user != null && !StringUtils.isEmpty(user.getTimeZoneId())) {
                zoneId = ZoneId.of(user.getTimeZoneId());
            }
        }

        if (zoneId == null) {
            zoneId = ZoneId.of("UTC");
        }
        ZonedDateTime dt = ZonedDateTime.now(zoneId);
        return nextAt(dt);
    }

    public ZoneId getZoneId() {
        ZoneId zoneId = null;
        if (!StringUtils.isEmpty(timeZoneId)) {
            zoneId = ZoneId.of(timeZoneId);
        } else if (timeZoneForUserId != null) {
            IUser user = UserController.instance().forId(timeZoneForUserId);
            if (user != null && !StringUtils.isEmpty(user.getTimeZoneId())) {
                zoneId = ZoneId.of(user.getTimeZoneId());
            }
        }
        if (zoneId == null) {
            zoneId = ZoneId.of("UTC");
        }
        return zoneId;
    }

    /**
     * Gets the next datetime matching the schedule, in UTC
     *
     * @return
     */
    public ZonedDateTime utcNextAt() {
        return nextAt(DateUtils.utcNow());
    }

    /**
     * Gets the next datetime matching the schedule, in the application timezone, as defined in the settings
     *
     * @return
     */
    public ZonedDateTime serverLocalNextAt() {
        ZoneId zoneId = null;
        if (zoneId == null) {
            zoneId = Context.getSettings().getTimeZoneId();
        }
        if (zoneId == null) {
            zoneId = ZoneId.of("UTC");
        }
        ZonedDateTime dt = ZonedDateTime.now(zoneId);
        return nextAt(dt);
    }

    /**
     * Gets the next datetime matching the schedule, passing in the current
     * date from which to look. Used for testing.
     *
     * @param startingFrom
     * @return
     */
    public ZonedDateTime nextAt(ZonedDateTime startingFrom) {
        if (!startingFrom.getZone().equals(getZoneId())) {
            startingFrom = startingFrom.withZoneSameInstant(getZoneId());
        }
        ZonedDateTime dt = new NextDateTimeFinder(startingFrom).find();
        return dt.withZoneSameInstant(ZoneId.of("UTC"));
    }

    /**
     * Returns true if this schedule matches the passed in datetime
     * @param dt
     * @return
     */
    public boolean matchesDateTime(ZonedDateTime dt) {
        ZonedDateTime now = dt.withSecond(0).withNano(0);
        ZonedDateTime nextRun = nextAt(now).withSecond(0).withNano(0);
        if (now.isEqual(nextRun)) {
            return true;
        }
        return false;
    }

    /**
     * Get a schedule instance that will run every night, at some random minute
     * during the 5AM hour
     * @return
     */
    public static Schedule daily() {
        return new Schedule()
                .randomMinute()
                .hours(5)
                .everyDay()
                .everyMonth()
                .verify();
    }

    /**
     * Run at the top of every hour
     * @return
     */
    public static Schedule hourly() {
        return new Schedule()
                .minutes(0)
                .everyHour()
                .everyDay()
                .everyMonth()
                .verify();
    }

    /** Get a schedule instance that will run on at 5AM UTC on the second and fourth Friday, every month
     *
     */
    public static Schedule paydays() {
        return new Schedule()
                .minutes(0)
                .hours(5)
                // Second and fourth Friday of the month
                .daysOfWeekMonth(DayOfWeek.FRIDAY, 2, 4)
                .everyMonth()
                .verify();

    }



    /**
     * Set which minutes of the hour the task will run at
     *
     * @param minutes
     * @return
     */
    public Schedule minutes(Integer ...minutes) {
        this._minutes.verifyAndUpdateUnset();
        for (Integer i : minutes) {
            this._minutes.add(i);
        }
        return this;
    }

    /**
     * Set the schedule to run every minute
     * @return
     */
    public Schedule everyMinute() {
        this._minutes.verifyAndUpdateUnset();
        this._minutes.setEvery(true);
        return this;
    }

    /**
     * Set the schedule to run on a random minute every hour
     * @return
     */
    public Schedule randomMinute() {
        this._minutes.verifyAndUpdateUnset();
        this._minutes.setRandom(true);
        return this;
    }

    /**
     * Set which hours of the day the task should run at
     * @param hours
     * @return
     */
    public Schedule hours(Integer ...hours) {
        this._hours.verifyAndUpdateUnset();
        for(Integer hour: hours) {
            this._hours.add(hour);
        }
        return this;
    }

    /**
     * Set the task to run each hour of the day
     * @return
     */
    public Schedule everyHour() {
        this._hours.verifyAndUpdateUnset();
        this._hours.setEvery(true);
        return this;
    }

    /**
     * Set the task to run every single day
     * @return
     */
    public Schedule everyDay() {
        this._days.verifyAndUpdateUnset();
        this._days.setEvery(true);
        return this;
    }

    /**
     * Set the days of the month on which the schedule is to run.
     *
     * @param daysOfMonth
     * @return
     */
    public Schedule daysOfMonth(Integer ...daysOfMonth) {
        this._days.verifyAndUpdateUnset();
        this._days.setIntervalType(Days.IntervalType.DAY_OF_MONTH);
        for(Integer day: daysOfMonth) {
            this._days.add(day);
        }
        return this;
    }

    /**
     * Set the days of the week on which the task is to run.
     *
     * @param days
     * @return
     */
    public Schedule daysOfWeek(DayOfWeek ...days) {
        this._days.verifyAndUpdateUnset();
        this._days.setIntervalType(Days.IntervalType.DAY_OF_WEEK);
        for(DayOfWeek day: days) {
            this._days.add(day.getValue());
        }
        return this;
    }


    /**
     * Set the day of the week and which weeks of the month combination
     * the schedule is to match.
     *
     * @param dayOfWeek
     * @param weeksOfMonth
     * @return
     */
    public Schedule daysOfWeekMonth(DayOfWeek dayOfWeek, Integer ...weeksOfMonth) {
        this._days.verifyAndUpdateUnset();
        this._days.setIntervalType(Days.IntervalType.DAY_AND_WEEKS_OF_MONTH);
        this._days.setDayOfWeek(dayOfWeek);
        for(Integer weekOfMonth: weeksOfMonth) {
            this._days.add(weekOfMonth);
        }
        return this;
    }

    /**
     * Runs the given days of the week, every other week.
     *
     * @param startingAt
     * @param days
     * @return
     */
    public Schedule daysBiweekly(Long startingAt, DayOfWeek ...days) {
        this._days.verifyAndUpdateUnset();
        this._days.setIntervalType(Days.IntervalType.BIWEEKLY_DAY_OF_WEEK);
        for(DayOfWeek day: days) {
            this._days.add(day.getValue());
        }
        ZonedDateTime startingWeek = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startingAt), ZoneId.of("UTC"));
        // Get the Monday 12PM of that week
        startingWeek = startingWeek.minusDays(startingWeek.getDayOfWeek().getValue()-1).withSecond(0).withHour(12).withMinute(0).withNano(0);
        this._days.setStartingDate(startingWeek);
        return this;
    }

    /**
     * Runs every month
     * @return
     */
    public Schedule everyMonth() {
        this._months.verifyAndUpdateUnset();
        this._months.setEvery(true);
        return this;
    }

    /**
     * Set to runs on the given month(s)
     *
     * @param months
     * @return
     */
    public Schedule months(Integer ...months) {
        this._months.verifyAndUpdateUnset();
        for(Integer month: months) {
            this._months.add(month);
        }
        return this;
    }

    /**
     * Verify that this is a valid schedule.
     *
     * @return
     * @throws ConfigException
     */
    public Schedule verify() throws ConfigException {
        for (BaseTimings timing: new BaseTimings[]{_minutes, _hours, _days, _months}) {
            if (timing.isUnset()) {
                throw new ConfigException("You did not configure the schedule field: " + timing.getClass().getSimpleName());
            }
            if (timing.size() == 0 && !timing.isEvery() && !timing.isRandom()) {
                throw new ConfigException("Timings are not set, nor is every interval set for schedule field: " + timing.getClass().getSimpleName());
            }
        }
        return this;
    }

    public Schedule timezone(String timeZoneId){
        this.timeZoneId = timeZoneId;
        return this;
    }

    /**
     * Helper class to actually do the work of finding the next run time.
     */
    public class NextDateTimeFinder {
        private ZonedDateTime startingFrom;

        public NextDateTimeFinder(ZonedDateTime startingFrom) {
            this.startingFrom = startingFrom;
        }


        public ZonedDateTime find() {

            // Verify the schedule is valid
            verify();

            ZonedDateTime dt = startingFrom;
            dt = dt.withSecond(0).withNano(0);

            if (_minutes.isRandom()) {
                dt.withMinute(ThreadLocalRandom.current().nextInt(0, 60));
            }


            for(int brake=0; brake<2000; brake++) { // fake while-loop with emergency brake pattern. Worst case(s): It is February 1, task runs on January 31st, we loop 11 plus 30 times
                Mismatched mismatched = checkMismatch(dt);
                if (mismatched.equals(Mismatched.NONE)) {
                    return dt;
                } else if (mismatched.equals(Mismatched.MONTH)) {
                    // Month doesn't match the schedule, skip to first day of the next month
                    dt = dt.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0);
                } else if (mismatched.equals(Mismatched.DAY)) {
                    /// Day doesn't match the schedule, skip to the first hour and minute of the next day
                    dt = dt.plusDays(1).withHour(0).withMinute(0);
                } else if (mismatched.equals(Mismatched.HOUR)) {
                    // Hour doesn't match the schedule, skip to the first minute of the next hour
                    dt = dt.plusHours(hoursToAdd(dt)).withMinute(0);
                } else if (mismatched.equals(Mismatched.MINUTE)) {
                    // Minute doesn't match the schedule, find the next viable minute
                    dt = dt.plusMinutes(minutesToAdd(dt));
                } else {
                    throw new AppException("This should never happen but it did. Invalid DealBreaker value: " + mismatched);
                }
            }
            throw new ConfigException("This should never happen. A schedule was created from which a next date could not be found.");
        }

        private Mismatched checkMismatch(ZonedDateTime dt) {
            if (!_months.isEvery() && !_months.contains(dt.getMonthValue())) {
                return Mismatched.MONTH;
            }
            if (!_days.isEvery()) {
                if (Days.IntervalType.DAY_OF_MONTH.equals(_days.getIntervalType())) {
                    if (!_days.contains(dt.getDayOfMonth())) {
                        return Mismatched.DAY;
                    }
                } else if (Days.IntervalType.DAY_OF_WEEK.equals(_days.getIntervalType())) {
                    if (!_days.contains(dt.getDayOfWeek().getValue())) {
                        return Mismatched.DAY;
                    }
                } else if (Days.IntervalType.DAY_AND_WEEKS_OF_MONTH.equals(_days.getIntervalType())) {
                    if (!_days.getDayOfWeek().equals(dt.getDayOfWeek())) {
                        return Mismatched.DAY;
                    }
                    int weekOfMonth = 1 + (dt.getDayOfMonth() / 7);
                    if (!_days.contains(weekOfMonth)) {
                        return Mismatched.DAY;
                    }
                } else if (Days.IntervalType.BIWEEKLY_DAY_OF_WEEK.equals(_days.getIntervalType())) {
                    if (!_days.contains(dt.getDayOfWeek().getValue())) {
                        return Mismatched.DAY;
                    }
                    // Calculate the elapsed weeks between the starting date and now
                    // if there is an odd number of weeks, then the week doesn't match based on a biweekly schedule
                    ZonedDateTime thisWeek = dt.minusDays(dt.getDayOfWeek().getValue()-1).withSecond(0).withHour(12).withMinute(0).withNano(0);
                    Period period = Period.between(thisWeek.toLocalDate(), _days.getStartingDate().toLocalDate());
                    int weekDif = period.getDays() / 7;
                    if (weekDif %2 != 0) {
                        return Mismatched.DAY;
                    }
                } else {
                    // This should never happen
                    throw new AppException("This should never happen but it did. Invalid intervalTye: " + _days.getIntervalType());
                }
            }
            if (!_hours.isEvery() && !_hours.contains(dt.getHour())) {
                return Mismatched.HOUR;
            }

            if (!_minutes.isRandom() && !_minutes.isEvery() && !_minutes.contains(dt.getMinute())) {
                return Mismatched.MINUTE;
            }

            return Mismatched.NONE;
        }


        public int hoursToAdd(ZonedDateTime dt) {
            if (_hours.isEvery()) {
                return 1;
            }
            for (Integer hour: _hours) {
                if (hour > dt.getHour()) {
                    return hour - dt.getHour();
                }
            }
            // Wrap around to the next day
            return 24 + _hours.get(0) - dt.getHour();
        }

        public int minutesToAdd(ZonedDateTime dt) {
            if (_minutes.isEvery()) {
                return 1;
            }
            if (_minutes.isRandom()) {
                return 0;
            }
            for (Integer minute: _minutes) {
                if (minute > dt.getMinute()) {
                    return minute - dt.getMinute();
                }
            }
            // We wrap around to the next hour
            return 60 + _minutes.get(0) - dt.getMinute();
        }

    }


    enum Mismatched {
        NONE,
        MONTH,
        DAY,
        HOUR,
        MINUTE
    }


}
