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

/**
 * A Persister actually handles interaction with the data store. There are
 * persister implementations for storing to a database, JSON files, text files,
 * etc.
 *
 * @param <T>
 */
public interface Persister<T extends Model> {

    /**
     * Initialize the persister, called during DalRegistery.register
     * @param registration
     * @param controller
     * @param stash
     */
    public void init(DataAccessRegistration registration, ModelController<T> controller, Stash<T> stash);

    /**
     * The Class of the model associated with this persister
     * @return
     */
    public Class<T> getModelClass();
    public Persister<T> setModelClass(Class<T> modelClass);

    /**
     * The bucket name associated with this persister.
     * @return
     */
    public String getBucket();
    public Persister<T> setBucket(String bucket);

    /**
     * Fetch all the items from the underlying data store.
     * @return
     */
    public List fetchAll();

    /**
     * Fetch one object with the given ID
     * @param id
     * @return
     */
    public T fetchOne(Long id);

    /**
     * Re-fetch the passed in object from the underlying data stores
     * @param obj
     * @return
     */
    public T fetchOne(T obj);

    /**
     * Get the controller associated with this persister
     * @return
     */
    public ModelController<T> getItemController();
    public Persister setItemController(ModelController<T> controller);

    /**
     * Get the Stash associated with this persister
     *
     * @return
     */
    public Stash<T> getStash();
    public Persister<T> setStash(Stash<T> stash);

    /**
     * Callback for file-based datastores when an object is changed in the file system
     *
     * @param relativePath
     */
    void watchEventCallback(String relativePath);

    /**
     * Persist the object to the underlying data store
     * @param obj
     */
    public void persist(T obj);

    /**
     * Permanently delete the object from the datastore
     * @param obj
     */
    public void hardDelete(T obj);

    /**
     * Attach any file system watchers for changed objects
     */
    public void attachWatcher();

    /**
     * Called by fetchOne() after the object has been loaded from the datastore
     *
     * @param obj
     */
    public void handleFetchOne(T obj);

    /**
     * Called after an object has been fetched from the data store
     */
    public void onFetchOne();

    /**
     * Called before any read operation.
     */
    public void onPreRead();

    /**
     * Reload the object if the version in the datastore is more recent than the
     * local object
     * @param obj
     * @return
     */
    public boolean reloadIfNewer(T obj);



    public FilterChain<T> filterChain();

    public boolean isDbBacked();

}
