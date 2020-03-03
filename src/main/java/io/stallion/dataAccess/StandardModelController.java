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
import io.stallion.dataAccess.filtering.FilterOperator;
import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;
import io.stallion.settings.Settings;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.stallion.utils.Literals.or;
import static io.stallion.utils.Literals.set;

/**
 * The standard implementation of a ModelController. You should almost certainly
 * use this as the base for your own model controllers.
 *
 * @param <T>
 */
public class StandardModelController<T extends Model> implements ModelController<T> {
    private String bucket;
    private Stash<T> stash;
    private Persister persister;
    private Boolean writable;
    private ReadOnlyWrapper<T> readOnlyWrapper;
    private Class<T> modelClass;

    public StandardModelController() {
    }



    protected void preInitialize(DataAccessRegistration registration) {

    }

    protected void postInitialize(DataAccessRegistration registration) {

    }


    @Override
    public void init(DataAccessRegistration registration, Persister<T> persister, Stash<T> stash) {
        this.bucket = registration.getBucket();
        this.persister = persister;
        this.stash = stash;
        this.writable = registration.isWritable();
        this.modelClass = (Class<T>)registration.getModelClass();
        if (this instanceof DisplayableModelController) {
            DisplayableModelController c = (DisplayableModelController)this;
            c.setDefaultTemplate(or(registration.getTemplatePath(), Settings.instance().getPageTemplate()));
        }
    }

    @Override
    public String getBucket() {
        return this.bucket;
    }

    /**
     * Instantiates a new item instance from the model class.
     *
     * @return
     */
    public T newModel() {
        return newModel(null);
    }

    /**
     * Instantiates a new item instance, assigning the values passed in with the map
     * @param o
     * @return
     */
    public T newModel(Map o) {
        return newModel(o, new String[]{});
    }

    public T newModel(Map o, String ...fields) {
        return newModel(o, true, fields);
    }

    public T newModel(Map o, boolean skipUnwriteableFields) {
        return newModel(o, skipUnwriteableFields, null);
    }

    public T newModel(Map o, boolean skipUnwriteableFields, String ...fields) {
        Set fieldSet = null;
        if (fields != null && fields.length > 0) {
            fieldSet = set(fields);
        }

        try {
            T model = getModelClass().newInstance();
            if (o != null && o.size() > 0) {
                for(Object key: o.keySet()) {
                    if (fieldSet != null && !fieldSet.contains(key)) {
                        continue;
                    }
                    if (skipUnwriteableFields && !PropertyUtils.isWriteable(model, key.toString())) {
                        continue;
                    }
                    Object value = o.get(key);
                    PropertyUtils.setProperty(model, key.toString(), value);
                }
            }
            return model;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T detach(T obj) {
        return getStash().detach(obj);
    }



    @Override
    public void save(T obj)  {
        T existing = this.forIdWithDeleted(obj.getId());
        if (existing == null) {
            Log.finer("Existing object not found. {0} new={1} id={2}", getBucket(), obj.hashCode(), obj.getId());
            onPreCreatePrepare(obj);
            onPreSavePrepare(obj);
            onPreCreateValidate(obj);
            onPreSaveValidate(obj);
            getStash().save(obj);
            onPostCreate(obj);
        } else {
            Log.finer("Existing found. {0} existing={1} new={2} id={3}", getBucket(), existing.hashCode(), obj.hashCode(), obj.getId());
            onPreSavePrepare(obj);
            onPreSaveValidate(obj);
            getStash().save(obj);
            onPostSave(obj);
        }
    }

    @Override
    public void softDelete(T obj)  {
        obj.setDeleted(true);
        this.save(obj);
    }

    @Override
    public void hardDelete(T obj)  {
        getStash().hardDelete(obj);
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
    @Deprecated
    public void onPreRead() {

    }


    public void onPostLoadItem(T item) {

    }

    @Override
    public FilterChain<T> filterChain() {
        return getStash().filterChain();
    }

    @Override
    public FilterChain<T> filter(String name, Object value)  {
        return filterChain().filter(name, value);
    }

    @Override
    public FilterChain<T> filter(String name, Object value, String op)  {
        return filterChain().filter(name, value, op);
    }

    @Override
    public FilterChain<T> filterBy(String name, Object value, FilterOperator op)  {
        return filterChain().filterBy(name, value, op);
    }

    @Override
    public FilterChain<T> filterByKey(String keyName, Object value)  {
        return getStash().filterByKey(keyName, value);
    }


    @Override
    public T originalForId(T id) {
        return null;
    }


    @Override
    public T forId(Long id) {
        T o = getStash().forId(id);
        if (o == null) {
            return o;
        }
        if (o.getDeleted() != null && o.getDeleted() == true) {
            return null;
        }
        return o;
    }

    @Override
    public T forIdWithDeleted(Long id) {
        return getStash().forId(id);
    }

    @Override
    public T originalForId(Long id) {
        return getStash().originalForId(id);
    }

    @Override
    public T forUniqueKey(String keyName, Object keyValue)  {
        return getStash().forUniqueKey(keyName, keyValue);
    }

    @Override
    public List<T> listForKey(String keyName, Object value) {
        return getStash().listForKey(keyName, value);
    }

    @Override
    public int countForKey(String keyName, Object value)  {
        return getStash().countForKey(keyName, value);
    }


    @Override
    public List<T> all()  {
        return filterChain().all();
    }

    @Override
    public void reset() {
        getStash().reset();
    }

    public Persister getPersister() {
        return this.persister;
    }

    @Override
    public Stash<T> getStash() {
        return this.stash;
    }

    @Override
    public Class<T> getModelClass() {
        return this.modelClass;
    }

    public void setPersister(Persister persister) {
        this.persister = persister;
    }

    @Override
    public Set<String> getKeyFields() {
        return getStash().getKeyFields();
    }

    @Override
    public Set<String> getUniqueFields() {
        return getStash().getUniqueFields();
    }

    @Override
    public Boolean isWritable() {
        return writable;
    }


    public void setWritable(Boolean writable) {
        this.writable = writable;
    }

    public ReadOnlyWrapper<T> getReadonlyWrapper() {
        if (readOnlyWrapper == null) {
            readOnlyWrapper = new ReadOnlyWrapper<>(this);
        }
        return readOnlyWrapper;
    }
}
