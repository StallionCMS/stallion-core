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

import java.util.Comparator;

/**
 * A comparator that compares objects based on the passed in property name, using
 * PropertyUtils to look up the parameter.
 *
 * @param <T>
 */
public class PropertyComparator<T> implements Comparator<T> {
    private String propertyName = "";

    public PropertyComparator(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public int compare(T o1, T o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }
        Comparable val1 = (Comparable)PropertyUtils.getPropertyOrMappedValue(o1, propertyName);
        Comparable val2 = (Comparable)PropertyUtils.getPropertyOrMappedValue(o2, propertyName);
        if (val1 == null && val2 == null) {
            return 0;
        } else if (val1 == null) {
            return 1;
        } else if (val2 == null) {
            return -1;
        }

        return val1.compareTo(val2);
    }
}
