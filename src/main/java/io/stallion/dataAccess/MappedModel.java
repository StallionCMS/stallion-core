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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stallion.reflection.PropertyUtils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

//import org.apache.commons.beanutils.PropertyUtils;


/**
 * A Model subclass that implements the Map interface and accepts arbitrary
 * key/value data. This is used by server-side Javascript plugins, which
 * cannot define standard Java pojos, and this is used to add custom data
 * to pages, posts, or other objects that are created via text files,
 * without having go into creating a Java project and writing out classes
 * that contain the fields.
 *
 */
public interface MappedModel extends Map<String, Object>  {
    //private Map<String, Object> attributes = new HashMap<String, Object>();

    public default int size() {
        return toCompleteMap().size();
    }

    @JsonIgnore
    public default boolean isEmpty() {
        return toCompleteMap().isEmpty();
    }


    public default boolean containsKey(String key) {
        return getAttributes().containsKey(key) || PropertyUtils.isReadable(this, key);
    }

    public default boolean containsKey(Object key) {
        return getAttributes().containsKey(key) || PropertyUtils.isReadable(this, key.toString());
    }


    public default boolean containsValue(Object value) {
        return getAttributes().containsValue(value);
    }


    public default Object get(Object key) {
        try {
            if (PropertyUtils.isReadable(this, key.toString())) {

                return PropertyUtils.getProperty(this, key.toString());
            }

            return getAttributes().get(key);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public default Object put(String key, Object value) {
        try {
            if (key.equals("id") || key.equals("lastModifiedMillis") && !(value instanceof Long)) {
                if (value instanceof Double) {
                    value = ((Double) value).longValue();
                } else if (value instanceof Integer) {
                    value = ((Integer) value).longValue();
                } else if (value instanceof Float) {
                    value = ((Float) value).longValue();
                } else if (value instanceof BigInteger) {
                    value = ((BigInteger) value).longValue();
                }

            }
            if (PropertyUtils.isWriteable(this, key)) {
                Class cls = null;
                if (value != null) {
                    cls = value.getClass();
                }
                PropertyUtils.setProperty(this, key, value);
                return null;
            } else {
                return getAttributes().put(key, value);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public default Object remove(Object obj) {
        return getAttributes().remove(obj);
    }


    public default void putAll(Map<? extends String, ? extends Object> m) {
        getAttributes().putAll(m);
    }


    public default void clear() {

    }

    public default Set<String> keySet() {
        return toCompleteMap().keySet();
    }


    public default Collection<Object> values() {
        return toCompleteMap().values();
    }


    public default Set<Map.Entry<String, Object>> entrySet() {
        return toCompleteMap().entrySet();
    }

    default Map<String, Object> toCompleteMap() {
        Map map = PropertyUtils.getProperties(this, JsonIgnore.class);
        for(Map.Entry<String, Object> entry: getAttributes().entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @JsonIgnore
    public Map<String, Object> getAttributes();

    public void setAttributes(Map<String, Object> attributes);

}
