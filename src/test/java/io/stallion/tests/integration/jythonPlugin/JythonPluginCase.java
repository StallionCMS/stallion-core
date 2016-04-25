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

package io.stallion.tests.integration.jythonPlugin;

import io.stallion.testing.AppIntegrationCaseBase;
import org.junit.After;
import org.junit.BeforeClass;


public class JythonPluginCase extends AppIntegrationCaseBase {


    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/jython_plugin_site");
    }

    /*
    @Test
    public void testEndpoints() {

        MockResponse response;

        response = client.get("/_stx/jyplug/pages/justice-at-sunrise.md");
        Assert.assertEquals(200, response.getStatus());
        Log.finest("View post content: {0}", response.getContent());
        Assert.assertTrue(response.getContent().contains("At sunrise he summoned all hands"));
        Assert.assertTrue(response.getContent().contains("\"slug\" : \"/justice-at-sunrise\""));


        response = client.get("/_stx/jyplug/pages/find-pages?title=The Steamer Across the Atlantic&author=Jules%20Verne");
        Assert.assertEquals(200, response.getStatus());
        Log.finest("Find pages content: {0}", response.getContent());
        Assert.assertTrue(response.getContent().contains("While these events"));
        Assert.assertTrue(response.getContent().contains("\"slug\" : \"/steamer-atlantic\""));
        Assert.assertTrue(!response.getContent().contains("\"slug\" : \"/justice-at-sunrise\""));

        response = client.post("/_stx/jyplug/pages/noop/skeleton-key", null);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getContent().contains("\"the_list\" : [ \"a\", \"b\", \"c\" ]"));
        Assert.assertTrue(response.getContent().contains("skeleton-key"));



        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("firstName", "Ebeneezer");
        response = client.post("/_stx/jyplug/pages/postback", queryMap);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getContent().contains("\"posted_value\" : \"Ebeneezer\""));
        Log.finest("postback content: {0}", response.getContent());

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("thingabob", "jabberwork");
        response = client.put("/_stx/jyplug/pages/putter", dataMap);
        Log.finest("putter content: {0}", response.getContent());
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getContent().contains("\"thingabob\" : \"jabberwork\""));

    }


    @Test
    public void testHooks() {
        Log.finer("test jython hooks");

        MockResponse response;

        response = client.get("/justice-at-sunrise");
        Assert.assertEquals(200, response.getStatus());
        Log.finest("Justice: {0}", response.getContent());

        Assert.assertTrue(response.getContent().contains("<meta name=\"jython_user\" value=\"Alabaster\">"));
        Assert.assertTrue(response.getContent().contains("<meta name=\"jython_value\" value=\"jabberwocky\">"));

    }

          */

    @After
    public void tearDown() throws Exception {

    }


}

