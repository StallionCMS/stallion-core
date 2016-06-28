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

import io.stallion.exceptions.UsageException;

public enum FilterOperator {
    EQUAL,
    NOT_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    GREATER_THAN,
    IN,
    GREATER_THAN_OR_EQUAL
    ;
    public static FilterOperator fromString(String op) {
        switch (op) {
            case "eq":
            case "=":
            case "==":
            case "===":
                return EQUAL;
            case "!=":
            case "neq":
                return NOT_EQUAL;
            case ">":
            case "gt":
                return GREATER_THAN;
            case ">=":
            case "gte":
                return GREATER_THAN_OR_EQUAL;
            case "lt":
            case "<":
                return LESS_THAN;
            case "lte":
            case "<=":
                return LESS_THAN_OR_EQUAL;
            case "in":
                return IN;
            default:
                return Enum.valueOf(FilterOperator.class, op);
        }
    }

    public String forSql() {
        switch (this) {
            case EQUAL:
                return "=";
            case NOT_EQUAL:
                return "!=";
            case LESS_THAN:
                return "<";
            case GREATER_THAN:
                return ">";
            case LESS_THAN_OR_EQUAL:
                return "<=";
            case IN:
                return "in";
            case GREATER_THAN_OR_EQUAL:
                return ">=";
            default:
                throw new UsageException("Cann use FilterOperator " + this.toString() + " with SQL");
        }
    }
}
