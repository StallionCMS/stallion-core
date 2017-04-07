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

import io.stallion.dataAccess.db.Col;
import io.stallion.dataAccess.filtering.FilterCache;
import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.exceptions.ConfigException;
import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static io.stallion.utils.Literals.*;

/**
 * {@inheritDoc}
 *
 * LocalMemoryStash syncs all items from the datastore into local memory.
 * It maintains the syncing throughout the life-time of the application.
 *
 * Internally, it stores all the items in a list called "items". It has
 * several different lookup tables for accessing the item by primary key,
 * by unique key, or by an non-unique key. If you have a lot of items,
 * it is essential to define keys and only find items by keys, otherwise
 * you will have to iterate through every single item every time you do a
 * filter or lookup.
 *
 * @param <T>
 */
public class LocalMemoryStash<T extends Model> extends StashBase<T> {

    protected Map<Long, T> itemByPrimaryKey;
    protected List<T> items;
    protected Set<String> keyFields;
    protected Set<String> uniqueFields;
    protected Map<String, Map<Object, Set<T>>> keyNameToKeyToValue;
    protected Map<String, Map<Object, T>> keyNameToUniqueKeyToValue;
    protected List<Col> columns;
    protected Set<String> uniqueFieldsCaseInsensitive = set();

    @Override
    public void init(DataAccessRegistration registration, ModelController<T> controller, Persister<T> persister) {
        super.init(registration, controller, persister);
        if (registration.getDynamicModelDefinition() != null) {
            columns = registration.getDynamicModelDefinition().getColumns();
        }
        initialize();
    }

    private void initialize() {
        if (getPersister() == null) {
            throw new ConfigException("A controller must have a persister before it is inited");
        }
        if (StringUtils.isEmpty(getBucket())) {
            throw new ConfigException("A controller must have a valid bucket before it is inited");
        }
        FilterCache.initCache(getBucket());
        this.items = new ArrayList<>();
        this.keyFields = set();
        this.uniqueFields = set();
        this.itemByPrimaryKey = new HashMap<Long, T>();
        this.keyNameToKeyToValue = new HashMap<String, Map<Object, Set<T>>>();
        if (this.getKeyFields() != null) {
            for (String keyFieldName : this.getKeyFields()) {
                HashMap<Object, Set<T>> keyToValues = new HashMap<>();
                this.keyNameToKeyToValue.put(keyFieldName, keyToValues);
            }
        }
        // Use a hashtable to enforce uniqueness
        this.keyNameToUniqueKeyToValue = new Hashtable<String, Map<Object, T>>();
        for(String key: getUniqueFields()) {
            this.keyNameToUniqueKeyToValue.put(key, new Hashtable<Object, T>());
        }

        // Get the unique keys and alternative keys from annotations
        for(String propertyName: PropertyUtils.getPropertyNames(this.getPersister().getModelClass())) {
            if (PropertyUtils.propertyHasAnnotation(this.getPersister().getModelClass(), propertyName, UniqueKey.class)) {
                Log.fine("Model:{0} has uniquekey on {1}", this.getPersister().getModelClass(), propertyName);
                this.keyNameToUniqueKeyToValue.put(propertyName, new HashMap<Object, T>());
            }
            if (PropertyUtils.propertyHasAnnotation(this.getPersister().getModelClass(), propertyName, AlternativeKey.class)) {
                Log.fine("Model:{0} has alternativeKey on {1}", this.getPersister().getModelClass(), propertyName);
                this.keyNameToKeyToValue.put(propertyName, new HashMap<Object, Set<T>>());
            }
        }
        if (!empty(columns)) {
            for (Col col: columns) {
                if (col.getUniqueKey()) {
                    if (true == col.getCaseInsensitive()) {
                        this.uniqueFieldsCaseInsensitive.add(col.getPropertyName());
                    }
                    this.keyNameToUniqueKeyToValue.put(col.getPropertyName(), new HashMap<Object, T>());
                } else if (col.getAlternativeKey()) {
                    this.keyNameToKeyToValue.put(col.getPropertyName(), new HashMap<Object, Set<T>>());
                }

            }
        }
        keyFields = this.keyNameToKeyToValue.keySet();
        uniqueFields = this.keyNameToUniqueKeyToValue.keySet();
    }

    @Override
    public void syncForSave(T obj) {
        T existing = this.originalForId(obj.getId());
        List<String> changedKeyFields = new ArrayList<>();
        cloneInto(obj, existing, null, true, changedKeyFields);
    }

    @Override
    public void sync(T obj) {
        // TODO: exclude properties with annotation @SyncExclude
        T existing = this.originalForId(obj.getId());
        List<String> changedKeyFields = new ArrayList<>();
        cloneInto(obj, existing, null, false, changedKeyFields);
    }

