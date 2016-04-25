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

package io.stallion.jobs;

import io.stallion.exceptions.ConfigException;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

/**
 *
 * */
class Days extends BaseTimings {
    private boolean isNextBusinessDayOfMonth = false;
    private DayOfWeek dayOfWeek;
    private IntervalType intervalType;
    private ZonedDateTime startingDate;

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public IntervalType getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(IntervalType intervalType) {
        this.intervalType = intervalType;
    }

     public ZonedDateTime getStartingDate() {
         return startingDate;
     }

     public void setStartingDate(ZonedDateTime startingDate) {
         this.startingDate = startingDate;
     }

     public enum IntervalType {
        DAY_OF_MONTH,
        DAY_OF_WEEK,
        BIWEEKLY_DAY_OF_WEEK,
        DAY_AND_WEEKS_OF_MONTH
    }

    @Override
    public boolean add(Integer i) {
        if (IntervalType.DAY_OF_MONTH.equals(intervalType)) {
            if (i < 1 || i > 31) {
                throw new ConfigException("Day must be between 1 and 31 when intervalType is DAY_OF_MONTH");
            }
        } else if (IntervalType.DAY_OF_WEEK.equals(intervalType) || IntervalType.BIWEEKLY_DAY_OF_WEEK.equals(intervalType)) {
            if (i < 1 || i > 6) {
                throw new ConfigException("Day must be between 1 and 6 when intervalType is DAY_OF_WEEK. 1 is Monday, 6 is Saturday");
            }
        }  else if (IntervalType.DAY_AND_WEEKS_OF_MONTH.equals(intervalType)) {
            if (i < 1 || i > 5) {
                throw new ConfigException("Week must be between 1 and 5 when intervalType is DAY_AND_WEEKS_OF_MONTH 1 is the first week, 5 is the last week, if it exists");
            }
        }
        return super.add(i);
    }

    public boolean isNextBusinessDayOfMonth() {
        return isNextBusinessDayOfMonth;
    }

    public void setNextBusinessDayOfMonth(boolean isNextBusinessDayOfMonth) {
        this.isNextBusinessDayOfMonth = isNextBusinessDayOfMonth;
    }


}
