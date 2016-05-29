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

package io.stallion.dataAccess;

import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.dataAccess.filtering.FilterOperator;
import io.stallion.exceptions.UsageException;

import java.util.List;
import java.util.Set;

/**
 * A wrapper around a ModelController that only gives access to read methods.
 * Used for sandboxed contexts where the plugin or template might only have
 * read access, not write access.
 *
 * @param <T>
 */
public class ReadOnlyWrapper<T extends Model> implements ModelController<T> {

    private ModelController<T> original;

    public ReadOnlyWrapper(ModelController<T> original) {
        this.original = original;
    }


    @Override
    public void init(DataAccessRegistration registration, Persister<T> persister, Stash<T> stash) {

    }

    @Override
    public String getBucket() {
        return original.getBucket();
    }


    @Override
    public T detach(T obj) {
        throw new UsageException("This controller is wrapped to be read-only.");

    }



    @Override
    public void save(T obj) {
        throw new UsageException("This controller is wrapped to be read-only.");
    }


    @Override
    public void softDelete(T obj) {
        throw new UsageException("This controller is wrapped to be read-only.");
    }

    @Override
    public void hardDelete(T obj) {
        throw new UsageException("This controller is wrapped to be read-only.");
    }



    @Override
    public void onPreRead() {
        throw new UsageException("This controller is wrapped to be read-only.");
    }


    @Override
    public void onPreSavePrepare(T obj) {

    }

    @Override
    public void onPreSaveValidate(T obj) {

    }

    @Override
    public void onPreCreatePrepare(T obj) {

    }

    @Override
    public void onPreCreateValidate(T obj) {

    }

    @Override
    public void onPostSave(T obj) {

    }

    @Override
    public void onPostCreate(T obj) {

    }

    @Override
    public void onPostLoadItem(T obj) {

    }

    @Override
    public FilterChain<T> filterChain() {
        return original.filterChain();
    }

    @Override
    public FilterChain<T> filter(String name, Object value) {
        return original.filter(name, value);
    }

    @Override
    public FilterChain<T> filter(String name, Object value, String op) {
        return original.filter(name, value, op);
    }

    @Override
    public FilterChain<T> filterBy(String name, Object value, FilterOperator op) {
        return original.filterBy(name, value, op);
    }

    @Override
    public FilterChain<T> filterByKey(String keyName, Object value) {
        return original.filterByKey(keyName, value);
    }


    @Override
    public T forIdWithDeleted(Long id) {
        return original.forIdWithDeleted(id);
    }

    @Override
    public T originalForId(T id) {
        return null;
    }

    @Override
    public T forId(Long id) {
        return original.forId(id);
    }

    @Override
    public T originalForId(Long id) {
        return original.originalForId(id);
    }

    @Override
    public T forUniqueKey(String keyName, Object value) {
        return original.forUniqueKey(keyName, value);
    }


    @Override
    public List<T> listForKey(String keyName, Object value) {
        return original.listForKey(keyName, value);
    }

    @Override
    public int countForKey(String keyName, Object value) {
        return original.countForKey(keyName, value);
    }

    @Override
    public Set<String> getKeyFields() {
        return original.getKeyFields();
    }


    @Override
    public Set<String> getUniqueFields() {
        return original.getUniqueFields();
    }


    @Override
    public List<T> all() {
        return original.all();
    }

    @Override
    public Persister<T> getPersister() {
        throw new UsageException("This controller is wrapped to be read-only.");
    }

    @Override
    public Stash<T> getStash() {
        throw new UsageException("This controller is wrapped to be read-only.");
    }

    @Override
    public Class<T> getModelClass() {
        return original.getModelClass();
    }

    @Override
    public Boolean isWritable() {
        return false;
    }

    @Override
    public void reset() {
        throw new UsageException("This controller is wrapped to be read-only.");
    }

    @Override
    public ReadOnlyWrapper<T> getReadonlyWrapper() {
        return this;
    }
}
