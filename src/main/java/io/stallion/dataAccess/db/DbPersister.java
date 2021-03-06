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
import io.stallion.dataAccess.*;
import io.stallion.dataAccess.db.mysql.MySqlFilterChain;
import io.stallion.dataAccess.filtering.FilterCache;
import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.requests.IRequest;
import io.stallion.requests.JobRequest;
import io.stallion.requests.TaskRequest;
import io.stallion.utils.DateUtils;

import javax.ws.rs.core.Cookie;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static io.stallion.utils.Literals.*;


public class DbPersister<T extends Model> extends BasePersister<T> {
    private long lastSyncAt = ZonedDateTime.now(UTC).toInstant().toEpochMilli();
    private String tableName = "";
    private String sortField = "id";
    private String sortDirection = "ASC";

    @Override
    public void init(DataAccessRegistration registration, ModelController<T> controller, Stash<T> stash) {
        super.init(registration, controller, stash);
        this.tableName = or(registration.getTableName(), getBucket());
        DefaultSort defaultSort = getModelClass().getAnnotation(DefaultSort.class);
        if (defaultSort != null) {
            sortField = defaultSort.field();
            sortDirection = defaultSort.direction();
        }
    }



    @Override
    public List<T> fetchAll() {
        lastSyncAt = DateUtils.mils();
        List<T> things = DB.instance().fetchAllSorted(getModelClass(), sortField, sortDirection);
        for (T thing: things) {
            thing.setBucket(getBucket());
        }
        return things;
    }

    @Override
    public T fetchOne(Long id) {
        T o = DB.instance().fetchOne(getModelClass(), id);
        if (o != null) {
            handleFetchOne(o);
        }
        return o;
    }

    @Override
    public T fetchOne(T obj) {
        return fetchOne(obj.getId());
    }

    @Override
    public void watchEventCallback(String relativePath) {

    }

    @Override
    public void persist(T obj) {
        DB.instance().save(obj);
    }

    @Override
    public void update(T obj, Map<String, Object> values) {
        DB.instance().update(obj, values);
    }

    @Override
    public void hardDelete(T obj) {
        DB.instance().delete(obj);
    }

    @Override
    public void attachWatcher() {

    }

    @Override
    public boolean isDbBacked() {
        return true;
    }


    final ReentrantLock syncLock = new ReentrantLock();

    @Override
    public void onPreRead() {
        Long now = mils();
        // Check to see if we have read this bucket yet, for this request
        if (Context.getRequest() == null) {
            return;
        }
        Boolean bucketSynced = (Boolean)Context.getRequest().getProperty(getBucketSyncedKey());
        if (bucketSynced != null && bucketSynced) {
            return;
        }
        Context.getRequest().setProperty(getBucketSyncedKey(), true);
        if (!checkNeedsSync()) {
            return;
        }
        try {
            if (syncLock.tryLock(10, TimeUnit.SECONDS)) {
                // Was synced by other thread, skipping
                if (lastSyncAt >= now) {
                    return;
                }
                boolean hasChanges = syncFromDatabase();
                if (hasChanges) {
                    FilterCache.clearBucket(getBucket());
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (syncLock.isHeldByCurrentThread()) syncLock.unlock();
        }

    }


    @Override
    public FilterChain<T> filterChain() {
        return new MySqlFilterChain<T>(getTableName(), getBucket(), getModelClass());
    }

    protected boolean syncFromDatabase() {
        Long now = mils();

        ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastSyncAt), ZoneId.of("UTC"));
        //dt.format(StallionUtils.ISO_FORMAT);
        String formattedLastSyncAt = dt.format(DateUtils.SQL_FORMAT);
        Class < T > cls = getModelClass();
        List<T> items = DB.instance().query(cls, "SELECT * FROM " + getTableName() + " WHERE row_updated_at >= ?", formattedLastSyncAt);
        boolean hasChanges = false;
        for (T item: items) {
            boolean changed = getStash().loadItem(item);
            if (changed) {
                hasChanges = true;
            }
        }
        lastSyncAt = now;
        return hasChanges;
    }



    protected String getBucketSyncedKey() {
        return "bucket-synced:" + getBucket();
    }

    protected boolean checkNeedsSync() {
        if (Context.getRequest() instanceof TaskRequest) {
            return true;
        }
        if (Context.getRequest() instanceof JobRequest) {
            return true;
        }
        if (!"GET".equals(Context.getRequest().getMethod())) {
            return true;
        }
        // Hasn't been synced in more than 15 seconds
        if (lastSyncAt < (mils() - 15000)) {
            return true;
        }
        // The current user
        Cookie postBackCookie = Context.getRequest().getCookie(IRequest.RECENT_POSTBACK_COOKIE);
        if (postBackCookie != null && !empty(postBackCookie.getValue())) {
            Long t = Long.parseLong(postBackCookie.getValue());
            if (t != null && t > (mils() - 15000)) {
                return true;
            }
        }
        return false;
    }

    protected String getTableName() {
        return this.tableName;
    }


}
