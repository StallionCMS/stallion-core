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

package io.stallion.tests.integration.javaSite;

import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.plugins.PluginRegistry;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.JerseyIntegrationBaseCase;
import io.stallion.utils.json.JSON;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;

import static io.stallion.utils.Literals.UTC;
import static io.stallion.utils.Literals.map;
import static io.stallion.utils.Literals.val;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaSiteTests extends JerseyIntegrationBaseCase {
    @BeforeClass
    public static void setUpClass() throws Exception {

        StallionJavaPlugin booter = new StallionJavaPlugin() {
            @Override
            public String getName() {
                return "java-site";
            }

            @Override
            public void onRegisterAll()  {
                ExamplePojoController.register();
            }

            @Override
            public void onBuildResourceConfig(ResourceConfig rc) {
                rc.register(MyResource.class);
            }
        };
        startApp("/java_site", null, booter);
    }

    @Test
    public void testHelloWorld() {
        /*
        {

        }
        */

        Response response = target("/my-hello/simple")
                .queryParam("name", "Garfield")
                .request()
                .get();
        String output = response.readEntity(String.class);
        Log.info(output);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("Hello, Garfield", output);

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
        pojo.setDueAt(ZonedDateTime.of(2020, 2, 20, 12, 0, 0, 0, UTC));
        Response response = target("/my-hello/creatify")
                .request()
                .header("X-Requested-By", "XmlHttpRequest")
                .post(
                        Entity.json(pojo)
                );

        //MockResponse response = client.post("/_stx/java-site/hello/creatify", pojo);
        Assert.assertEquals(200, response.getStatus());
        String output = response.readEntity(String.class);
        Log.info("Response: {0}", output);
        // Status field should be dropped, because it was not settable
        assertNotContains(output, "fake-status-zzzxxx");
        ExamplePojo actual = JSON.parse(output, ExamplePojo.class);
        assertEquals("new", actual.getStatus());
        assertEquals("markymark", actual.getUserName());
        // secret is null because it is not included in the JSON response
        assertNull(actual.getInternalSecret());



        // Username should be the default (blank), since it is not updateable, but displayName will be updated
        pojo.setDisplayName("Mark W");
        pojo.setUserName("markw");
        response = target("/my-hello/updatifyHello")
                .request()
                .header("X-Requested-By", "XmlHttpRequest")
                .post(
                        Entity.json(pojo)
                );
        Assert.assertEquals(200, response.getStatus());
        output = response.readEntity(String.class);
        Log.finer("Updatify response: {0}", output);
        actual = JSON.parse(output, ExamplePojo.class);
        assertEquals("", actual.getUserName());
        assertEquals("Mark W", actual.getDisplayName());
        assertEquals("new", actual.getStatus());
        // secret is null because it is not included in the JSON response
        assertNull(actual.getInternalSecret());

        actual.setStatus("approved");
        response = target("/my-hello/moderatify")
                .request()
                .header("X-Requested-By", "XmlHttpRequest")
                .post(
                        Entity.json(actual)
                );
        Assert.assertEquals(200, response.getStatus());
        output = response.readEntity(String.class);
        Log.finer("Response: {0}", output);
        actual = JSON.parse(output, ExamplePojo.class);
        assertEquals("approved", actual.getStatus());
        assertEquals("theInternalSecret", actual.getInternalSecret());



    }

    @Test
    public void testPatternedEndpoint() throws Exception {
        // Test endpoint with id
        ExamplePojo pojo = new ExamplePojo();
        pojo.setAge(41L);
        Response response = target("/my-hello/123/updatify")
                .request()
                .header("X-Requested-By", "XmlHttpRequest")
                .post(
                        Entity.json(pojo)
                );
        Assert.assertEquals(200, response.getStatus());

        ExamplePojo mypojo = response.readEntity(ExamplePojo.class);
        Assert.assertEquals("123", mypojo.getId().toString());
        Assert.assertEquals(123L, (long)mypojo.getId());
        //Log.finer("Response: {0}", response.getContent());

    }

    @Test
    public void testBodyParam() {
        {
            Response response = target("/my-hello/body-slam")
                    .request()
                    .header("X-Requested-By", "XmlHttpRequest")
                    .post(
                            Entity.json(
                                    map(
                                            val("thingId", 102L)
                                    )
                            )
                    );
            assertEquals(200, response.getStatus());
            Map result = JSON.parseMap(response.readEntity(String.class));
            assertEquals(102, result.get("thingId"));
            assertNull(result.get("secretNote"));
        }
        {
            Response response = target("/my-hello/body-slam")
                    .request()
                    .header("X-Requested-By", "XmlHttpRequest")
                    .post(
                            Entity.json(
                                    map(
                                            val("note", "foobar"),
                                            val("thingId", 102L),
                                            val("score", 23L),
                                            val("approved", false)
                                    )
                            )
                    );
            assertEquals(200, response.getStatus());
            Map result = JSON.parseMap(response.readEntity(String.class));
            assertEquals(102, result.get("thingId"));
            assertEquals("foobar", result.get("note"));
            assertNull(result.get("secretNote"));
        }

        // TODO make validation work
        /*
        {
            Response response = target("/my-hello/body-slam")
                    .request()
                    .header("X-Requested-By", "XmlHttpRequest")
                    .post(
                            Entity.json(
                                    map(
                                            val("thingId", 101L),
                                            val("email", "bademail")

                                    )
                            )
                    );
            assertEquals(400, response.getStatus());

        }


        {
            Response response = target("/my-hello/body-slam")
                    .request()
                    .header("X-Requested-By", "XmlHttpRequest")
                    .post(
                            Entity.json(
                                    map(
                                            val("note", "foobar")

                                    )
                            )
                    );
            assertEquals(400, response.getStatus());

        }
        */
    }


}
