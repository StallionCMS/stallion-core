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

package io.stallion.dataAccess.filtering;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;

/**
 * A cache used by filters to cache the results of filtering queries,
 * default TTL is 25 seconds, the cache is expired every time there is
 * any sort of update to an item in the bucket.
 *
 */
public class FilterCache {

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
        CacheConfiguration config = new CacheConfiguration(bucket, 25000);
        //config.setDiskPersistent(false);
        //config.setMaxElementsOnDisk(0);
        //config.setMaxBytesLocalDisk(0L);
        config.setOverflowToOffHeap(false);
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        persistenceConfiguration.strategy(PersistenceConfiguration.Strategy.NONE);
        config.persistence(persistenceConfiguration);
        manager.addCache(new Cache(config));

        //Cache cache = manager.getCache(bucket);
        //CacheConfiguration config = cache.getCacheConfiguration();


        //config.setMaxBytesLocalHeap(150000000L);
    }

    public static void shutdown() {
        if (manager != null) {
            manager.shutdown();
        }
        manager = null;
    }

    public static Object get(String bucket, String key) {
        if (manager == null) {
            return null;
        }
        Cache cache = manager.getCache(bucket);
        if (cache == null) {
            return null;
        }
        Element element = cache.get(key);
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
        element.setTimeToLive(60);
        manager.getCache(bucket).put(element);
    }

    public static void clearBucket(String bucket) {
        if (manager.cacheExists(bucket) && manager.getCache(bucket).getSize() > 0) {
            manager.getCache(bucket).removeAll();
        }
    }

}
