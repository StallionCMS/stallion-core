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
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.Schema;
import io.stallion.dataAccess.filtering.FilterCache;
import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static io.stallion.utils.Literals.*;


/**
 * PartialStash loads the most recently updated 50,000 items in memory, and also
 * keeps in memory every item directly loaded by id. This is useful for largish tables
 * with recent data accessed very frequently, but where you don't want to incur
 * a really long load-time when booting the server during deployment.
 *
 * @param <T>
 */
public class PartialStash<T extends Model> extends Stash<T> {

    protected Map<Long, T> itemByPrimaryKey;
    protected List<T> items;
    protected Set<String> uniqueFields;
    protected Map<String, Map<Object, T>> keyNameToUniqueKeyToValue;
    protected List<Col> columns;

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
        this.uniqueFields = set();
        this.itemByPrimaryKey = new HashMap<Long, T>();
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
        }
        if (!empty(columns)) {
            for (Col col: columns) {
                if (col.getUniqueKey()) {
                    this.keyNameToUniqueKeyToValue.put(col.getPropertyName(), new HashMap<Object, T>());
                }
            }
        }
        uniqueFields = this.keyNameToUniqueKeyToValue.keySet();
    }


    @Override
    public void sync(T obj) {
        // TODO: exclude properties with annotation @SyncExclude
        T existing = this.originalForId(obj.getId());
        if (existing != null) {
            List<String> changedKeyFields = new ArrayList<>();
            cloneInto(obj, existing, null, false, changedKeyFields);
        }
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



    @Override
    public T detach(T obj) {
        // Always detached, so can just return object
        return obj;
    }


    @Override
    public void save(T obj) {
        T existing = originalForId(obj.getId());
        if (existing != null) {
            sync(obj);
            getPersister().persist(existing);
        } else {
            getPersister().persist(obj);
        }
    }


    @Override
    public void hardDelete(T obj) {
        getPersister().hardDelete(obj);
    }

    @Override
    public void loadAll() {
        for(T obj: DB.instance().query(getPersister().getModelClass(), getInitialLoadSql())) {
            loadItem(obj);
        }
    }

    public String getInitialLoadSql() {
        Schema schema = DB.instance().getSchema(getPersister().getModelClass());
        String sql = "SELECT * FROM " + schema.getName() + " ORDER BY row_updated_at DESC LIMIT 50000";
        return sql;
    }






    public void loadForId(Long id)  {
        T item = (T) getPersister().fetchOne(id);
        loadItem(item);
        FilterCache.clearBucket(getBucket());
    }



    public boolean loadItem(T item)  {
        //Log.fine("Pojo item: {0}:{1}", item.getClass().getName(), item.getId());
        boolean hasChanges =  false;
        if (item.getId() == null) {
            Log.warn("Loading a pojo item with a null ID! bucket: {0} class:{1}", getBucket(), item.getClass().getName());
        }
        T original = itemByPrimaryKey.getOrDefault(item.getId(), null);
        if (original != null) {
            hasChanges = cloneInto(item, original, null, true, list());
            item = original;
        } else {
            registerItem(item);
            hasChanges = true;
        }
        getController().onPostLoadItem(item);
        registerKeys(item);
        return hasChanges;
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
        for (String uniqueKey : keyNameToUniqueKeyToValue.keySet()) {
            Object val = PropertyUtils.getPropertyOrMappedValue(item, uniqueKey);
            if (val != null) {
                this.keyNameToUniqueKeyToValue.get(uniqueKey).put(val, item);
            }
        }
    }




    @Override
    public List<T> getItems() {
        throw new UsageException("This PartialStash does not have all items in memory.");
    }


    @Override
    public void reset() {
        items = new ArrayList<>();
        itemByPrimaryKey = new HashMap<Long, T>();
        keyNameToUniqueKeyToValue = new Hashtable<String, Map<Object, T>>();
        initialize();
        loadAll();
        FilterCache.clearBucket(getBucket());
    }



    @Override
    public void onPreRead() {
        getPersister().onPreRead();
    }


    @Override
    public T forId(Long id) {
        T item = this.itemByPrimaryKey.get(id);
        if (item != null) {
            onPreRead();
            return detach(item);
        } else {
            item = filterChain().filter("id", id).first();
            loadItem(item);
            return item;
        }
    }

    @Override
    public T originalForId(Long id) {
        T item = this.itemByPrimaryKey.get(id);
        if (item != null) {
            onPreRead();
            return item;
        } else {
            return getPersister().fetchOne(id);
        }
    }

    @Override
    public T forUniqueKey(String keyName, Object lookupValue) {
        Map<Object, T> map = this.keyNameToUniqueKeyToValue.getOrDefault(keyName, null);
        if (map == null) {
            throw new ConfigException("There is no unique key '" + keyName + "' defined for bucket '" + getBucket() + "'");
        }
        T value = map.get(lookupValue);
        if (value != null) {
            onPreRead();
            return detach(value);
        } else {
            return filterChain().filter(keyName, value).first();
        }
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
