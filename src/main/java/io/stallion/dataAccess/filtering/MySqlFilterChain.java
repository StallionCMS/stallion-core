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

package io.stallion.dataAccess.filtering;


import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.Schema;
import io.stallion.dataAccess.db.SmartQueryCache;
import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.utils.DateUtils;
import io.stallion.utils.Literals;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.apply;
import static io.stallion.utils.Literals.asArray;
import static io.stallion.utils.Literals.map;

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
        super(bucket, new ArrayList<T>(), null);
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
        return newCopy(chain);
    }

    @Override
    protected void process(int page, int size, boolean fetchTotalCount)  {

        if (page < 1) {
            page = 1;
        }

        StringBuilder whereBuilder = new StringBuilder();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM " + getSchema().getName());

        int x = 0;
        List<Object> params = new ArrayList<>();
        boolean hasDeletedOp = false;
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
            if (op.getFieldName().equals("deleted")) {
                hasDeletedOp = true;
            }

            if (op.isOrOperation()) {
                addOrOperation(op, whereBuilder, params);
            } else {
                if (op.getOperator().equals(FilterOperator.ANY)) {
                    addInClause(whereBuilder, op, params);
                } else {
                    String operatorSql = op.getOperatorForSql();
                    if (operatorSql.equals("=") && op.getOriginalValue() == null) {
                        whereBuilder.append(MessageFormat.format(" (`{0}` IS NULL) ", op.getFieldName()));
                    } else {
                        whereBuilder.append(MessageFormat.format(" (`{0}` {1} ?) ", op.getFieldName(), operatorSql));
                        params.add(formatParam(op));
                    }

                }

            }
            x++;
        }
        if (!getIncludeDeleted() && !hasDeletedOp) {
            if (whereBuilder.length() > 0) {
                whereBuilder.append(" AND ");
            }
            whereBuilder.append(" deleted=0 ");
        }
        String whereSql = whereBuilder.toString();
        if (whereSql.trim().length() > 0) {
            whereSql = " WHERE " + whereSql;
            sqlBuilder.append(whereSql);
        }
        if (!Literals.empty(getSortField())) {
            List<String> columnNames = apply(getSchema().getColumns(), col->col.getName());
            if (!"id".equals(getSortField()) && !columnNames.contains(getSortField().toLowerCase())) {
                throw new UsageException(MessageFormat.format("Sort field not found in schema: field={0} schema={1}", getSortField(), clazz.getName()));
            }
            sqlBuilder.append(" ORDER BY " + getSortField() + " " + getSortDirection().forSql());
            if (isIdAsSecondarySort()) {
                sqlBuilder.append(", id ASC");
            }
        }
        sqlBuilder.append(" LIMIT " + (page - 1) * size + ", " + size);

        Object[] paramObjects = params.toArray();
        setObjects(DB.instance().query(clazz, sqlBuilder.toString(), paramObjects));

        if (page == 1 && getObjects().size() < size) {
            setMatchingCount(getObjects().size());
        } else if (fetchTotalCount) {
            String countSql = "SELECT COUNT(*) FROM " + getSchema().getName() + whereSql;
            Object count = DB.instance().queryScalar(countSql, paramObjects);
            Integer countInt = count instanceof Integer ? (Integer)count : ((Long)count).intValue();
            setMatchingCount(countInt);
        }

        this.setAverages(map());
        this.setSums(map());

        if (getAverageColumns().size() > 0 || getSumColumns().size() > 0) {
            List<String> columnNames = apply(getSchema().getColumns(), col->col.getName());
            String sql = "";
            for (String col: getAverageColumns()) {
                if (!columnNames.contains(col)) {
                    Log.warn("Tried to average column that doesn't exist " + col);
                    continue;
                }
                sql += " AVG(`" + col + "`) AS `staverage_" + col + "`,";
            }
            for (String col: getSumColumns()) {
                if (!columnNames.contains(col)) {
                    Log.warn("Tried to sum column that doesn't exist " + col);
                    continue;
                }
                sql += " SUM(`" + col + "`) AS `stsum_" + col + "`,";
            }
            if (!StringUtils.isEmpty(sql)) {
                sql = " SELECT " + StringUtils.strip(sql, ",") + " FROM `" + getSchema().getName() + "` " + whereSql;
                Map result = DB.instance().findRecord(sql, paramObjects);
                for (String col : getAverageColumns()) {
                    if (result.containsKey("staverage_" + col)) {
                        getAverages().put(col, toDouble(result.get("staverage_" + col)));
                    }
                }
                for (String col : getSumColumns()) {
                    if (result.containsKey("stsum_" + col)) {
                        getSums().put(col, toDouble(result.get("stsum_" + col)));
                    }
                }
            }

        }

    }

    public Double toDouble(Object obj) {
        if (obj instanceof BigInteger) {
            return ((BigInteger)obj).doubleValue();
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal)obj).doubleValue();
        } else if (obj instanceof Double) {
            return (Double)obj;
        } else if (obj instanceof Long) {
            ((Long)obj).longValue();
        } else if (obj instanceof Integer) {
            ((Integer)obj).intValue();
        } else if (obj instanceof Float) {
            ((Float)obj).doubleValue();
        }
        return (Double)obj;
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
            if (subOp.getOperator().equals(FilterOperator.ANY)) {
                addInClause(whereBuilder, subOp, params);
            } else {
                if (subOp.getOperatorForSql().equals("=") && subOp.getOriginalValue() == null) {
                    whereBuilder.append("(`" + subOp.getFieldName() + "` IS null )");
                } else {
                    whereBuilder.append("(`" + subOp.getFieldName() + "` " + subOp.getOperatorForSql() + " ? )");
                    params.add(formatParam(subOp));
                }

            }



        }
        whereBuilder.append(")");
    }

    private void addInClause(StringBuilder whereBuilder, FilterOperation op, List<Object> params) {
        StringBuilder placeholders = new StringBuilder();
        placeholders.append(" (");
        Object[] inParams = null;
        if (op.getOriginalValue() instanceof List) {
            inParams = asArray((List)op.getOriginalValue(), Object.class);
        } else {
            inParams = (Object[])op.getOriginalValue();
        }
        if (inParams.length == 0) {
            return;
        }

        for(int x = 0; x < inParams.length; x++) {
            if (x >= (inParams.length - 1)) {
                placeholders.append("?");
            } else {
                placeholders.append("?,");
            }
            params.add(inParams[x]);
        }
        placeholders.append(") ");
        whereBuilder.append("(`" + op.getFieldName() + "` IN " + placeholders.toString() + ")");
    }

    private Object formatParam(FilterOperation op) {
        Object val = op.getOriginalValue();
        if (op.getOperator().equals(FilterOperator.LIKE)) {
            return "%" + val.toString() + "%";
        } else if (val instanceof ZonedDateTime) {
            return DateUtils.SQL_FORMAT.format(((ZonedDateTime) val).withZoneSameInstant(Literals.UTC));
        } else if (val != null && val.getClass().isEnum()) {
            return val.toString();
        } else {
            return op.getOriginalValue();
        }
    }

    @Override
    public boolean checkSkipCache(String key) {
        return SmartQueryCache.checkShouldSkip(getBucket(), key);
    }
}
