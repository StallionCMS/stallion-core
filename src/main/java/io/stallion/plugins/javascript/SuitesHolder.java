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

package io.stallion.plugins.javascript;

import java.util.List;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class SuitesHolder {
    private List<JsTestSuite> suites = list();
    private String file = "";

    public String getFile() {
        return file;
    }

    public SuitesHolder setFile(String file) {
        this.file = file;
        return this;
    }

    public SuitesHolder add(JsTestSuite suite) {
        this.suites.add(suite);
        return this;
    }

    public List<JsTestSuite> getSuites() {
        return suites;
    }

    public SuitesHolder setSuites(List<JsTestSuite> suites) {
        this.suites = suites;
        return this;
    }
}
