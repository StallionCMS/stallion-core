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


public enum SortDirection {
    DESC,
    ASC;

    public static SortDirection fromString(String direction) {
        switch (direction) {
            case "DESC":
            case "-":
            case "desc":
                return SortDirection.DESC;
            case "ASC":
            case "asc":
            case "":
                return SortDirection.ASC;
            default:
                return Enum.valueOf(SortDirection.class, direction);
        }
    }

    public String forSql() {
        switch (this) {
            case DESC:
                return "desc";
            default:
                return "asc";
        }
    }

}
