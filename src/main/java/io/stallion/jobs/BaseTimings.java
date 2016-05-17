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

import io.stallion.exceptions.ConfigException;

import java.util.ArrayList;

/** Base class for schedule timings */
class BaseTimings extends ArrayList<Integer> {
    private boolean unset = true;
    private boolean every = false;
    private boolean random = false;

    public void verifyAndUpdateUnset() throws ConfigException{
        if (unset == true) {
            unset = false;
        } else {
            throw new ConfigException("You are trying to set a field that has already been set: " + this.getClass().getSimpleName());
        }
    }

    public boolean isUnset() {
        return unset;
    }


    public boolean isEvery() {
        return every;
    }

    public void setEvery(boolean every) {
        this.every = every;
    }

    public boolean isRandom() {
        return random;
    }

    public BaseTimings setRandom(boolean random) {
        this.random = random;
        return this;
    }
}
