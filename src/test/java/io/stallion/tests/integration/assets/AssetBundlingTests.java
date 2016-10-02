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

package io.stallion.tests.integration.assets;

import io.stallion.Context;


import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.MockResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class AssetBundlingTests extends AppIntegrationCaseBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/a_minimal_site");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        cleanUpClass();
    }

    @Test
    public void testFileAssetBundle() {

    }

    @Test
    public void testResourceBundle() {


    }

    @Test
    public void fullTestWithEndpoints() {
        MockResponse response;


        response = client.get("/an-assetful-page");
        assertEquals(200, response.getStatus());
        Log.finer("Page content: {0}", response.getContent());

        assertContains(response.getContent(), "/st-resource/stallion/always/stallion.js");
        assertContains(response.getContent(), "/st-assets/pure-min.css");


        response = client.get("/st-assets/pure.css");
        Log.finer("Local asset content: {0}", response.getContent());
        assertEquals(200, response.getStatus());
        // TODO fix this:
        //assertTrue(response.getContent().contains("Pure v0.5.0"));

        response = client.get("/st-resource/stallion/always/stallion.js");
        Log.finer("Resource asset content: {0}", response.getContent());
        assertResponseContains(response, "Stallion common JS library");


        response = client.get("/st-assets/site.head.bundle.js?stBundle=standard");
        Log.finer("Bundle content: {0}", response.getContent());
        assertEquals(200, response.getStatus());



        // request the page
        // request the resource asset directly
        // request the local asset directly
        // request the bundle directly
    }

    private static final String LOCAL_ASSET_BUNDLE = "//=pure-min.css|pure.css\n" +
            "//=nested/nested.js";
    private static final String EXTERNAL_BUNDLE = "//=key:jquery|https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js|https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.js\n" +
            "//=key:jquery|will-be-skipped.js";
    private static final String RESOURCE_BUNDLE = "//=resource:stallion|always/stallion.js|always/stallion.js|http://local.stallion.io/core-resources/stallion.js";


}

