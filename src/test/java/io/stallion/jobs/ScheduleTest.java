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

package io.stallion.jobs;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

import java.util.List;
import java.util.Map;

import io.stallion.services.Log;
import org.junit.Test;


public class ScheduleTest {

    @Test
    public void testMinutes() {
        // Schedule on every 13
        // Schedule on 12, 45, 57
        // Random minute
        // Schedule every minute
        Log.warn("Implement me!");
    }

    @Test
    public void testHours() {
        // Schedule at 22
        // Schedule at 5, 9, 12
        // Schedule every hour
        Log.warn("Implement me!");
    }

    @Test
    public void testDays() {
        // Every day
        // 3, 8, 9 day of month
        // Friday, Every week
        // Monday, Wednesday, Every Week
        // Thursday, Every other week
        Log.warn("Implement me!");
    }


}
