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

package io.stallion.dal.db;

import io.stallion.Context;
import io.stallion.dal.base.*;
import io.stallion.dal.filtering.FilterChain;
import io.stallion.dal.filtering.MySqlFilterChain;
import io.stallion.requests.JobRequest;
import io.stallion.requests.TaskRequest;
import io.stallion.requests.RequestProcessor;
import io.stallion.utils.DateUtils;

import javax.servlet.http.Cookie;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static io.stallion.utils.Literals.*;


public class DbPersister<T extends Model> extends BasePersister<T> {
    private long lastSyncAt = 0;
    private String tableName = "";

    @Override
    public void init(DalRegistration registration, ModelController<T> controller, Stash<T> stash) {
        super.init(registration, controller, stash);
        this.tableName = or(registration.getTableName(), getBucket());
    }



    @Override
    public List<T> fetchAll() {
        lastSyncAt = DateUtils.mils();
        List<T> things = DB.instance().fetchAll(getModelClass());
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

    @Override
    public void onPreRead() {
        // Check to see if we have read this bucket yet, for this request
        if (Context.getRequest() == null || Context.getRequest().getItems() == null) {
            return;
        }
        Boolean bucketSynced = (Boolean)Context.getRequest().getItems().get(getBucketSyncedKey());
        if (bucketSynced != null && bucketSynced) {
            return;
        }
        Context.getRequest().getItems().put(getBucketSyncedKey(), true);
        if (!checkNeedsSync()) {
            return;
        }
        syncFromDatabase();
    }


    @Override
    public FilterChain<T> filterChain() {
        return new MySqlFilterChain<T>(getTableName(), getBucket(), getModelClass());
    }

    protected void syncFromDatabase() {
        Long now = mils();

        ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastSyncAt), ZoneId.of("UTC"));
        //dt.format(StallionUtils.ISO_FORMAT);
        String formatedNow = dt.format(DateUtils.SQL_FORMAT);
        Class < T > cls = getModelClass();
        List<T> items = DB.instance().query(cls, "SELECT * FROM " + getTableName() + " WHERE row_updated_at >= ?", formatedNow);
        for (T item: items) {
            getStash().loadItem(item);
        }
        lastSyncAt = now;
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
        Cookie postBackCookie = Context.getRequest().getCookie(RequestProcessor.RECENT_POSTBACK_COOKIE);
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