    @Override
    public T detach(T obj) {
        T existing = this.itemByPrimaryKey.get(obj.getId());
        if (existing == null) {
            return obj;
        }
        T newItem = null;
        try {
            newItem = (T)existing.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        newItem.setId(obj.getId());
        cloneInto(existing, newItem, null, true, null);
        return newItem;
    }



    @Override
    public void save(T obj) {
        T internal = this.itemByPrimaryKey.getOrDefault(obj.getId(), null);
        if (internal == null) {
            try {
                internal = (T)obj.getClass().newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            cloneInto(obj, internal, null, true, null);
            internal.setId(obj.getId());
            if (empty(obj.getId())) {
                internal.setId(DataAccessRegistry.instance().getTickets().nextId());
                internal.setIsNewInsert(true);
            }
            preRegisterItem(internal);
            getPersister().persist(internal);
            registerItem(internal);
            registerKeys(internal);
            obj.setId(internal.getId());
            obj.setIsNewInsert(false);
            cloneInto(internal, obj, null, true, null);
        } else {

            Map<String, Object> changedValues = map();
            // If these are not the same reference in memory, then we find the changed values,
            // and sync values from the detached object into the permanent object
            if (internal != null && obj != internal) {
                for (Map.Entry<String, Object> entry: PropertyUtils.getProperties(obj).entrySet()) {
                    Object org = PropertyUtils.getPropertyOrMappedValue(internal, entry.getKey());
                    if (org == null && entry.getValue() != null) {
                        changedValues.put(entry.getKey(), entry.getValue());
                    } else if (org != null && !org.equals(entry.getValue())) {
                        changedValues.put(entry.getKey(), entry.getValue());
                    }
                }
                this.syncForSave(obj);
                getPersister().update(obj, changedValues);
            } else {
                getPersister().persist(obj);
            }

        }
        FilterCache.clearBucket(getBucket());

    }


    @Override
    public void hardDelete(T obj)  {
        getPersister().hardDelete(obj);
        obj.setDeleted(true);
        if (itemByPrimaryKey.containsKey(obj.getId())) {
            sync(obj);
            itemByPrimaryKey.remove(obj.getId());
        }
        FilterCache.clearBucket(getBucket());
    }


    /**
     * Clones all non-null values from "source" into "dest"
     * @param source
     * @param dest
     * @param properties
     * @param copyNulls
     * @param changedKeyFields
     * @return true if there were changes
     */
    public boolean cloneInto(Object source, Object dest, Iterable<String> properties, Boolean copyNulls, List<String> changedKeyFields) {
        if (changedKeyFields == null) {
            changedKeyFields = new ArrayList<String>();
        }
        if (properties == null){
            //properties = PropertyUtils.describe(dest).keySet();
            properties = PropertyUtils.getProperties(dest).keySet();
        }
        boolean hasChanges = false;
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
                if (o == previous || o != null && o.equals(previous)) {
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

    public void loadForId(Long id)  {
        T item = (T) getPersister().fetchOne(id);
        loadItem(item);
        FilterCache.clearBucket(getBucket());
    }

    @Override
    public void loadAll()  {
        // In lightweight mode, we don't sync all models so as to boot quicker
        if (Settings.instance().getLightweightMode()) {
            return;
        }

        Log.fine("Load all from {0}. ", getBucket());
        List<T> items = this.getPersister().fetchAll();
        for(T item: items) {
            loadItem(item);
        }
    }


    public boolean loadItem(T item)  {
        //Log.fine("Pojo item: {0}:{1}", item.getClass().getName(), item.getId());
        boolean hasChanges = false;
        if (item.getId() == null) {
            Log.warn("Loading a pojo item with a null ID! bucket: {0} class:{1}", getBucket(), item.getClass().getName());
        }
        T original = itemByPrimaryKey.getOrDefault(item.getId(), null);
        if (original != null) {
            hasChanges = cloneInto(item, original, null, false, list());
            item = original;
        } else {
            hasChanges = true;
            registerItem(item);
        }
        getController().onPostLoadItem(item);
        registerKeys(item);
        return hasChanges;
    }

    public T reloadIfNewer(T obj) {
        boolean reloaded = getPersister().reloadIfNewer(obj);
        if (reloaded) {
            return forId(obj.getId());
        }
        return obj;
    }



    /**
     * This gets called before persist, to avoid a race-condition whereby:
     *
     * 1) Thread A saves to the database
     * 2) Thread B syncs from the database and loads the new object
     * 3) Thread A finishes the database save, and then adds the newly saved item to the registry
     * 4) Now we have two copies of the object in memory
     *
     *
     *
     * @param item
     */
    protected void preRegisterItem(T item) {
        itemByPrimaryKey.put(item.getId(), item);
    }

    protected void registerItem(T item) {
        itemByPrimaryKey.put(item.getId(), item);
        items.add(item);
    }

    public void registerKeys(T item) {
        if (getKeyFields() != null) {
            for (String keyField : keyNameToKeyToValue.keySet()) {
                Object obj = PropertyUtils.getPropertyOrMappedValue(item, keyField);
                if (obj != null) {
                    if (!this.keyNameToKeyToValue.get(keyField).containsKey(obj)) {
                        this.keyNameToKeyToValue.get(keyField).put(obj, set());
                    }
                    this.keyNameToKeyToValue.get(keyField).get(obj).add(item);
                }
            }
        }
        for (String uniqueKey : keyNameToUniqueKeyToValue.keySet()) {
            Object val = PropertyUtils.getPropertyOrMappedValue(item, uniqueKey);

            if (val != null) {
                if (this.uniqueFieldsCaseInsensitive.contains(uniqueKey) && val instanceof String) {
                    val = ((String) val).toLowerCase();
                }
                this.keyNameToUniqueKeyToValue.get(uniqueKey).put(val, item);
            }
        }
    }


    @Override
    public List<T> getItems() {
        return items;
    }

    @Override
    public void reset() {
        items = new ArrayList<>();
        itemByPrimaryKey = new HashMap<Long, T>();
        keyNameToKeyToValue = new HashMap<String, Map<Object, Set<T>>>();
        keyNameToUniqueKeyToValue = new Hashtable<String, Map<Object, T>>();
        if (getKeyFields() != null) {
            for (String keyFieldName : getKeyFields()) {
                HashMap<Object, Set<T>> keyToValues = new HashMap<>();
                keyNameToKeyToValue.put(keyFieldName, keyToValues);
            }
        }
        initialize();
        loadAll();
        FilterCache.clearBucket(getBucket());
    }

    @Override
    public T forId(Long id) {
        onPreRead();
        T item = this.itemByPrimaryKey.get(id);
        if (item == null) {
            return null;
        }
        return detach(item);
    }

    @Override
    public T originalForId(Long id) {
        onPreRead();
        T item = this.itemByPrimaryKey.get(id);
        if (item == null) {
            return null;
        }
        return item;
    }

    @Override
    public T forUniqueKey(String keyName, Object lookupValue) {
        onPreRead();
        Map<Object, T> map = this.keyNameToUniqueKeyToValue.getOrDefault(keyName, null);
        if (map == null) {
            throw new ConfigException("There is no unique key '" + keyName + "' defined for bucket '" + getBucket() + "'");
        }
        T value = map.get(lookupValue);
        if (value == null) {
            return value;
        }
        return detach(value);
    }

    @Override
    public List<T> listForKey(String keyName, Object value) {
        onPreRead();
        Set<T> things = this.keyNameToKeyToValue.get(keyName).getOrDefault(value, Collections.EMPTY_SET);
        return new ArrayList<T>(things);
    }

    @Override
    public int countForKey(String keyName, Object value) {
        return this.keyNameToKeyToValue.get(keyName).size();
    }

    @Override
    public FilterChain<T> filterChain() {
        return new FilterChain<T>(getBucket(), getItems(), this);
    }


    @Override
    public FilterChain<T> filterChain(List<T> subset) {
        return new FilterChain<T>(getBucket(), subset, this);
    }


    @Override
    public FilterChain<T> filterByKey(String keyName, Object value) {
        onPreRead();
        Map<Object, Set<T>> keyValMap = this.keyNameToKeyToValue.getOrDefault(keyName, null);
        if (keyValMap == null) {
            throw new ConfigException("The key " + keyName + " is not a valid key for the bucket " + getBucket() +
                    ". You must either override getKeyFields() in your controller, or pass in the key during the constructore");
        }
        Set<T> vals = keyValMap.getOrDefault(value, null);
        // If no values with this key, return an empty filter chain
        if (vals == null) {
            Log.finer("No items found for key field {0} key value {1}", keyName, value);
            return new FilterChain<>(getBucket(), this);
        }

        FilterChain chain = filterChain(new ArrayList<T>(vals)).filter(keyName, value);
        return chain;
    }

    @Override
    public void onPreRead() {
        getPersister().onPreRead();
    }

    @Override
    public Set<String> getKeyFields() {
        return keyFields;
    }


    @Override
    public Set<String> getUniqueFields() {
        return uniqueFields;
    }


}
