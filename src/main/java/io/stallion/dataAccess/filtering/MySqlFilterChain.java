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

package io.stallion.dataAccess.filtering;

import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.Schema;
import io.stallion.dataAccess.db.SmartQueryCache;
import io.stallion.exceptions.UsageException;
import io.stallion.utils.Literals;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A filter chain that generates a MySQL query, rather than operating on
 * an in memory datastructure
 *
 * @param <T>
 */
public class MySqlFilterChain<T extends Model> extends FilterChain<T> {
    private Class<T> clazz;
    private String table;

    public MySqlFilterChain(String table, String bucket, Class<T> clazz) {
        super(bucket, null);
        this.clazz = clazz;
        this.table = table;
    }

    public MySqlFilterChain(String table, FilterChain chain) {
        super(chain, null);
        this.table = table;
    }

    public Schema getSchema() {
        return DB.instance().getSchema(clazz);
    }

    protected FilterChain<T> newCopy() {
        FilterChain<T> chain = new MySqlFilterChain<T>(table, this.getBucket(), this.clazz);
        chain.operations = (ArrayList<FilterOperation>)getOperations().clone();
        chain.setIncludeDeleted(getIncludeDeleted());
        return chain;
    }

    @Override
    protected void process(int page, int size, boolean fetchTotalCount)  {

        if (page < 1) {
            page = 0;
        }

        StringBuilder whereBuilder = new StringBuilder();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM " + table + " WHERE ");

        int x = 0;
        List<Object> params = new ArrayList<>();
        for(FilterOperation op: getOperations()) {
            if (getSchema().getColumns().contains(op.getFieldName())) {
                throw new UsageException(MessageFormat.format("Trying to filter on a field that is not in the schema: {0} schema:{1}", op.getFieldName(), clazz.getName()));
            }
            if (x >= 1) {
                whereBuilder.append(" AND ");
            }
            if (op.getIsExclude() == true) {
                whereBuilder.append(" NOT ");
            }


            if (op.isOrOperation()) {
                addOrOperation(op, whereBuilder, params);
            } else {
                whereBuilder.append(MessageFormat.format(" ({0} {1} ?) ", op.getFieldName(), op.getOperator().forSql()));
                params.add(op.getOriginalValue());
            }
            x++;
        }
        sqlBuilder.append(whereBuilder.toString());
        if (!Literals.empty(getSortField())) {
            if (!"id".equals(getSortField()) && !getSchema().getColumns().contains(getSortField())) {
                throw new UsageException(MessageFormat.format("Sort field not found in schema: field={0} schema={1}", getSortField(), clazz.getName()));
            }
            sqlBuilder.append(" ORDER BY " + getSortField() + " " + getSortDirection().forSql());
        }
        sqlBuilder.append(" LIMIT " + (page - 1) * size + ", " + size);

        Object[] paramObjects = params.toArray();
        setObjects(DB.instance().query(clazz, sqlBuilder.toString(), paramObjects));

        if (page == 1 && getObjects().size() < size) {
            setMatchingCount(getObjects().size());
        } else if (fetchTotalCount) {
            String countSql = "SELECT COUNT(*) FROM " + table + " WHERE " + whereBuilder.toString();
            Object count = DB.instance().queryScalar(countSql, paramObjects);
            Integer countInt = count instanceof Integer ? (Integer)count : ((Long)count).intValue();
            setMatchingCount(countInt);
        }

    }

    private void addOrOperation(FilterOperation or, StringBuilder whereBuilder, List<Object> params) {
        whereBuilder.append("(");
        for (int x = 0; x < or.getOrSubOperations().size(); x++) {
            if (x > 0) {
                whereBuilder.append(" OR ");
            }
            FilterOperation subOp = or.getOrSubOperations().get(x);
            if (subOp.isOrOperation()) {
                addOrOperation(subOp, whereBuilder, params);
                continue;
            }
            if (subOp.getIsExclude()) {
                whereBuilder.append(" NOT ");
            }
            whereBuilder.append("(`" + subOp.getFieldName() + "`" + subOp.getOperator().forSql() + " ? )");
            params.add(subOp.getOriginalValue());

        }
        whereBuilder.append(")");
    }

    @Override
    public boolean checkSkipCache(String key) {
        return SmartQueryCache.checkShouldSkip(getBucket(), key);
    }
}
