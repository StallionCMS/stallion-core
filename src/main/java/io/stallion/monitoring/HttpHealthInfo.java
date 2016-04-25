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

package io.stallion.monitoring;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class HttpHealthInfo {
    private int error500s = 0;
    private int error400s = 0;
    private int error404s = 0;
    private int requestCount = 0;

    public int getError500s() {
        return error500s;
    }

    public void setError500s(int error500s) {
        this.error500s = error500s;
    }

    public int getError400s() {
        return error400s;
    }

    public void setError400s(int error400s) {
        this.error400s = error400s;
    }

    public int getError404s() {
        return error404s;
    }

    public void setError404s(int error404s) {
        this.error404s = error404s;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}
