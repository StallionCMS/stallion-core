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

package io.stallion.dataAccess.db.converters;

import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.db.Schema;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ModelListHandler<T extends Model> extends AbstractListHandler<T> implements ResultToModel<T> {
    private Class modelClass;
    private Schema schema;

    public ModelListHandler(Schema schema) {
        this.modelClass = schema.getClazz();
        this.schema = schema;
    }


    @Override
    public Class getModelClass() {
        return modelClass;
    }


    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    protected T handleRow(ResultSet resultSet) throws SQLException {
        return handleOneRow(resultSet);
    }

    /*
    @Override
    public List<BaseModel> handle(ResultSet resultSet) throws SQLException {

    }
    */
}
