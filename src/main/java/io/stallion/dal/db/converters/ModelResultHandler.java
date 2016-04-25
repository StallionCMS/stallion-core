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
import io.stallion.dal.db.Schema;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ModelResultHandler implements ResultSetHandler<Model>, ResultToModel {

    private Class modelClass;
    private Schema schema;

    public ModelResultHandler(Schema schema) {
        this.modelClass = schema.getClazz();
        this.setSchema(schema);
    }

    @Override
    public Model handle(ResultSet resultSet) throws SQLException {
        Boolean hasMore = resultSet.next();
        if (hasMore) {
            return handleOneRow(resultSet);
        } else {
            return null;
        }
    }

    public Class getModelClass() {
        return modelClass;
    }

    public void setModelClass(Class modelClass) {
        this.modelClass = modelClass;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
