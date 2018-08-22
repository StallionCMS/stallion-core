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

import java.util.List;

import static io.stallion.utils.Literals.list;

/**
 * A persister that does absolutely nothing. Used for testing.
 * @param <T>
 */
public class DummyPersister<T extends Model> extends BasePersister<T> {


    @Override
    public List fetchAll() {
        return list();
    }

    @Override
    public T fetchOne(Long id) {
        return null;
    }

    @Override
    public T fetchOne(T obj) {
        return null;
    }


    @Override
    public void watchEventCallback(String relativePath) {

    }

    @Override
    public void persist(Model obj) {

    }

    @Override
    public void hardDelete(Model obj) {

    }

    @Override
    public void attachWatcher() {

    }

    @Override
    public void onPreRead() {

    }

    @Override
    public FilterChain<T> filterChain() {
        return null;
    }
}
