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

package io.stallion.utils;

import io.stallion.Context;
import io.stallion.exceptions.UsageException;
import io.stallion.settings.Settings;
import io.stallion.utils.json.JSON;
import org.parboiled.common.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.emptyInstance;


public class DateUtils {
    static ZoneId UTC = ZoneId.of("UTC");

    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("MMM d, YYYY h:mm a");
    public static final DateTimeFormatter SLUG_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd-HHmm-ssSS");
    public static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final DateTimeFormatter SQL_FORMAT =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Gets the current time in UTC
     * @return a ZonedDateTime with the current time in UTC
     */
    public static ZonedDateTime utcNow()
    {
        return ZonedDateTime.now(UTC);
    }

    /**
     * Milliseconds since the epoch
     * @return
     */
    public static long mils() {
        return new Date().getTime();
    }

    /**
     * Converts milliseconds since the unix epoch to a ZonedDateTime in UTC
     * @param mils
     * @return
     */
    public static ZonedDateTime milsToDateTime(long mils) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(mils), UTC);
    }


    /**
     * Formats a ZonedDateTime into a string with the given DateTimeFormatter pattern
     * @param date
     * @param formatPattern
     * @return
     */
    public static String formatLocalDateFromZonedDate(ZonedDateTime date, String formatPattern) {
        if (date == null) {
            return "";
        }

        ZonedDateTime localDt = date.withZoneSameInstant(Context.getSettings().getTimeZoneId());

        DateTimeFormatter formatter;
        if (StringUtils.isEmpty(formatPattern)) {
            formatter = DEFAULT_FORMAT;
        } else if ("iso".equals(formatPattern.toLowerCase())) {
            formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        } else {
            formatter = DateTimeFormatter.ofPattern(formatPattern);
        }
        return localDt.format(formatter);
    }

    /**
     * Turns the date into a format that has no spaces, colons, or any other invalid characters invalid for a file name
     * The actual format used is: YYYY-MM-dd-HHmm-ssSS
     * @param epochMillis
     * @return
     */
    public static String slugifyDate(Long epochMillis) {
        return slugifyDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), UTC));
    }

    /**
     * Turns the date into a format that has no spaces, colons, or any other invalid characters invalid for a file name
     * The actual format used is: YYYY-MM-dd-HHmm-ssSS
     * @param date
     * @return
     */
    public static String slugifyDate(ZonedDateTime date) {
        return date.format(SLUG_FORMAT);
    }

    /**
     * Gets the current time in the local time zone. The local time zone is first determined by the current user, if no
     * user, then use the zone defined in settings, if no settings, use the zone of the server
     * @return
     */
    public static ZonedDateTime localNow() {
        return ZonedDateTime.now(Context.getSettings().getTimeZoneId());
    }


    public static ZoneId currentUserTimeZoneId() {
        if (!empty(Context.getUser().getTimeZoneId())) {
            return ZoneId.of(Context.getUser().getTimeZoneId());
        } else if (!emptyInstance(Settings.instance().getTimeZoneId())) {
            return Settings.instance().getTimeZoneId();
        } else {
            return ZoneId.of("UTC");
        }
    }

    public static int currentUserUtcOffsetMinutes() {
        ZonedDateTime now = ZonedDateTime.now(currentUserTimeZoneId());
        return now.getOffset().getTotalSeconds() / 60;
    }

    /**
     * Format the current time using a date format string
     * @param format
     * @return
     */
    public static String formatNow(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return utcNow().format(formatter);
    }

    /* We have to add this generic Object overload, because Method typing dispatching
    * does not work correctly when called from the templates. So instead we have to
    * include the type in the name of each function. Blech.
    * */
    public static String formatLocalDate(Object dt, String formatPattern) {
        if (dt instanceof Long) {
            return formatLocalDateFromLong((Long) dt, formatPattern);
        } else if (dt instanceof ZonedDateTime) {
            return formatLocalDateFromZonedDate((ZonedDateTime) dt, formatPattern);
        } else if (dt instanceof Date) {
            return formatLocalDateFromJDate((Date) dt, formatPattern);
        } else if (dt instanceof String) {
            return formatLocalDateFromString((String)dt, formatPattern);
        }
        return "";
    }

    public static String formatLocalDate(Object dt) {
        return formatLocalDate(dt, null);
    }


    public static String formatLocalDateFromJDate(Date date) {
        return formatLocalDateFromJDate(date, null);
    }

    public static String formatLocalDateFromJDate(Date date, String formatPattern) {
        if (date == null) {
            return "";
        }
        return formatLocalDateFromZonedDate(ZonedDateTime.ofInstant(date.toInstant(), UTC), formatPattern);
    }

    public static String formatLocalDateFromString(String dateStamp, String formatPattern) {
        LocalDateTime ldt = null;
        if (dateStamp.length() == 19) {
             ldt = LocalDateTime.parse(dateStamp, SQL_FORMAT);
        } else if (dateStamp.length() == 20) {
            ldt = LocalDateTime.parse(dateStamp, SLUG_FORMAT);
        }
        if (ldt == null) {
            throw new UsageException("DateStamp " + dateStamp + " did not have a recognizable format.");
        }
        return formatLocalDateFromZonedDate(ldt.atZone(UTC), formatPattern);
    }

    public static String formatLocalDateFromLong(long epochMillis) {
        return formatLocalDateFromLong(epochMillis, null);
    }

    public static String formatLocalDateFromLong(long epochMillis, String formatPattern) {
        if (epochMillis == 0L) {
            return "";
        }
        return formatLocalDateFromZonedDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), UTC), formatPattern);
    }

    public static String formatLocalDateFromLong(Long epochMillis) {
        return formatLocalDateFromLong(epochMillis, null);
    }

    public static String formatLocalDateFromLong(Long epochMillis, String formatPattern) {
        if (epochMillis == 0L || epochMillis == null) {
            return "";
        }
        return formatLocalDateFromZonedDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), UTC), formatPattern);
    }

    public static String formatLocalDateFromZonedDate(ZonedDateTime date) {
        return formatLocalDateFromZonedDate(date, null);
    }

}
