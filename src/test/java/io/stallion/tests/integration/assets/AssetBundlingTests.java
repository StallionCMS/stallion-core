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

package io.stallion.tests.integration.assets;

import io.stallion.Context;
import io.stallion.assets.BundleHandler;

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
    public void testLocalAssetsBundle() {
        BundleHandler bundle = new BundleHandler("site-local.bundle.css");
        bundle.setFileContents(LOCAL_ASSET_BUNDLE);

        String debugHtml = bundle.toDebugHtml();

        Log.finer("Debug HTML: {0}", debugHtml);
        assertContains(debugHtml, "<link rel=\"stylesheet\" href=\"http://localhost:8090/testing/st-assets/pure-min.css?vstring=");
        assertContains(debugHtml, "<link rel=\"stylesheet\" href=\"http://localhost:8090/testing/st-assets/nested/nested.js?vstring=");

        String liveHtml = bundle.toLiveHtml();
        Log.finer("Live HTML: {0}", liveHtml);
        assertContains(liveHtml, "<link rel=\"stylesheet\" href=\"http://localhost:8090/testing/st-assets/site-local.bundle.css?stBundle=standard&ts=");


        String liveContent = bundle.toConcatenatedContent();
        Log.finer("Live Content: {0}", liveContent);
        assertContains(liveContent, "Pure v0.5.0");
        assertTrue(StringUtils.split(liveContent, "\n").length< 12);
    }

    @Test
    public void testExternalBundle() {
        BundleHandler bundle = new BundleHandler("site-external.bundle.js");
        bundle.setFileContents(EXTERNAL_BUNDLE);

        String debugHtml = bundle.toDebugHtml();

        Log.finer("Debug HTML: {0}", debugHtml);

        assertContains(debugHtml, "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js?vstring=");
        assertTrue(!debugHtml.contains("will-be-skipped.js"));

        String liveHtml = bundle.toLiveHtml();
        Log.finer("Live HTML: {0}", liveHtml);
        assertContains(liveHtml, "<script src=\"http://localhost:8090/testing/st-assets/site-external.bundle.js?stBundle=standard&ts=");


        String liveContent = bundle.toConcatenatedContent();
        Log.finer("Live Content: {0}", liveContent);
        assertContains(liveContent, "/*! jQuery v1.11.2");
        assertTrue(StringUtils.split(liveContent, "\n").length< 7);

    }

    @Test
    public void testResourceBundle() {
        BundleHandler bundle = new BundleHandler("site-resource.bundle.js");
        bundle.setFileContents(RESOURCE_BUNDLE);


        Context.getSettings().setDevMode(true);
        String devHtml = bundle.toDebugHtml();
        assertContains(devHtml, "src=\"http://local.stallion.io/core-resources/stallion.js?vstring=");


        Context.getSettings().setDevMode(false);
        String debugHtml = bundle.toDebugHtml();
        assertContains(debugHtml, "src=\"http://localhost:8090/testing/st-resource/stallion/always/stallion.js?vstring=");


        String liveHtml = bundle.toLiveHtml();
        assertContains(liveHtml, "<script src=\"http://localhost:8090/testing/st-assets/site-resource.bundle.js?stBundle=standard&ts=");


        String liveContent = bundle.toConcatenatedContent();
        assertContains(liveContent, "Stallion common JS library");

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

