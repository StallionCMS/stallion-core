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

import java.util.*;


public class FilterGroup<Y> {
    private List<Y> items = new ArrayList<>();
    private String key = "";
    private int count = 0;
    private Set<List<String>> firstOfs = new HashSet<>();
    private Set<List<String>> lastOfs = new HashSet<>();

    public FilterGroup(String key) {
        this.key = key;
    }


    public boolean lastOf(String...fieldNames) {
        if (getLastOfs().contains(Arrays.asList(fieldNames))) {
            return true;
        }
        return false;
    }

    public boolean firstOf(String...fieldNames) {
        if (getFirstOfs().contains(Arrays.asList(fieldNames))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * The first matching item for this group.
     *
     * @return
     */
    public Y getFirst() {
        if (items.size() == 0) {
            return null;
        } else {
            return getItems().get(0);
        }
    }

    /**
     * All matching items for this group
     * @return
     */
    public List<Y> getItems() {
        return items;
    }

    public void setItems(List<Y> items) {
        this.items = items;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The count of matching items.
     *
     * @return
     */
    public int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }

    int incrCount() {
        count++;
        return count;
    }

    public Set<List<String>> getFirstOfs() {
        return firstOfs;
    }

    public void setFirstOfs(Set<List<String>> firstOfs) {
        this.firstOfs = firstOfs;
    }

    public Set<List<String>> getLastOfs() {
        return lastOfs;
    }

    public void setLastOfs(Set<List<String>> lastOfs) {
        this.lastOfs = lastOfs;
    }
}
