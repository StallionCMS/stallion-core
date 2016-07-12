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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.stallion.Context;
import io.stallion.dataAccess.Model;
import io.stallion.services.Log;
import io.stallion.users.Role;
import io.stallion.utils.json.JSON;
import io.stallion.utils.json.RestrictedViews;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.map;
import static io.stallion.utils.Literals.val;

public class GeneralUtils {

    public static final Object NULL = null;

    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("MMM d, YYYY h:mm a");
    public static final DateTimeFormatter SLUG_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd-HHmm-ssSS");
    public static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final DateTimeFormatter SQL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final ZoneId UTC = ZoneId.of("UTC");
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIHYPHENS = Pattern.compile("\\-\\-+");
    // Because probeContentType doesn't work on all platforms;
    private static final Map<String, String> mimeTypes = map(
            val("css", "text/css"),
            val("js", "text/javascript"),
            val("tag", "text/javascript"),
            val("woff", "application/font-woff"),
            val("otf", "application/octet-stream"),
            val("eot", "application/octet-stream"),
            val("ttf", "application/octet-stream"),
            val("map", "application/json"),
            val("json", "application/json")
    );

    /**
     * Converts the string into a string containing only hyphens, lower-case letters, and numbers, removing all
     * other characters.
     *
     * @param input
     * @return
     */
    public static String slugify(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = MULTIHYPHENS.matcher(NONLATIN.matcher(normalized).replaceAll("-")).replaceAll("-");
        return slug.toLowerCase(Locale.ENGLISH);
    }


    public static String guessMimeType(String path) {
        return mimeTypes.getOrDefault(FilenameUtils.getExtension(path), null);
    }

    public static String md5Hash(String val) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(val.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }


    // DEPRECATED methods


    @Deprecated
    public static ZonedDateTime utcNow() {
        return ZonedDateTime.now(UTC);
    }

    @Deprecated
    public static ZonedDateTime localNow() {
        return ZonedDateTime.now(Context.getSettings().getTimeZoneId());
    }

    /* Current milliseconds since the epoch */
    @Deprecated
    public static long mils() {
        return new Date().getTime();
    }

