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

package io.stallion.testing;

public class StubbingInfo {
    private int minCount = 1;
    private int maxCount = 99;
    private String caller;

    public int getMinCount() {
        return minCount;
    }

    public StubbingInfo setMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public StubbingInfo setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public String getCaller() {
        return caller;
    }

    public StubbingInfo setCaller(String caller) {
        this.caller = caller;
        return this;
    }
}
