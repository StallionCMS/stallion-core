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

import org.apache.commons.beanutils.BeanComparator;

import java.util.List;

/**
 * Used internally by FilterChain to represent the application of one filter.
 */
public class FilterOperation {
    private String fieldName = "";
    private FilterOperator operator = null;
    private Boolean isExclude = false;
    private BeanComparator comparator;
    private Object typedValue;
    private Object originalValue;
    private Comparable comparableValue = null;
    private Object iterable;
    private Boolean hasDot = null;
    private boolean orOperation;
    private List<FilterOperation> orSubOperations;
    private boolean caseInsensitive = false;

    public boolean hasDot() {
        if (hasDot == null) {
            hasDot = fieldName.contains(".");
        }
        return hasDot;
    }

    public Object getTypedValue() {
        return typedValue;
    }

    public FilterOperation setTypedValue(Object typedValue) {
        this.typedValue = typedValue;
        return this;
    }



    public String getFieldName() {
        return fieldName;
    }

    public FilterOperation setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public String getOperatorForSql() {
        return getOperator().forSql();
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public FilterOperation setOperator(String operator) {
        setOperator(FilterOperator.fromString(operator));
        return this;
    }

    public FilterOperation setOperator(FilterOperator operator) {
        this.operator = operator;
        return this;
    }

    public Boolean getIsExclude() {
        return isExclude;
    }

    public FilterOperation setIsExclude(Boolean isExclude) {
        this.isExclude = isExclude;
        return this;
    }

    public BeanComparator getComparator() {
        return comparator;
    }

    public FilterOperation setComparator(BeanComparator comparator) {
        this.comparator = comparator;
        return this;
    }

    public Object getIterable() {
        return iterable;
    }

    public void setIterable(Object iterable) {
        this.iterable = iterable;
    }

    public Object getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(Object originalValue) {
        this.originalValue = originalValue;
    }

    public Comparable getComparableValue() {
        if (comparableValue == null) {
            if (getTypedValue() instanceof Comparable) {
                comparableValue  = (Comparable) getTypedValue();
            } else if (getOriginalValue() instanceof Comparable) {
                comparableValue = (Comparable) getOriginalValue();
            }
        }
        return comparableValue;

    }

    public boolean isOrOperation() {
        return orOperation;
    }

    public FilterOperation setOrOperation(boolean orOperation) {
        this.orOperation = orOperation;
        return this;
    }

    public List<FilterOperation> getOrSubOperations() {
        return orSubOperations;
    }

    public FilterOperation setOrSubOperations(List<FilterOperation> orSubOperations) {
        this.orSubOperations = orSubOperations;
        return this;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public FilterOperation setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
        return this;
    }
}
