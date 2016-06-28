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

package io.stallion.services;

import io.stallion.Context;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;

import static io.stallion.utils.Literals.*;

/**
 * A service that stores values semi-permanently both in the local file system
 * and in memory.
 *
 * All values will live in memory as long as the process does, and in the file system as
 * long as the cache is not cleaned.
 *
 * This should only be used for bounded data, otherwise memory could explode.
 *
 *
 *
 */
public class PermaCache {
    private static final Map<String, String> cache = map();
    private static String cacheFolder;

    public static String get(String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        if (!empty(getCacheFolder())) {
            File file = new File(getCacheFolder() + "/" + DigestUtils.md5Hex(key));
            if (file.exists()) {
                try {
                    String content = FileUtils.readFileToString(file, "utf-8");
                    cache.put(key, content);
                    return content;
                } catch (IOException e) {
                    Log.exception(e, "Error reading file from disk: " + file.toString());
                }
            }
        }
        return null;
    }

    public static void set(String key, String contents) {
        cache.put(key, contents);
        if (!empty(getCacheFolder())) {
            File file = new File(getCacheFolder() + "/" + DigestUtils.md5Hex(key));
            try {
                FileUtils.write(file, contents, "utf-8");
            } catch (IOException e) {
                Log.exception(e, "Error writing file to cache: " + file.toString());
            }
        }
    }

    public static void setInMemoryOnly(String key, String contents) {
        cache.put(key, contents);
    }

    public static String getCacheFolder() {
        if (cacheFolder == null) {
            cacheFolder = initCacheFolder();
        }
        return cacheFolder;
    }

    public static String initCacheFolder() {
        File tmp = new File("/tmp/stallion-cache");
        if (tmp.isDirectory() && tmp.canWrite()) {
            return "/tmp/stallion-cache";
        }
        Boolean made = tmp.mkdirs();
        if (made) {
            return "/tmp/stallion-cache";
        }
        String appCachePath = Context.settings().getDataDirectory() + "/_cache";
        tmp = new File(appCachePath);
        if (tmp.isDirectory() && tmp.canWrite()) {
            return appCachePath;
        }
        made = tmp.mkdirs();
        if (made) {
            return appCachePath;
        }
        Log.warn("Could not create cache folder /tmp/stallion-cache or {0}!", appCachePath);
        return "";
    }

}
