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

package io.stallion.tests.integration.javaSite;

import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.plugins.PluginRegistry;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.MockResponse;
import io.stallion.utils.json.JSON;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class JavaSiteTests extends AppIntegrationCaseBase {


    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/java_site");
        StallionJavaPlugin booter = new StallionJavaPlugin() {
            @Override
            public String getPluginName() {
                return "java-site";
            }

            @Override
            public void boot() throws Exception {

            }
        };
        booter.setPluginRegistry(PluginRegistry.instance());
        ExamplePojoController.register();
        EndpointsRegistry.instance().addResource("/_stx/java-site", new MyResource());

    }

    @Test
    public void testPostObject() throws Exception {

        // Creatify
        ExamplePojo pojo = new ExamplePojo();
        pojo.setAge(41L);
        pojo.setContent("The content");
        pojo.setDisplayName("Marky Mark");
        pojo.setUserName("markymark");
        pojo.setStatus("fake-status-zzzxxx");
        MockResponse response = client.post("/_stx/java-site/hello/creatify", pojo);
        Assert.assertEquals(200, response.getStatus());
        Log.finer("Response: {0}", response.getContent());
        // Status field should be dropped, because it does not have the @Creatable annotation
        assertResponseDoesNotContain(response, "fake-status-zzzxxx");
        ExamplePojo actual = responseToObject(response);
        assertEquals("new", actual.getStatus());
        assertEquals("markymark", actual.getUserName());
        // secret is null because it is not included in the JSON response
        assertNull(actual.getInternalSecret());


        // Username should be the default (blank), since it is not updateable, but displayName will be updated
        pojo.setDisplayName("Mark W");
        pojo.setUserName("markw");
        response = client.post("/_stx/java-site/hello/updatifyHello", pojo);
        Assert.assertEquals(200, response.getStatus());
        Log.finer("Updatify response: {0}", response.getContent());
        actual = responseToObject(response);
        assertEquals("", actual.getUserName());
        assertEquals("Mark W", actual.getDisplayName());
        assertEquals("new", actual.getStatus());
        // secret is null because it is not included in the JSON response
        assertNull(actual.getInternalSecret());

        actual.setStatus("approved");
        response = client.post("/_stx/java-site/hello/moderatify", actual);
        Assert.assertEquals(200, response.getStatus());
        Log.finer("Response: {0}", response.getContent());
        actual = responseToObject(response);
        assertEquals("approved", actual.getStatus());
        assertEquals("theInternalSecret", actual.getInternalSecret());



    }

    @Test
    public void testPatternedEndpoint() throws Exception {
        // Test endpoint with id
        ExamplePojo pojo = new ExamplePojo();
        pojo.setAge(41L);
        MockResponse response = client.post("/_stx/java-site/123/updatify", pojo);
        Assert.assertEquals(200, response.getStatus());
        ExamplePojo mypojo = JSON.parse(response.getContent(), ExamplePojo.class);
        Assert.assertEquals("123", mypojo.getId().toString());
        Assert.assertEquals(123L, (long)mypojo.getId());
        Log.finer("Response: {0}", response.getContent());

    }

    public ExamplePojo responseToObject(MockResponse response) throws Exception {
        return JSON.parse(response.getContent(), ExamplePojo.class);
    }


}