    /* Epoch milliseconds to a ZonedDateTime */
    @Deprecated
    public static ZonedDateTime milsToDateTime(long mils) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(mils), UTC);
    }

    @Deprecated
    public static String formatNow(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return utcNow().format(formatter);
    }



    /* We have to add this generic Object overload, because Method typing dispatching
    * does not work correctly when called from the templates. So instead we have to
    * include the type in the name of each function. Blech.
    * */
    @Deprecated
    public static String formatLocalDate(Object dt, String formatPattern) {
        if (dt instanceof Long) {
            return formatLocalDateFromLong((Long) dt, formatPattern);
        } else if (dt instanceof ZonedDateTime) {
            return formatLocalDateFromZonedDate((ZonedDateTime) dt, formatPattern);
        } else if (dt instanceof Date) {
            return formatLocalDateFromJDate((Date) dt, formatPattern);
        }
        return "";
    }

    @Deprecated
    public static String formatLocalDate(Object dt) {
        return formatLocalDate(dt, null);
    }

    @Deprecated
    public static String formatLocalDateFromJDate(Date date) {
        return formatLocalDateFromJDate(date, null);
    }

    @Deprecated
    public static String formatLocalDateFromJDate(Date date, String formatPattern) {
        if (date == null) {
            return "";
        }
        return formatLocalDateFromZonedDate(ZonedDateTime.ofInstant(date.toInstant(), UTC), formatPattern);
    }

    @Deprecated
    public static String formatLocalDateFromLong(long epochMillis) {
        return formatLocalDateFromLong(epochMillis, null);
    }

    @Deprecated
    public static String formatLocalDateFromLong(long epochMillis, String formatPattern) {
        if (epochMillis == 0L) {
            return "";
        }
        return formatLocalDateFromZonedDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), UTC), formatPattern);
    }

    @Deprecated
    public static String formatLocalDateFromLong(Long epochMillis) {
        return formatLocalDateFromLong(epochMillis, null);
    }

    @Deprecated
    public static String formatLocalDateFromLong(Long epochMillis, String formatPattern) {
        if (epochMillis == 0L || epochMillis == null) {
            return "";
        }
        return formatLocalDateFromZonedDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), UTC), formatPattern);
    }

    @Deprecated
    public static String formatLocalDateFromZonedDate(ZonedDateTime date) {
        return formatLocalDateFromZonedDate(date, null);
    }


    /**
     * Turn the given long id into a random base32 string token. This can be used for generating unique, secret strings
     * for accessing data, such as a web page only viewable by a secret string. By using the long id, of the
     * underlying object we guarantee uniqueness, by adding on  random characters, we make the URL nigh
     * impossible to guess.
     *
     * @param id - a long id that will be convered to base32 and used as the first part of the string
     * @param length - the number of random base32 characters to add to the end of the string
     * @return
     */
    public static String tokenForId(Long id, int length) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(id);
        return StringUtils.stripStart(new Base32().encodeAsString(buffer.array()), "A").replace("=", "").toLowerCase() + "8" + randomTokenBase32(length);
    }

    /**
     * Psuedo-random string of the given length, in base32 characters
     * @param length
     * @return
     */
    public static String randomTokenBase32(int length) {
        byte[] r = new byte[256]; //Means 2048 bit
        new Random().nextBytes(r);
        String s = new Base32().encodeAsString(r).substring(0, length).toLowerCase();
        return s;

    }

    /**
     * Generates a random string using the SecureRandom module, of the given length,
     * using URL safe base64 characters
     *
     * @param length
     * @return
     */
    public static String secureRandomToken(int length) {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[length * 4];
        random.nextBytes(bytes);
        String s = Base64.encodeBase64URLSafeString(bytes).substring(0, length);
        return s;
    }

    /**
     * Generates a random string using the psuedo-random module, of the given length,
     * using URL safe base64 characters
     *
     * @param length
     * @return
     */
    public static String randomToken(int length) {
        byte[] r = new byte[256]; //Means 2048 bit
        new Random().nextBytes(r);
        String s = Base64.encodeBase64URLSafeString(r).substring(0, length);
        return s;
    }

    @Deprecated
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

    @Deprecated
    public static String slugifyDate(Long epochMillis) {
        return slugifyDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), UTC));
    }

    @Deprecated
    public static String slugifyDate(ZonedDateTime date) {
        return date.format(SLUG_FORMAT);
    }

    public static Object htmlSafeJson(Object obj) {

        String out = JSON.stringify(obj);
        out = out.replace("<", "\\u003c");
        return out;
    }

    /**
     * Gets the object in a JSON form that is safe for being outputted on a web page:
     * &lt;script&gt;
     *     var myObj = {{ utils.htmlSafeJson(obj, "member") }}
     * &lt;script&gt;
     * @param obj
     * @param restrictionLevel - Uses the JsonView annotation to determine which properties of the object
     *                         should be outputed. Possible values are: unrestricted/public/member/owner/internal
     * @return
     */
    @Deprecated
    public static Object htmlSafeJson(Object obj, String restrictionLevel) {
        String out = "";
        try {
            restrictionLevel = restrictionLevel == null ? "public" : restrictionLevel.toLowerCase();
            if ("public".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Public.class, true);
            } else if ("unrestricted".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Unrestricted.class, false);
            } else if ("member".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Member.class, true);
            } else if ("owner".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Owner.class, true);
            } else if ("internal".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Internal.class, true);
            } else {
                out = "Unknown restriction level: " + restrictionLevel;
            }
        } catch (JsonProcessingException ex) {
            String objId = obj.toString();
            if (obj instanceof Model) {
                objId = ((Model)obj).getId().toString();
            }
            String msg = "Error JSON.stringifying object {0}" + obj.getClass().getSimpleName() + ":" + objId;
            if (Context.getSettings().getDebug() || Context.getUser().isInRole(Role.ADMIN)) {
                out = msg + "\n\nStacktrace-----\n\n" + ExceptionUtils.getStackTrace(ex);
            }
            Log.exception(ex, msg);
        }
        out = out.replace("<", "\\u003c");
        return out;
    }


}


