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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.ArrayHandler;


public class BeanListHandler<T> implements ResultSetHandler<List<T>> {
    private final Class<T> type;


    public BeanListHandler(Class<T> type) {
        this.type = type;
    }


    public List<T> handle(ResultSet rs) throws SQLException {
        List<T> beans = list();
        String[] columnToProperty = null;
        while (rs.next()) {
            try {
                if (columnToProperty == null) {
                    columnToProperty = makeColumnToProperty(rs.getMetaData(), PropertyUtils.getPropertyNames(type));
                }
                T bean = (T)type.newInstance();
                for(int i=1;i<columnToProperty.length;i++) {
                    Object val = rs.getObject(i);
                    String propertyName = columnToProperty[i];
                    if (empty(propertyName)) {
                        continue;
                    }
                    PropertyUtils.setProperty(bean, propertyName, val);
                }
                beans.add(bean);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return beans;
    }

    private String[] makeColumnToProperty(ResultSetMetaData rsmd, List<String> propertyNames) throws SQLException {
        int cols = rsmd.getColumnCount();
        String[] columnToProperty = new String[cols + 1];
        Arrays.fill(columnToProperty, "");


        for(int col = 1; col <= cols; ++col) {
            String columnName = rsmd.getColumnLabel(col);
            if(null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }

            for(int i = 0; i < propertyNames.size(); ++i) {
                String propertyName = propertyNames.get(i);
                if(propertyName.equalsIgnoreCase(columnName)) {
                    columnToProperty[col] = propertyName;
                    break;
                }
            }
        }
        return columnToProperty;
    }

    private void hydrateBean(T bean, ResultSet rs) throws SQLException {

    }

}

