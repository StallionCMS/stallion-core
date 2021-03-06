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
import io.stallion.reflection.PropertyUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Stash sits between the ModelController and the Persister. ModelController
 * is what is used externally to access the data. The Persister controls the
 * interaction between Stallion and the actual data store, whether that be your
 * file system or database. The Stash can either be a complete pass-through and
 * do nothing at all, or it can actually sync all the objects from the datastore
 * into local memory (using LocalMemoryStash).
 *
 *
 * @param <T>
 */
public abstract class Stash<T extends Model> {
    private ModelController<T> controller;
    private String bucket;
    private Persister<T> persister;
    private Class<? extends T> modelClass;
    private Set<String> keyFields = new HashSet<>();
    private Set<String> uniqueFields = new HashSet<>();

    /**
     * Initialized the Stash with required attributes, this is
     * called via dataAccess registration.
     *
     * @param registration
     * @param controller
     * @param persister
     */
    public void init(DataAccessRegistration registration, ModelController<T> controller, Persister<T> persister) {
        this.controller = controller;
        this.bucket = registration.getBucket();
        this.persister = persister;
        this.modelClass = (Class<? extends T>)registration.getModelClass();
    }

    /**
     * Get the associated controller
     * @return
     */
    public ModelController<T> getController() {
        return controller;
    }

    /**
     * Get the associated persister
     * @return
     */
    public Persister<T> getPersister() {
        return persister;
    }

    /**
     * Get the bucket name
     * @return
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Get the fields that have been defined as alternative lookup keys. A LocalMemoryStash
     * will create lookup tables for all these fields, for fast access.
     *
     * @return
     */
    public Set<String> getKeyFields() {
        return keyFields;
    }


    public Stash<T> setKeyFields(Set<String> keyFields) {
        this.keyFields = keyFields;
        return this;
    }

    /**
     * Get the field names that have been defined as unique keys. A LocalMemoryStash
     * will create lookup tables for all these fields, for fast access.
     * @return
     */
    public Set<String> getUniqueFields() {
        return uniqueFields;
    }


    public Stash<T> setUniqueFields(Set<String> uniqueFields) {
        this.uniqueFields = uniqueFields;
        return this;
    }

    /**
     * Take the passed in object, find the object in the stash with the same id, and
     * sync the data of the passed in object to the stashed object.
     *
     * This is used when saving a detached object -- first you have to sync the changes
     * to the stashed version of the object, then persist it.
     *
     * By default, sync will not sync builtin or internal fields
     *
     * @param obj
     */
    public abstract void sync(T obj);


    public void syncForSave(T obj) {
        sync(obj);
    }

    /**
     * Return a cloned version of the object.
     *
     * @param obj
     * @return
     */
    public abstract T detach(T obj);

    public T forceDetach(T obj) {

        T newItem = null;
        try {
            newItem = (T)obj.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        newItem.setId(obj.getId());
        cloneInto(obj, newItem, null, true, null);
        return newItem;
    }


    /**
     * Clones all non-null values from "source" into "dest"
     * @param source
     * @param dest
     * @param properties
     * @param copyNulls
     * @param changedKeyFields
     */
    public boolean cloneInto(Object source, Object dest, Iterable<String> properties, Boolean copyNulls, List<String> changedKeyFields) {
        if (changedKeyFields == null) {
            changedKeyFields = new ArrayList<String>();
        }
        boolean hasChanges = false;
        if (properties == null){
            //properties = PropertyUtils.describe(dest).keySet();
            properties = PropertyUtils.getProperties(dest).keySet();
        }
        for(String name: properties) {
            if (name.equals("id")) {
                continue;
            }
            if (name.equals("class")) {
                continue;
            }
            if (name.equals("controller")) {
                continue;
            }
            Object o = PropertyUtils.getProperty(source, name);
            Object previous = PropertyUtils.getProperty(dest, name);
            if (o != null || copyNulls) {
                if (previous == o || o != null && o.equals(previous)) {
                    continue;
                }
                if (getKeyFields() != null && this.getKeyFields().contains(name) && previous != null && !previous.equals(o)) {
                    changedKeyFields.add(name);
                }
                hasChanges = true;
                PropertyUtils.setProperty(dest, name, o);
            }
        }
        return hasChanges;
    }

    /**
     * Save the object to the stash and the underlying data store
     * @param obj
     */
    public abstract void save(T obj);

    /**
     * Remove the object from the stash and the underlying data store
     * @param obj
     */
    public abstract void hardDelete(T obj);

    /**
     * Load all items from the datastore into the stash.
     */
    public abstract void loadAll();

    /**
     * Re-load the given object from the datastore into the stash
     * @param obj
     */
    public abstract boolean loadItem(T obj);

    /**
     * Load or reload the item with the given id from the datastore into the stash
     * @param id
     */
    public abstract void loadForId(Long id);

    /**
     * All the items in the stash.
     *
     * @return
     */
    public abstract List<T> getItems();

    /**
     * Reset the cache, nulling out all existing fields, and then reloading everything from
     * the database.
     */
    public abstract void reset();

    /**
     * Called before any of the data access methods run
     */
    public abstract void onPreRead();

    /**
     * Reload from the underlying datastore if the stashed version is older
     * than the version in the datastore.
     *
     * @param obj
     * @return
     */
    public T reloadIfNewer(T obj) {
        return obj;
    }

    /**
     * Get a cloned/detached object by id
     *
     * @param id
     * @return
     */
    public abstract T forId(Long id);

    /**
     * Get the original object by id, if modified, the object in the stash
     * will be modified, so be careful.
     *
     * @param id
     * @return
     */
    public abstract T originalForId(Long id);

    /**
     * Retrieve an object via unique key.
     *
     * @param keyName
     * @param value
     * @return
     */
    public abstract T forUniqueKey(String keyName, Object value);

    /**
     * Retrieve all objects for a given alternative key
     *
     * @param keyName
     * @param value
     * @return
     */
    public abstract List<T> listForKey(String keyName, Object value);

    /**
     * Count the number of objects associated with an alternative key
     *
     * @param keyName
     * @param value
     * @return
     */
    public abstract int countForKey(String keyName, Object value);

    /**
     * Create a new FilterChain for this stash
     *
     * @return
     */
    public abstract FilterChain<T> filterChain();

    /**
     * Create a new filterChain, restricting it to items matching the lookupValue for the key
     *
     * @param key - a model field that has been defined as a key
     * @param lookupValue
     * @return
     */
    public abstract FilterChain<T> filterByKey(String key, Object lookupValue);

    /**
     * Create a new filter chain using a subset of items
     *
     * @param subset
     * @return
     */
    public abstract FilterChain<T> filterChain(List<T> subset);



}
