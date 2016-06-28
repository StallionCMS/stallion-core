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

package io.stallion.dataAccess.db;

import io.stallion.Context;
import io.stallion.requests.JobRequest;
import io.stallion.requests.TaskRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;

import javax.servlet.http.Cookie;

import static io.stallion.utils.Literals.*;

/**
 * SmartQueryCache is used by the DB singleton to cache query results.
 * Smart cache uses a bunch of heuristics to ensure that a user who has
 * just made an update never sees stale data.
*/
public class SmartQueryCache {
    private static CacheManager manager;


    public static void load() {

        manager = CacheManager.create();
    }

    public static void initCache(String bucket) {
        if (manager == null) {
            load();
        }
        if (manager.getCache(bucket) != null) {
            return;
        }
        CacheConfiguration config = new CacheConfiguration(bucket, 10000);
        //config.setDiskPersistent(false);
        //config.setMaxElementsOnDisk(0);
        //config.setMaxBytesLocalDisk(0L);
        config.setOverflowToOffHeap(false);
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        persistenceConfiguration.strategy(PersistenceConfiguration.Strategy.NONE);
        config.persistence(persistenceConfiguration);
        manager.addCache(new Cache(config));
    }

    public static void shutdown() {
        if (manager != null) {
            manager.shutdown();
        }
        manager = null;
    }

    /**
     * Under certain conditions, will skip the cache lookup and always return null, thus prompting a cache refresh
     * This is to prevent getting a stale object right after a user has made a POST request.
     * Otherwise, will hit the cache as normal.
     *
     *  By default:
     *
     * * Results are cached for 15 seconds
     * * caching is skipped when run via a task or a job
     * * caching is skipped for non-GET requests
     * * caching is only skipped once per request
     * * caching is skipped if the user made a post request in the last 15 seconds.
     *
     * Thus altogether, if you are getting dozens of GET requests per second, due to a page
     * getting heavy traffic, then the cache will almost always be hit. But if a user
     * has just made some sort of update, they are guaranteed not to get stale data.
     *
     * @param bucket
     * @param key
     * @return
     */
    public static Object getSmart(String bucket, String key) {
        if (checkShouldSkip(bucket, key)) {
            return null;
        }
        return get(bucket, key);
    }


    public static boolean checkShouldSkip(String bucket, String key) {
        // We only skip the cache at most once per lookup, per request
        // So we set a request context item to mark that we've done this check.
        Boolean seen = (Boolean)Context.getRequest().getItems().get("checked-should-skip-for-key-" + bucket + "---" + key);
        if (seen != null && seen) {
            return false;
        }
        Context.getRequest().getItems().put("checked-should-skip-for-key-" + bucket + "---" + key, true);

        if (Context.getRequest() instanceof TaskRequest) {
            return true;
        }
        if (Context.getRequest() instanceof JobRequest) {
            return true;
        }
        if (!"GET".equals(Context.getRequest().getMethod())) {
            return true;
        }

        // The current user
        Cookie postBackCookie = Context.getRequest().getCookie("st-recent-postback");
        if (postBackCookie != null && !empty(postBackCookie.getValue())) {
            Long t = Long.parseLong(postBackCookie.getValue());
            if (t != null && t > (mils() - 15000)) {
                return true;
            }
        }
        return false;
    }

    public static Object get(String bucket, String key) {
        if (manager == null) {
            return null;
        }
        Element element = manager.getCache(bucket).get(key);
        if (element != null) {
            return element.getObjectValue();
        }
        return null;
    }

    public static void set(String bucket, String key, Object value) {
        if (manager == null) {
            load();
        }
        if (!manager.cacheExists(bucket)) {
            initCache(bucket);
        }
        Element element = new Element(key, value);
        element.setTimeToLive(15);
        manager.getCache(bucket).put(element);
    }

    public static void clearBucket(String bucket) {
        if (manager.cacheExists(bucket) && manager.getCache(bucket).getSize() > 0) {
            manager.getCache(bucket).removeAll();
        }
    }
}
