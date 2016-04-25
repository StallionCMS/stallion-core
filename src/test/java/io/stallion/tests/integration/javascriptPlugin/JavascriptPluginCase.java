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

package io.stallion.tests.integration.javascriptPlugin;

import io.stallion.dal.DalRegistry;
import io.stallion.dal.base.ModelController;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.javascript.TestResults;
import io.stallion.testing.MockResponse;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.utils.json.JSON;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static io.stallion.utils.Literals.*;


public class JavascriptPluginCase extends AppIntegrationCaseBase {


    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/javascript_plugin_site");
    }

    @Test
    public void testControllersRegistered() {
        ModelController controller = DalRegistry.instance().get("js-toml-things");
        assertNotNull(controller);
        assertTrue(controller.filter("name", "jonas").count() > 0);
    }

    @Test
    public void testHeaderHook() {
        MockResponse response = client.get("/");
        assertEquals("my-header", response.getHeader("x-js-plugin-header"));
    }

    @Test
    public void testRouteParams() {

        assertResponseSucceeded(client.post(
                "/js-plugin-test/exercise-params/the-first-thing/1234?limit=50&query=searchterm",
                map(val("fooList", list("bar", "jabber")))
        ));

    }

    @Test
    public void testReturnJson() {
        MockResponse response = client.post("/js-plugin-test/return-json/shires", map());
        Map map = JSON.parseMap(response.getContent());
        assertEquals(500, map.get("five-hundred"));
        assertEquals("alpha", ((List)map.get("fooList")).get(0));
        assertEquals("subValue", ((Map)map.get("fooMap")).get("subItem"));
        assertEquals("shires", map.get("something"));

    }


    @Test
    public void testRender() {
        MockResponse response = client.get("/js-plugin-test/render-template/barrow");
        assertEquals("<h1>barrow</h1>", response.getContent());
    }

    @Test
    public void testHelpers() {
        assertResponseSucceeded(client.get("/js-plugin-test/helpers"));
    }

    @Test
    public void testMyContext() {
        assertResponseSucceeded(client.get("/js-plugin-test/my-context"));
    }

    @Test
    public void testTestFramework() {
        List<TestResults> allResults = PluginRegistry.instance().runJsTests("jsplug", "jsplug.test.js");
        assertEquals(1, allResults.size());
        TestResults results = allResults.get(0);
        assertEquals(2, results.getSucceededCount());
        //assertEquals(1, results.getFailedCount());

    }

    @After
    public void tearDown() throws Exception {

    }


}
