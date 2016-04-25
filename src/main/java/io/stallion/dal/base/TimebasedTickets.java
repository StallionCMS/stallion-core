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

package io.stallion.dal.base;

import io.stallion.settings.Settings;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This generates unique IDs (unique within a particular stallion application) based on the current time.
 * This is used in file based systems.
 *
 * Generates a unique ticket based on a counter, the current time in epoch seconds, and the node number.
 * This will create duplicates if you need to generate more than 10,000 tickets in a second.
 * This will also exceed the MAX_SAFE_INTEGER value for javascript around the year 2248.
 *
 *
 */
public class TimebasedTickets implements Tickets {
    private AtomicInteger i = new AtomicInteger();

    /**
     *
     *
     * @return
     */
    @Override
    public Long nextId() {
        if (i.get() > 9000) {
            i.set(0);
        }
        return ((System.currentTimeMillis() / 1000) * 100000) + i.incrementAndGet() * 10 + Settings.instance().getNodeNumber();
    }
}
