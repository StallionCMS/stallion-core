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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;

import javax.persistence.Column;


public class DemoThing {
    private String name;
    private List<String> tags = list();
    private ZonedDateTime createdAt;


    public static List<DemoThing> getExamples() {
        return list(
                new DemoThing()
                        .setName("Foo")
                        .setTags(list("dreams", "variables"))
                        .setCreatedAt(utcNow()),
                new DemoThing()
                        .setName("Bar")
                        .setTags(list("crimes", "history"))
                        .setCreatedAt(utcNow().minusDays(2).minusMinutes(911))
        );
    }

    @Column
    public String getName() {
        return name;
    }

    public DemoThing setName(String name) {
        this.name = name;
        return this;
    }

    @Column
    public List<String> getTags() {
        return tags;
    }

    public DemoThing setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    @Column
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public DemoThing setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
