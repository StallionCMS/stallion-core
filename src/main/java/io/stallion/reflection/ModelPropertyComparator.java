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

import java.util.Comparator;


public class ModelPropertyComparator <T extends Model> implements Comparator<T> {
    private String propertyName = "";
    private boolean idIsSecondarySort = false;

    public ModelPropertyComparator(String propertyName) {
        this.propertyName = propertyName;
    }


    public ModelPropertyComparator(String propertyName, boolean idIsSecondarySort) {
        this.propertyName = propertyName;
        this.idIsSecondarySort = idIsSecondarySort;
    }


    @Override
    public int compare(T o1, T o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }
        Comparable val1 = (Comparable)PropertyUtils.getPropertyOrMappedValue(o1, propertyName);
        Comparable val2 = (Comparable)PropertyUtils.getPropertyOrMappedValue(o2, propertyName);
        if (val1 == null && val2 == null) {
            if (idIsSecondarySort && o1.getId() != null && o2.getId() != null) {
                return o1.getId().compareTo(o2.getId());
            } else {
                return 0;
            }
        } else if (val1 == null) {
            return 1;
        } else if (val2 == null) {
            return -1;
        }

        int result = val1.compareTo(val2);
        if (result == 0 && idIsSecondarySort && o1.getId() != null && o2.getId() != null) {
            o1.getId().compareTo(o2.getId());
        }
        return result;
    }
}
