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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;

import java.util.Timer;
import java.util.TimerTask;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class LocalMemoryCache {
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

        manager = CacheManager.create();
    }

    public static void initCache(String bucket) {
        if (manager == null) {
            load();
        }
        if (manager.getCache(bucket) != null) {
            return;
        }
        synchronized(manager) {
            if (manager.getCache(bucket) != null) {
                return;
            }
            CacheConfiguration config = new CacheConfiguration();
            config.setName(bucket);
            // Max cache size is 100MB
            config.setMaxBytesLocalHeap(100 * 1000 * 1000L);
            //config.setDiskPersistent(false);
            //config.setMaxElementsOnDisk(0);
            //config.setMaxBytesLocalDisk(0L);
            config.setOverflowToOffHeap(false);
            PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
            persistenceConfiguration.strategy(PersistenceConfiguration.Strategy.NONE);
            config.persistence(persistenceConfiguration);
            manager.addCache(new Cache(config));
        }
    }

    public static Object get(String key) {
        return get("default", key);
    }

    public static Object get(String bucket, String key) {
        bucket = or(bucket, "default");
        if (manager == null) {
            return null;
        }
        if (!manager.cacheExists(bucket)) {
            return null;
        }
        Element element = manager.getCache(bucket).get(key);
        if (element != null) {
            return element.getObjectValue();
        }
        return null;
    }
    public static void set(String key, Object value, int timeoutSeconds) {
        set("default", key, value, timeoutSeconds);
    }
    public static void set(String bucket, String key, Object value) {
        set(bucket, key, value, 15);
    }
    public static void set(String bucket, String key, Object value, int timeoutSeconds) {
        bucket = or(bucket, "default");
        if (manager == null) {
            load();
        }
        if (!manager.cacheExists(bucket)) {
            initCache(bucket);
        }
        Element element = new Element(key, value);
        element.setTimeToLive(timeoutSeconds);
        manager.getCache(bucket).put(element);
    }

    public static void clearBucket(String bucket) {
        if (manager.cacheExists(bucket) && manager.getCache(bucket).getSize() > 0) {
            manager.getCache(bucket).removeAll();
        }
    }

}
