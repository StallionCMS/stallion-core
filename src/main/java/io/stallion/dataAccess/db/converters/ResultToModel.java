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

package io.stallion.dataAccess.db.converters;

import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.db.Col;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.Schema;
import io.stallion.exceptions.IllegalEnumValue;
import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static io.stallion.utils.Literals.empty;


public interface ResultToModel<T extends Model> {
    public Class getModelClass();
    public Schema getSchema();

    public default T handleOneRow(ResultSet resultSet) throws SQLException {
        try {
            T obj = (T)getModelClass().newInstance();
            try {
                Date date = resultSet.getDate("row_updated_at");
                if (date != null) {
                    obj.setLastModifiedMillis(date.getTime());
                }
            } catch (SQLException e) {

            }
            for (Col col : getSchema().getColumns()) {
                Object value = null;
                // TODO: really wish there was a way to test for the existence of a column,
                // rather than throwing the exception
                try {
                    value = resultSet.getObject(col.getName());
                } catch (SQLException e) {
                    continue;
                }
                if (col.getJsDbColumnConverter() != null) {
                    value = col.getJsDbColumnConverter().fromDb(obj, value, col.getName());
                } else if (col.getAttributeConverter() != null) {
                    value = col.getAttributeConverter().convertToEntityAttribute(value);
                } else if (!empty(col.getConverterClassName())) {
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
                if (value instanceof Date) {
                    value = ((Date) value).toLocalDate();
                }
                if (value == null && col.getDefaultValue() != null) {
                    value = col.getDefaultValue();
                }
                try {
                    PropertyUtils.setProperty(obj, col.getPropertyName(), value);
                } catch (IllegalEnumValue e) {
                    Log.warn("Illegal enum value error: " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
                }
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
