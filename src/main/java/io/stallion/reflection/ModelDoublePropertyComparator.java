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

package io.stallion.reflection;

import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.filtering.SortDirection;

import java.util.Comparator;


public class ModelDoublePropertyComparator<T extends Model> implements Comparator<T> {
    private String propertyName = "";
    private SortDirection dir;
    private String secondaryName = "";
    private SortDirection dir2;

    public ModelDoublePropertyComparator(String propertyName, SortDirection dir) {
        this.propertyName = propertyName;
        this.dir = dir;

    }

    public ModelDoublePropertyComparator(String propertyName, SortDirection dir, String secondaryName, SortDirection dir2) {
        this.propertyName = propertyName;
        this.dir = dir;
        this.secondaryName = secondaryName;
        this.dir2 = dir2;
    }




    @Override
    public int compare(T o1, T o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }
        Integer result = compareProperties(propertyName, o1, o2);
        if (SortDirection.DESC.equals(dir)) {
            result = -result;
        }

        if (result == 0) {
            result = compareProperties(secondaryName, o1, o2);
            if (SortDirection.DESC.equals(dir2)) {
                result = -result;
            }

        }
        return result;
    }

    private int compareProperties(String propertyName, Object o1, Object o2) {
        Integer result = null;
        Comparable val1 = (Comparable)PropertyUtils.getPropertyOrMappedValue(o1, propertyName);
        Comparable val2 = (Comparable)PropertyUtils.getPropertyOrMappedValue(o2, propertyName);

        if (val1 == null && val2 == null) {
            result = 0;
        } else if (val1 == null) {
            return 1;
        } else if (val2 == null) {
            return -1;
        }

        if (result != null) {
            if (val1 instanceof String && val2 instanceof String) {
                result = ((String) val1).compareToIgnoreCase((String) val2);
            } else {
                result = val1.compareTo(val2);
            }
        }
        return result;
    }
}
