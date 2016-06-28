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

package io.stallion.plugins.javascript;

import io.stallion.services.Log;
import io.stallion.testing.TestClient;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class JsTestSuite {
    private List<Map.Entry<String, Function>> tests = list();
    private TestClient client = new TestClient();
    private String name = "";
    private String file = "";
    private TestResults results;

    public void setUp(Object self) {};
    public void setUpSuite(Object self) {};
    public void tearDown(Object self) {};
    public void tearDownSuite(Object self) {};



    public void add(String name, Function test) {
        Log.info("adding javascript test: {0}", name);
        tests.add(val(name, test));
    }

    public void run() {
        results = new TestResults(getName()).setFile(getFile());
        tearDownSuite(this);
        for (Map.Entry<String, Function> entry: tests) {
            Log.info("Run test {0}", entry.getKey());
            setUp(this);

            try {
                entry.getValue().apply(this);
                results.addResult(entry.getKey(), true, false, null);
            } catch (AssertionError e) {
                ExceptionUtils.printRootCauseStackTrace(e);
                results.addResult(entry.getKey(), false, false, e);
            } catch (Exception e) {
                ExceptionUtils.printRootCauseStackTrace(e);
                results.addResult(entry.getKey(), false, true, e);
            }
            tearDown(this);
        }
    }

    public TestClient getClient() {
        return client;
    }

    public String getName() {
        return name;
    }

    public JsTestSuite setName(String name) {
        this.name = name;
        return this;
    }

    public TestResults getResults() {
        return results;
    }

    public String getFile() {
        return file;
    }

    public JsTestSuite setFile(String file) {
        this.file = file;
        return this;
    }
}
