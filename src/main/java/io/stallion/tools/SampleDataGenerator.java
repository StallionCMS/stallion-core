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

package io.stallion.tools;

import io.stallion.exceptions.UsageException;

import java.util.Set;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public abstract class SampleDataGenerator {
    private Set<Long> seenIds = set();

    public abstract Long getBaseId();


    public abstract void generate();

    public Long newId(int extra) {
        Long id = getBaseId() + extra;
        if (seenIds.contains(id)) {
            throw new UsageException("The id " + extra + " has already been used.");
        }
        seenIds.add(id);
        return id;
    }

    public Long getId(int extra) {
        return getBaseId() + extra;
    }


}
