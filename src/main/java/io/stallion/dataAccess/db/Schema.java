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

package io.stallion.dataAccess.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Used internally to represent the schema of a database table. Model classes can
 * be parsed into schemas, the schemas are then used to build the INSERT or UPDATE
 * sql when doing database operations.
 */
public class Schema {
    private List<Col> columns;
    private String name;
    private Class clazz;
    private List<String> keyNames;

    public Schema(String name, Class clazz) {
        columns = new ArrayList<Col>();
        this.name = name;
        this.clazz = clazz;
    }

    public List<Col> getColumns() {
        return columns;
    }

    public Schema setColumns(List<Col> columns) {
        this.columns = columns;
        return this;
    }

    public String getName() {
        return name;
    }

    public Schema setName(String name) {
        this.name = name;
        return this;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public List<String> getKeyNames() {
        if (keyNames == null) {
            keyNames = new ArrayList<>();
            for(Col col: columns) {
                if (col.getAlternativeKey()) {
                    keyNames.add(col.getPropertyName());
                }
            }
        }
        return this.keyNames;
    }


}
