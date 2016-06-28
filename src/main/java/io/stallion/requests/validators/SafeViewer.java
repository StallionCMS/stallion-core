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

package io.stallion.requests.validators;

import io.stallion.dataAccess.Model;
import io.stallion.reflection.PropertyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;


public class SafeViewer<T extends Model> {

    private String[] fields = new String[]{};

    public SafeViewer(String...fields) {
        this.fields = fields;
    }

    public SafeViewer(List<String> fields) {
        this.fields = asArray(fields, String.class);
    }

    public Map<String, Object> wrap(T obj) {
        return objToMap(obj);
    }

    public List<Map<String, Object>> wrap(List<T> objects) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (T obj: objects) {
            items.add(objToMap(obj));
        }
        return items;
    }

    private Map<String, Object> objToMap(T obj) {
        Map<String, Object> map = new HashMap<>();
        for (String field: fields) {
            Object val = PropertyUtils.getPropertyOrMappedValue(obj, field);
            map.put(field, val);
        }
        return map;
    }


}
