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

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * Holds a conditions for matching objects that gets passed to FilterChain.andAnyOf()
 */
public class Or {
    private String field;
    private Object value;
    private FilterOperator op;

    public Or(String field, Object value) {
        this(field, value, FilterOperator.EQUAL);
    }
    public Or(String field, Object value, String op) {
        this(field, value, FilterOperator.fromString(op));
    }
    public Or(String field, Object value, FilterOperator op) {
        this.field = field;
        this.value = value;
        this.op = op;
    }

    public String getField() {
        return field;
    }


    public Object getValue() {
        return value;
    }

    public FilterOperator getOp() {
        return op;
    }

}
