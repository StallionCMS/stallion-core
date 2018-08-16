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

package io.stallion.tests.integration.aMinimalSite;

import io.stallion.Context;
import static io.stallion.Context.*;
import static org.junit.Assert.assertEquals;

import io.stallion.dataAccess.file.TextFilePersister;
import io.stallion.dataAccess.file.TextItem;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.JerseyIntegrationBaseCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public class AMinimalSiteTest extends JerseyIntegrationBaseCase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/a_minimal_site");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        cleanUpClass();
    }

    @Test
    public void testSettings() {
        assertEquals("http://localhost:8090", Context.settings().getSiteUrl());
        assertEquals("http://localhost:8090/testing", Context.settings().getCdnUrl());
        assertEquals("testoverriden", Context.settings().getCustom().get("foobar"));
        assertEquals("notoverriden", Context.settings().getCustom().get("bar"));
        assertEquals("noop", getSettings().getEmail().getUsername());
        assertEquals("overridePassword", getSettings().getEmail().getPassword());
        assertEquals("The default page title", getSettings().getDefaultTitle());
        assertEquals("The quite overriden site name", getSettings().getSiteName());

    }


    @Test
    public void testOnePage()
    {
        {
            Response response = GET("/faq");
            assertContains(response.readEntity(String.class), "<h1>Frequently Asked Questions</h1>");
        }

        {
            Response response = GET("/");
            assertContains(response.readEntity(String.class), "<h1>The home.jinja template</h1>");
        }

    }


    @Test
    public void testJinjaPage()
    {
        {
            Response response = GET("/jinja-page");
            assertEquals(200, response.getStatus());
            String output = response.readEntity(String.class);
            assertContains(output, "<h1>This is a base template using jinja</h1>");
            assertContains(output, "This is a page written by: Maestro");
            assertContains(output, "This is a page written using jinja. This is the page content.");
        }

    }

    @Test
    public void testErrors() {

        {
            Response response = GET("/non-existent-page");
            assertContains(response.readEntity(String.class), "This is a custom 404 template");
            assertEquals(response.getStatus(), 404);
        }

        {
            Response response = GET("/another-non-existent-page");
            assertContains(response.readEntity(String.class), "This is a custom 404 template");
            assertEquals(response.getStatus(), 404);
        }


        {
            Response response = GET("/exception-generating-page");
            assertEquals(500, response.getStatus());
            assertContains(response.readEntity(String.class), "There was an error trying to handle your request");
        }



        /*
        MockResponse response = client.get("/non-existent-page");
        Log.finer("404 result: {0}", response.getContent());
        Assert.assertTrue(response.getContent().contains("This is a custom 404 template"));
        assertEquals(404, response.getStatus());


        response = client.get("/exception-generating-page");
        Log.finer("500 result: {0}", response.getContent());
        assertEquals(500, response.getStatus());
        Assert.assertTrue(response.getContent().contains("There was an error trying to handle your request"));
        */
    }


    @Test
    public void testStaticAsset()
    {
        Response response = target("/st-assets/bootstrap.css")
                .queryParam("ts", "1406396040000")
                .request()
                .get()
                ;
        assertEquals(200, response.getStatus());
        assertContains(response.readEntity(String.class), "Bootstrap v3.1.1");


    }

    @Test
    public void testMarkdown() throws IOException {
        String page = IOUtils.toString(getClass().getResource("/text_files/busted-page.txt"), "UTF-8");
        TextFilePersister persister = new TextFilePersister();
        persister.setModelClass(TextItem.class);
        TextItem item = persister.fromString(page, Paths.get("/naming-things.txt"));
        assertEquals("/how-to-name-variables", item.getSlug());
        // publishDate:2013-12-12 11:30:00
        ZonedDateTime dt = ZonedDateTime.of(2013, 12, 12, 11, 30, 0, 0, ZoneId.of("America/New_York"));
        assertEquals(dt, item.getPublishDate());
    }
}
