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

package io.stallion.dal.base;

import io.stallion.dal.filtering.FilterChain;
import io.stallion.dal.filtering.FilterOperator;
import io.stallion.dal.filtering.Or;
import io.stallion.exceptions.NotFoundException;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface ModelController<T extends Model> {

    /**
     * Initialize the controller, loading all the key fields, setting defaults,
     * initializing key variables, etc. This must be callsed before the ModelController
     * can be used.
     *
     * @param registration
     * @param persister
     * @param stash
     */
    public void init(DalRegistration registration, Persister<T> persister, Stash<T> stash);


    // Associated information and classes

    /**
     * A name by which this controller gets accessed when it is accessed via a template,
     * or via DalRegistry.instance().get("bucketName"). Usually this is the table name or the
     * the file-system folder name (for file backed sites).
     *
     * @return
     */
    public String getBucket();

    /**
     * The instance of the Perister class
     * @return
     */
    public Persister<T> getPersister();

    /**
     * The instance of the Stash class.
     * @return
     */
    public Stash<T> getStash();

    /**
     * The Model class this controller manages.
     * @return
     */
    public Class<T> getModelClass();

    // Config and helpers

    /**
     * True if this controller can create or update objects, false if this
     * controller is read-only.
     *
     * @return
     */
    public Boolean isWritable();

    /**
     * Gets a controller implementation that wraps this controller, while stubbing out
     * all the write methods. This is used when passing the controller to a sandboxed plugin
     * that may only have read-only access.
     *
     * @return
     */
    public ReadOnlyWrapper<T> getReadonlyWrapper();

    // Crud

    /***
     * Gets a cloned version of an object, so that you can work with the object with out affecting
     * the live, in memory version
     * @param obj
     * @return
     */
    T detach(T obj);


    /**
     * Merge the given fields from the newInfo object into the target object.
     *
     * @param target
     * @param newInfo
     * @param fields
     * @return
     */
    public T mergeUpdates(T target, T newInfo, String...fields);

    public default T mergeUpdates(Long id, T toMergeIn, String fields) {
        return mergeUpdates(forIdOrNotFound(id), toMergeIn, fields);
    }

    /**
     * Accepts a detached object "toMergeIn", usually which has been POSTed from a REST API
     * and then finds the existing in-memory object and creates a detached clone
     * It then merges all non-null values of toMergeIn into the detached clone.
     * If an annotation is provided, it only merges properties that have that annotation.
     * It returns the detached merged object.
     * @param id
     * @param toMergeIn
     * @param anno
     * @return
     */
    T mergeDetached(Long id, T toMergeIn, Class<Annotation> anno);

    /**
     * Call mergedDetached(Long, T, Class&lt;Annotation&gt;) with a null annotation.
     * @param id
     * @param obj
     * @return
     */
    T mergeDetached(Long id, T obj);

    /**
     * Merges the passed in object with a default instance, where all the null fields
     * in the passed in object get set to their default values.
     * @param toMergeIn
     * @return
     */
    T mergeWithDefaults(T toMergeIn);

    /**
     * Merges the passed in object with a default instance, where all the null fields
     * in the passed in object get set to their default values.
     *
     * If an annotation is present, then we only keep properties with that annotation, otherwise,
     * properties without that annotation set set to their default value.
     *
     * @param toMergeIn
     * @param anno
     * @return
     */
    T mergeWithDefaults(T toMergeIn, Class<Annotation> anno);

    /**
     * Saves "obj" to the persistence layer, creating it if it does not exist.
     * If obj is detached, it synces the fields to the in-memory object, and saves
     * the real copy
     * @param obj
     */
    public void save(T obj);

    /**
     * Calls obj.setDeleted(true) then saves the object.
     * @param obj
     */
    public void softDelete(T obj);

    /**
     * Actually removes the item from the underlying data store
     *
     * @param obj
     */
    public void hardDelete(T obj);


    // Hooks

    void onPreRead();

    /**
     * Override this to perform an action every time before the object is saved.
     *
     * @param obj
     */
    public void onPreSavePrepare(T obj);

    /**
     * Override this to validate the object before it is saved.
     *
     * @param obj
     */
    public void onPreSaveValidate(T obj);

    /**
     * Override this to prepare the object with any default values before it is
     * saved to the datastore for the first time.
     *
     * @param obj
     */
    public void onPreCreatePrepare(T obj);

    /**
     * Override this to prepare the validate the object before it is saved to the datastore
     * for the very first time.
     *
     * @param obj
     */
    public void onPreCreateValidate(T obj);

    /**
     * Override this to perform some action after the object is saved
     * @param obj
     */
    public void onPostSave(T obj);

    /**
     * Override this to perform some action after the object is created.
     * @param obj
     */
    public void onPostCreate(T obj);

    /**
     * Override this to perform some action after an item is loaded from the datastore
     * @param obj
     */
    public void onPostLoadItem(T obj);


    // Fetching

    /**
     * Return a list of all objects.
     *
     * @return
     */
    public List<T> all();

    /**
     * Create a new FilterChain instance for this controller.
     * @return
     */
    public FilterChain<T> filterChain();

    /**
     * Create a new FilterChain and set an initial filter whereby
     * the field @name is equal to @value
     *
     * @param name
     * @param value
     * @return
     */
    public FilterChain<T> filter(String name, Object value);

    /**
     *
     * Create a new FilterChain and initialize with an initial filter.
     *
     * @param name
     * @param value
     * @param op
     * @return
     */
    public FilterChain<T> filter(String name, Object value, String op);

    /**
     *
     * Create a new FilterChain and initialize with an initial filter.
     *
     * @param name
     * @param value
     * @param op
     * @return
     */
    public FilterChain<T> filterBy(String name, Object value, FilterOperator op);

    /**
     * Short-cut for applying filter(name, value) for every key-value pair in the dictionary.
     *
     * Use from javascript as so:
     *
     * controller.find({'author': 'Mark Twain', 'type': 'short-story'});
     *
     * @param where - a map of key value pairs to find matching objets of
     * @return
     */
    public default FilterChain<T> find(Map<String, Object> where) {
        FilterChain<T> chain = filterChain();
        for(Map.Entry<String, Object> entry: where.entrySet()) {
            chain = chain.filter(entry.getKey(), entry.getValue());
        }
        return chain;
    }

    /**
     * Short-cut for filterChain().andAnyOf(Or("someField", "someValue"), Or("someField", "someValue"));
     * Finds all items that match any of the criteria.
     */
    public default FilterChain<T> anyOf(Or...ors) {
        return filterChain().andAnyOf(ors);
    }

    /**
     * Short-cut for filterChain().andAnyOf(["someField", "value"], ["otherField", "anotherValue"]);
     * Finds all items that match any of the criteria
     */
    public default FilterChain<T> anyOf(List<String>...filters) {
        return filterChain().andAnyOf(filters);
    }


    /**
     * Instantiate a filter chain and start by filtering on an index/keyed field.
     *
     * @param keyName
     * @param value
     * @return
     */
    public FilterChain<T> filterByKey(String keyName, Object value);


    /**
     * Get the object by id. Will return objects that have been soft-deleted.
     *
     * @param id
     * @return
     */
    public T forIdWithDeleted(Long id);

    /**
     * Load an object by id, without detaching it. Changes to the object
     * will affect the object stashed in memory. Do not use this unless
     * you know what you are doing.
     *
     * @param id
     * @return
     */
    public T originalForId(T id);

    /**
     * Loads an object by primary id. Detaches/clones the returned object
     * so that changes will not affect the original object. Missing or
     * soft deleted objects will return as null.
     *
     * @param id
     * @return
     */
    T forId(Long id);

    /**
     * Calls forId() to load an object by id, raises a NotFoundException if it does not exist
     * @param id
     * @return
     */
    default public T forIdOrNotFound(Long id) {
        T o = forId(id);
        if (o == null) {
            throw new NotFoundException("The " + getBucket() + " item was not found.");
        }
        return o;
    }

    /**
     * Load an object by id, without detaching it. Changes to the object
     * will affect the object stashed in memory. Do not use this unless
     * you know what you are doing.
     *
     * @param id
     * @return
     */
    T originalForId(Long id);

    /**
     * Look up an object by a unique key. Only works if the @UniqueKey annotation
     * has been added to the getter of the property.
     *
     * @param keyName
     * @param value
     * @return
     */
    public T forUniqueKey(String keyName, Object value);

    /**
     * Finds the item by key and value, raises a NotFoundException if it does not exist
     * @param keyName
     * @param value
     * @return
     */
    default public T forUniqueKeyOrNotFound(String keyName, Object value) {
        T o = forUniqueKey(keyName, value);
        if (o == null) {
            throw new NotFoundException("The " + getBucket() + " item was not found.");
        }
        return o;
    }

    /**
     * Get all items for a indexed/keyed field. This only works if the @Key annotation
     * has been added to the getter of the property
     *
     * @param keyName
     * @param value
     * @return
     */
    List<T> listForKey(String keyName, Object value);

    /**
     * Counts the items for a indexed/keyed field. This only works if the @Key annotation
     * has been added to the getter of the property
     * @param keyName
     * @param value
     * @return
     */
    int countForKey(String keyName, Object value);

    // Keys

    /**
     * Get all model field names that were marked as indexed/keyed using the @Key
     * annotation on the getter
     * @return
     */
    public Set<String> getKeyFields();

    /**
     * Get all model field names that were marked as a unique key using the @UniqueKey
     * annotation on the getter.
     * @return
     */
    public Set<String> getUniqueFields();

    /** If the datastore has been synced to memory, reset() will resync everything.
     *
     */
    public void reset();


}
