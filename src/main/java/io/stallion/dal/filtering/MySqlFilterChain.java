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

package io.stallion.dal.filtering;

import io.stallion.dal.base.Model;
import io.stallion.dal.db.DB;
import io.stallion.dal.db.Schema;
import io.stallion.dal.db.SmartQueryCache;
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

        StringBuilder whereBuilder = new StringBuilder();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM " + table + " WHERE ");

        int x = 0;
        List<Object> params = new ArrayList<>();
        for(FilterOperation op: getOperations()) {
            if (getSchema().getColumns().contains(op.getFieldName())) {
                throw new UsageException(MessageFormat.format("Trying to filter on a field that is not in the schema: {0} schema:{1}", op.getFieldName(), clazz.getName()));
            }
            String not = "";
            if (op.getIsExclude() == true) {
                not = " NOT ";
            }
            if (x >= 1) {
                whereBuilder.append(" AND ");
            }

            whereBuilder.append(MessageFormat.format(" {0} ({1} {2} ?) ", not, op.getFieldName(), op.getOperator().forSql()));
            params.add(op.getOriginalValue());
            x++;
        }
        sqlBuilder.append(whereBuilder.toString());
        if (!Literals.empty(getSortField())) {
            if (!getSchema().getColumns().contains(getSortField())) {
                throw new UsageException(MessageFormat.format("Sort field not found in schema: field={0} schema={1}", getSortField(), clazz.getName()));
            }
            sqlBuilder.append(" ORDER BY " + getSortField() + " " + getSortDirection().forSql());
        }
        sqlBuilder.append(" LIMIT " + (page) * size + ", " + size);

        Object[] paramObjects = params.toArray();
        setObjects(DB.instance().query(clazz, sqlBuilder.toString(), paramObjects));

        if (fetchTotalCount) {
            String countSql = "SELECT COUNT(*) FROM " + table + " WHERE " + whereBuilder.toString();
            setMatchingCount(DB.instance().queryScalar(countSql, paramObjects));
        }

    }

    @Override
    public boolean checkSkipCache(String key) {
        return SmartQueryCache.checkShouldSkip(getBucket(), key);
    }
}
