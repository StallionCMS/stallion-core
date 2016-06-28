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

package io.stallion.tests.sql;


public class PicnicAttendence {
    private String name = "";
    private int count = 0;
    private PicnicRsvpStatus rsvp = PicnicRsvpStatus.MAYBE;

    public String getName() {
        return name;
    }

    public PicnicAttendence setName(String name) {
        this.name = name;
        return this;
    }

    public int getCount() {
        return count;
    }

    public PicnicAttendence setCount(int count) {
        this.count = count;
        return this;
    }

    public PicnicRsvpStatus getRsvp() {
        return rsvp;
    }

    public PicnicAttendence setRsvp(PicnicRsvpStatus rsvp) {
        this.rsvp = rsvp;
        return this;
    }
}
