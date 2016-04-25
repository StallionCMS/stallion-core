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

package io.stallion.dal.db.converters;

import io.stallion.dal.base.Model;
import io.stallion.dal.db.Col;
import io.stallion.dal.db.DB;
import io.stallion.dal.db.Schema;
import io.stallion.reflection.PropertyUtils;


import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static io.stallion.utils.Literals.empty;


public interface ResultToModel<T extends Model> {
    public Class getModelClass();
    public Schema getSchema();

    public default T handleOneRow(ResultSet resultSet) throws SQLException {
        try {
            T obj = (T)getModelClass().newInstance();
            for (Col col : getSchema().getColumns()) {
                Object value = resultSet.getObject(col.getName());

                if (col.getConverter() != null) {
                    value = col.getConverter().fromDb(obj, value, col.getName());
                }
                if (!empty(col.getConverterClassName())) {
                    AttributeConverter converter = DB.instance().getConverter(col.getConverterClassName());
                    value = converter.convertToEntityAttribute(value);
                }
                // MySQL driver returns bigint as BigInteger, despite it really being a Long sized field
                if (value instanceof BigInteger) {
                    value = ((BigInteger) value).longValue();
                }
                if (value instanceof Timestamp) {
                    //value = ((Timestamp)value)
                    value = ZonedDateTime.ofInstant(((Timestamp) value).toInstant(), ZoneId.of("UTC"));
                }
                if (value == null && col.getDefaultValue() != null) {
                    value = col.getDefaultValue();
                }
                PropertyUtils.setProperty(obj, col.getPropertyName(), value);
            }

            Object idObj = resultSet.getObject("id");
            Long id;
            if (idObj instanceof BigInteger) {
                id = ((BigInteger) idObj).longValue();
            } else  {
                id = (Long)idObj;
            }
            obj.setId(id);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
