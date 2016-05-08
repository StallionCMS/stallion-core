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

import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.DateUtils;

import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This generates unique IDs (unique within a particular stallion application) based on the current time.
 * This is used in file based systems.
 *
 * Generates a unique ticket based on a counter, the current time rounded to nearest 10 seconds, and the node number.
 * This will create duplicates if you need to generate more than 10,000 tickets in a second.
 * This will also exceed the MAX_SAFE_INTEGER value for javascript around the year 2248.
 *
 *
 */
public class TimebasedTickets implements Tickets {
    private AtomicInteger i = new AtomicInteger();
    private Long baseSeconds = null;
    private static int loadedCount = 0;

    public TimebasedTickets() {
        // If we reload tickets during a test, we need to start with a higher increment or else
        // we will generate the same IDs over again. If we run more than 100 test cases with
        // full app context reloads in a single
        // second, then we might get duplicate IDs, but that is probably impossible
        loadedCount++;
        i.set(loadedCount * 1000);
    }




    /**
     *
     *
     * @return
     */
    @Override
    public Long nextId() {
        Integer counter = i.incrementAndGet();
        if (counter > 99500) {
            i.set(0);
        }
        Long currentSeconds = System.currentTimeMillis() / 1000;
        Long sec = currentSeconds - getBaseSeconds();
        Long ticket = ((sec) * 1000000) + (counter * 10) + Settings.instance().getNodeNumber();
        return ticket;
    }

    private Long getBaseSeconds() {
        if (baseSeconds == null) {
            baseSeconds = Settings.instance().getAppCreatedMillis() / 1000;
        }
        return baseSeconds;
    }
}
