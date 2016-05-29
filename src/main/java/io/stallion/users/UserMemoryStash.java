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

package io.stallion.users;

import io.stallion.dataAccess.LocalMemoryStash;
import io.stallion.dataAccess.filtering.FilterCache;
import io.stallion.services.Log;

import java.io.File;
import java.util.List;


public class UserMemoryStash<T extends IUser> extends LocalMemoryStash<T> {
    private PredefinedUsersPersister<T> predefinedUserPersister;



    @Override
    public void loadForId(Long id)  {
        T existing = forId(id);
        T item = null;
        if (isPredefinedUser(existing)) {
            item = (T)predefinedUserPersister.fetchOne(id);
        }
        if (item == null) {
            item = (T) getPersister().fetchOne(id);
        }
        loadItem(item);
        FilterCache.clearBucket(getBucket());
    }


    @Override
    public void save(T obj) {
        if (isPredefinedUser(obj)) {
            predefinedUserPersister.persist(obj);
            return;
        }
        super.save(obj);
    }


    public boolean isPredefinedUser(T user) {
        if (user == null) {
            return false;
        }
        return user.isPredefined();
    }


    @Override
    public void loadAll()  {
        Log.fine("Load all from {0}. ", getBucket());
        List items = this.getPersister().fetchAll();
        for(Object item: items) {
            T displayable = (T)item;
            loadItem(displayable);
        }
        loadPredefinedUsers();
    }


    public void loadItem(T item)  {
        //Log.fine("Pojo item: {0}:{1}", item.getClass().getName(), item.getId());

        if (item.getId() == null) {
            Log.warn("Loading a pojo item with a null ID! bucket: {0} class:{1}", getBucket(), item.getClass().getName());
        }

        T original = itemByPrimaryKey.getOrDefault(item.getId(), null);
        if (original != null) {
            sync(item);
        } else {
            registerItem(item);
        }
        getController().onPostLoadItem(item);
        registerKeys(item);
        item = this.itemByPrimaryKey.get(item.getId());

    }


    /**
     * Users are defined one of two ways:
     * - dynamically via the application, and stored either in the production database or the production app-date folder
     * - as a json file in target-path/users folder. These users are created at the command line and are version controlled
     *   and deployed with the app. These mainly should be for admins and super users, for small sites.
     */
    public void loadPredefinedUsers() {
        predefinedUserPersister = new PredefinedUsersPersister();
        predefinedUserPersister.init(getController());
        File folder = new File(predefinedUserPersister.getBucketFolderPath());
        if (folder.isDirectory()) {
            List items = predefinedUserPersister.fetchAll();
            for(Object item: items) {
                T displayable = (T)item;
                displayable.setApproved(true);
                loadItem(displayable);
            }
        } else {
            folder.mkdirs();
        }
        predefinedUserPersister.attachWatcher();
    }

}
