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

package io.stallion.tests.mysql;

import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.db.Converter;
import io.stallion.dataAccess.db.converters.JsonSetConverter;

import javax.persistence.Column;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.stallion.utils.Literals.*;

@Table(name="stallion_test_picnic")
public class Picnic extends ModelBase {
    private String location = "";
    private String description = "";
    private ZonedDateTime date;
    private boolean canceled = false;
    private List<PicnicAttendence> attendees = list();
    private Set<Long> adminIds = set();
    private List<String> dishes = list();
    private Map<String, Object> extra = map();
    private PicnicType type = PicnicType.INVITEES_ONLY;


    @Column
    public String getLocation() {
        return location;
    }

    public Picnic setLocation(String location) {
        this.location = location;
        return this;
    }

    @Column
    public String getDescription() {
        return description;
    }

    public Picnic setDescription(String description) {
        this.description = description;
        return this;
    }

    @Column
    public ZonedDateTime getDate() {
        return date;
    }

    public Picnic setDate(ZonedDateTime date) {
        this.date = date;
        return this;
    }

    @Column
    public boolean isCanceled() {
        return canceled;
    }

    public Picnic setCanceled(boolean canceled) {
        this.canceled = canceled;
        return this;
    }

    @Column
    @Converter(name="io.stallion.tests.mysql.PicnicAttendeesConverter")
    public List<PicnicAttendence> getAttendees() {
        return attendees;
    }

    public Picnic setAttendees(List<PicnicAttendence> attendees) {
        this.attendees = attendees;
        return this;
    }

    @Column
    @Converter(cls= JsonSetConverter.class)
    public Set<Long> getAdminIds() {
        return adminIds;
    }

    public Picnic setAdminIds(Set<Long> adminIds) {
        this.adminIds = adminIds;
        return this;
    }

    @Column
    @Converter(name="io.stallion.dataAccess.db.converters.JsonListConverter")
    public List<String> getDishes() {
        return dishes;
    }

    public Picnic setDishes(List<String> dishes) {
        this.dishes = dishes;
        return this;
    }

    @Column
    @Converter(name="io.stallion.dataAccess.db.converters.JsonMapConverter")
    public Map<String, Object> getExtra() {
        return extra;
    }

    public Picnic setExtra(Map<String, Object> extra) {
        this.extra = extra;
        return this;
    }

    @Column
    public PicnicType getType() {
        return type;
    }

    public Picnic setType(PicnicType type) {
        this.type = type;
        return this;
    }
}
