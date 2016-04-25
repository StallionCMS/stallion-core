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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stallion.reflection.PropertyUtils;
//import org.apache.commons.beanutils.PropertyUtils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A Model subclass that implements the Map interface and accepts arbitrary
 * key/value data. This is used by server-side Javascript plugins, which
 * cannot define standard Java pojos, and this is used to add custom data
 * to pages, posts, or other objects that are created via text files,
 * without having go into creating a Java project and writing out classes
 * that contain the fields.
 *
 */
public class MappedModel extends ModelBase implements Map<String, Object>  {
    private Map<String, Object> attributes = new HashMap<String, Object>();

    public int size() {
        return toCompleteMap().size();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return toCompleteMap().isEmpty();
    }


    public boolean containsKey(String key) {
        return getAttributes().containsKey(key) || PropertyUtils.isReadable(this, key);
    }

    public boolean containsKey(Object key) {
        return getAttributes().containsKey(key) || PropertyUtils.isReadable(this, key.toString());
    }


    public boolean containsValue(Object value) {
        return getAttributes().containsValue(value);
    }


    public Object get(Object key) {
        try {
            if (PropertyUtils.isReadable(this, key.toString())) {

                return PropertyUtils.getProperty(this, key.toString());
            }

            return getAttributes().get(key);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object put(String key, Object value) {
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


    public Object remove(Object obj) {
        return getAttributes().remove(obj);
    }


    public void putAll(Map<? extends String, ? extends Object> m) {
        getAttributes().putAll(m);
    }


    public void clear() {

    }

    public Set<String> keySet() {
        return toCompleteMap().keySet();
    }


    public Collection<Object> values() {
        return toCompleteMap().values();
    }


    public Set<Map.Entry<String, Object>> entrySet() {
        return toCompleteMap().entrySet();
    }

    private Map<String, Object> toCompleteMap() {
        Map map = PropertyUtils.getProperties(this, JsonIgnore.class);
        for(Map.Entry<String, Object> entry: getAttributes().entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @JsonIgnore
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
