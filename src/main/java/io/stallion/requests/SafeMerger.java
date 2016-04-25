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

package io.stallion.requests;

import io.stallion.dal.base.Model;
import io.stallion.exceptions.ClientException;
import io.stallion.reflection.PropertyUtils;
import io.stallion.settings.Settings;

import java.util.Map;


public class SafeMerger<T extends Model> {
    private T newVersion;
    private Map<String, Object> map;
    private String[] requiredFields = new String[]{};
    private String[] optionalFields = new String[]{};
    private boolean errorOnExtra = false;

    public SafeMerger(T newVersion) {
        this.newVersion = newVersion;
        this.errorOnExtra = Settings.instance().isStrict();
    }

    public SafeMerger(Map<String, Object> map) {
        this.map = map;
        this.errorOnExtra = Settings.instance().isStrict();
    }

    public SafeMerger<T> withOptional(String ...optionalFields) {
        this.optionalFields = optionalFields;
        return this;
    }


    public SafeMerger<T> withRequired(String ...requiredFields) {
        this.requiredFields = requiredFields;
        return this;
    }

    public SafeMerger<T> errorOnExtra(boolean err) {
        this.errorOnExtra = err;
        return this;
    }

    public T mergeInto(T obj) {
        if (this.newVersion != null) {
            return mergeInfoFromObject(obj);
        } else {
            return mergeIntoFromMap(obj);
        }
    }

    public T mergeInfoFromObject(T obj) {
        for(String field: requiredFields) {
            Object val = PropertyUtils.getPropertyOrMappedValue(newVersion, field);
            if (val == null) {
                throw new ClientException("You must pass in a value for the field: " + field);
            }
            PropertyUtils.setProperty(obj, field, val);
        }
        for(String field: optionalFields) {
            Object val = PropertyUtils.getPropertyOrMappedValue(newVersion, field);
            if (val == null) {
                continue;
            }
            PropertyUtils.setProperty(obj, field, val);
        }
        if (errorOnExtra) {

        }
        return obj;
    }

    public T mergeIntoFromMap(T obj) {
        for(String field: requiredFields) {
            Object val = map.getOrDefault(field, null);
            if (val == null) {
                throw new ClientException("You must pass in a value for the field: " + field);
            }
            PropertyUtils.setProperty(obj, field, val);
        }
        for(String field: optionalFields) {
            Object val = map.getOrDefault(field, null);
            if (val == null) {
                continue;
            }
            PropertyUtils.setProperty(obj, field, val);
        }
        if (errorOnExtra) {

        }
        return obj;

    }

}
