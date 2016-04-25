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

package io.stallion.plugins.javascript;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.stallion.dal.DalRegistry;
import io.stallion.dal.base.MappedModel;
import io.stallion.dal.base.ModelController;
import io.stallion.utils.GeneralUtils;


import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonSerialize(using = JsModelSerializer.class)
public abstract class BaseJavascriptModel extends MappedModel {
    public static String table;
    public abstract Map properties();
    @JsonIgnore
    public abstract String getBucketName();
    @JsonIgnore
    public abstract Map<String, String> getTypeMap();
    @JsonIgnore
    public abstract Map<String, BaseDynamicColumn> getDynamicProperties();
    @JsonIgnore
    public abstract List<String> getJsonIgnoredColumns();

    @JsonIgnore
    @Override
    public ModelController getController() {
        return DalRegistry.instance().get(getBucketName());
    }

    public BaseJavascriptModel() {

    }

    public BaseJavascriptModel(Map<String, Object> values) {
        for (String key: values.keySet()) {
            put(key, values.get(key));
        }
    }

    @Override
    public boolean containsKey(Object key) {
        if (getDynamicProperties().containsKey(key.toString())) {
            return true;
        }
        return super.containsKey(key);
    }

    @Override
    public Object get(Object key) {
        if (getDynamicProperties().containsKey(key.toString())) {
            BaseDynamicColumn col = getDynamicProperties().get(key.toString());
            return col.func(this);
        }
        return super.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        if (value == null) {
            super.put(key, value);
            return value;
        }

        if (getTypeMap().containsKey(key)) {
            String type = getTypeMap().get(key);
            switch (type) {
                case "long":
                    if (value instanceof Long) {
                        break;
                    } else if (value instanceof Integer) {
                        value = new Long((Integer) value);
                    } else if (value instanceof Number) {
                        value = ((Number) value).longValue();
                    } else {
                        value = Long.parseLong(value.toString());
                    }
                    break;
                case "integer":
                    if (value instanceof Integer) {
                        break;
                    } else if (value instanceof Number) {
                        value = ((Number)value).intValue();
                    } else {
                        value = Integer.parseInt(value.toString());
                    }
                    break;
                case "boolean":
                    if (value instanceof Boolean) {
                        break;
                    } else if (value instanceof Integer) {
                        value = ((Integer)value == 1);
                    } else if (value instanceof Long) {
                        value = ((Long)value == 1);
                    } else {
                        value = value.toString().toLowerCase().equals("true");
                    }
                    break;
                case "datetime":
                    if (value instanceof ZonedDateTime) {
                        break;
                    } else if (value instanceof Date) {
                        value = ((Date)value).toInstant().atZone(GeneralUtils.UTC);
                    } else if (value instanceof java.sql.Date) {
                        value = ((java.sql.Date)value).toInstant().atZone(GeneralUtils.UTC);
                    } else if (value instanceof Long) {
                        value = Instant.ofEpochMilli((Long)value).atZone(GeneralUtils.UTC);
                    }
            }
        }
        return super.put(key, value);

    }
}
