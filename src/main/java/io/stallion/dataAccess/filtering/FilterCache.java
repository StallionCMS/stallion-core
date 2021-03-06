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

package io.stallion.dataAccess.filtering;

import io.stallion.services.Log;
import io.stallion.settings.Settings;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PersistenceConfiguration;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A cache used by filters to cache the results of filtering queries,
 * default TTL is 25 seconds, the cache is expired every time there is
 * any sort of update to an item in the bucket.
 *
 */
public class FilterCache {

    private static CacheManager manager;
    private static TimerTask evictThread;
    private static Timer evictThreadTimer;


    public static void start() {
        if (manager == null) {
            load();
        }

        // Every 5 minutes, evict all expired elements from the cache so they do not build up over time
        evictThread = new TimerTask() {
            public void run() {
                Thread.currentThread().setName("stallion-ehcache-evict-task");
                if (manager == null) {
                    return;
                }
                for(String name: manager.getCacheNames()) {
                    manager.getCache(name).evictExpiredElements();
                }
            }
        };

        evictThreadTimer = new Timer("stallion-ehcache-evict-timer");
        evictThreadTimer.scheduleAtFixedRate(evictThread, 0, 5 * 60 * 1000);
    }

    public static void shutdown() {
        if (evictThreadTimer != null) {
            evictThreadTimer.cancel();
            evictThreadTimer = null;
        }
        if (evictThread != null) {
            evictThread.cancel();
            evictThread = null;
        }
        if (manager != null) {
            manager.shutdown();
            manager = null;
        }
    }


    public static void load() {
        Log.finest("Load cache manager.");
        Configuration config = new Configuration();
        config.setName("stallionFilterCache");
        CacheManager.create(config);
        manager = CacheManager.create();
        Log.finest("Cache manager created.");
    }

    public static void initCache(String bucket) {
        Log.finer("Init cache for {0}", bucket);
        if (manager == null) {
            load();
        }
        if (manager.getCache(bucket) != null) {
            return;
        }
        // We have to do this way because there is no way to programmmatically configured the
        // sizeOfEngine
        System.setProperty("net.sf.ehcache.sizeofengine.stallionFilterCache." + bucket, "io.stallion.dataAccess.filtering.EstimatedSizeOfEngine");

        CacheConfiguration config = new CacheConfiguration();
        config.setName(bucket);
        // Max cache size is very approximately, 50MB

        config.setMaxBytesLocalHeap(Settings.instance().getFilterCacheSize());
        //config.setDiskPersistent(false);
        //config.setMaxElementsOnDisk(0);
        //config.setMaxBytesLocalDisk(0L);
        config.setOverflowToOffHeap(false);
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        persistenceConfiguration.strategy(PersistenceConfiguration.Strategy.NONE);

        config.persistence(persistenceConfiguration);
        //SizeOfPolicyConfiguration sizeOfPolicyConfiguration = new SizeOfPolicyConfiguration();
        //sizeOfPolicyConfiguration.
        //config.addSizeOfPolicy();
        //config.set
        Log.finest("Construct new cache for " + bucket);
        Cache cache = new Cache(config);

        manager.addCache(cache);

        //Cache cache = manager.getCache(bucket);
        //CacheConfiguration config = cache.getCacheConfiguration();


        //config.setMaxBytesLocalHeap(150000000L);
        Log.finer("Finish cache for {0}", bucket);
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
