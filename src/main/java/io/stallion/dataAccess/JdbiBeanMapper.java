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
import io.stallion.dataAccess.db.converters.AttributeConverter;
import io.stallion.reflection.PropertyUtils;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.math.BigInteger;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static io.stallion.utils.Literals.empty;


public class JdbiBeanMapper<T> implements RowMapper<T> {
    private Class cls;
    private Schema schema;

    public JdbiBeanMapper(Class<? extends T> cls) {
        this.cls = cls;
        this.schema = DB.instance().getSchema(cls);
    }

    @Override
    public T map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        try {
            ResultSetMetaData rsmd = resultSet.getMetaData();
            T obj = (T) cls.newInstance();
            for (int i=1; i <= rsmd.getColumnCount(); i++) {
                String columnName = rsmd.getColumnName(i);
                String columnLabel = rsmd.getColumnLabel(i);

                Object value = resultSet.getObject(columnName);

                /*
                if (col.getAttributeConverter() != null) {
                    value = col.getAttributeConverter().convertToEntityAttribute(value);
                } else if (!empty(col.getConverterClassName())) {
                    AttributeConverter converter = DB.instance().getConverter(col.getConverterClassName());
                    value = converter.convertToEntityAttribute(value);
                }
                */
                // MySQL driver returns bigint as BigInteger, despite it really being a Long sized field
                if (value instanceof BigInteger) {
                    value = ((BigInteger) value).longValue();
                }
                if (value instanceof Timestamp) {
                    //value = ((Timestamp)value)
                    value = ZonedDateTime.ofInstant(((Timestamp) value).toInstant(), ZoneId.of("UTC"));
                }

                PropertyUtils.setProperty(obj, columnLabel, value);
            }

            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
