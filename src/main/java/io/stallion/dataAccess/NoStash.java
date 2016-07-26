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

package io.stallion.dataAccess;

import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.exceptions.UsageException;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * {@inheritDoc}
 *
 * The NoStash class contains no local copy of the objects,
 * and instead looks up objects directly from the persister.
 *
 * Any filtering is passed through to the persister.
 *
 * @param <T>
 */
public class NoStash<T extends Model> extends StashBase<T> {


    @Override
    public void sync(T obj) {
        // Doesn't do anything, since no local copy
    }


    @Override
    public T detach(T obj) {
        // Always detached, so can just return object
        return obj;
    }

    @Override
    public void save(T obj) {
        getPersister().persist(obj);
    }


    @Override
    public void hardDelete(T obj) {
        getPersister().hardDelete(obj);
    }

    @Override
    public void loadAll() {
        // Don't load all for non synced
    }

    @Override
    public boolean loadItem(T obj) {
        return false;
    }

    @Override
    public void loadForId(Long id) {
        // No syncing, no loading
    }

    @Override
    public List<T> getItems() {
        throw new UsageException("This StashDummy does not have all items in memory.");
    }

    @Override
    public void reset() {

    }

    @Override
    public void onPreRead() {

    }

    @Override
    public T forId(Long id) {
        return filterChain().filter("id", id).first();
    }

    @Override
    public T originalForId(Long id) {
        return getPersister().fetchOne(id);
    }

    @Override
    public T forUniqueKey(String keyName, Object value) {
        return filterChain().filter(keyName, value).first();
    }


    @Override
    public List<T> listForKey(String keyName, Object value) {
        return filterChain().filter(keyName, value).all();
    }

    @Override
    public int countForKey(String keyName, Object value) {
        return filterChain().filter(keyName, value).count();
    }

    @Override
    public FilterChain<T> filterChain() {
        return getPersister().filterChain();
    }

    @Override
    public FilterChain<T> filterByKey(String key, Object lookupValue) {
        return filterChain().filter(key, lookupValue);
    }

    @Override
    public FilterChain<T> filterChain(List<T> subset) {
        return new FilterChain<T>(getBucket(), subset, null);
    }


}
