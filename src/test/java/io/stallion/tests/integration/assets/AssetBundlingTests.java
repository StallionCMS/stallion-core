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


import io.stallion.assets.FileSystemAssetBundleRenderer;
import io.stallion.assets.ResourceAssetBundleRenderer;
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
        FileSystemAssetBundleRenderer br;
        String html;
        String content;

        // Test site.bundle.css

        br = new FileSystemAssetBundleRenderer("site.bundle.css");

        html = br.renderDebugHtml();
        assertContains(html, "<link rel=\"stylesheet\" href=\"http://localhost:8090/testing/st-assets/site.bundle.css?isBundleFile=true&bundleFilePath=%2Fpure-min.css.css&ts=");
        assertContains(html, "<link rel=\"stylesheet\" href=\"http://localhost:8090/testing/st-assets/site.bundle.css?isBundleFile=true&bundleFilePath=%2Fcustom.css.css&ts=");

        html = br.renderProductionHtml();
        assertContains(html, "site.bundle.css?");

        content = br.renderFile("site.css.css");
        assertContains(content, "body.a-minimal-site{color:#333;");
        assertNotContains(content, "body.custom-css{color:#333;}");

        content = br.renderProductionContent();
        assertContains(content, "body.a-minimal-site{color:#333;");
        assertContains(content, "body.custom-css{color:#333;}");
        assertNotContains(content, "console.log('a_minimal_site:site.js')");


        // Test site.bundle.js
        br = new FileSystemAssetBundleRenderer("site.bundle.js");

        html = br.renderDebugHtml();
        assertContains(html, "<script src=\"http://localhost:8090/testing/st-assets/site.bundle.js?isBundleFile=true&bundleFilePath=%2Fsite.js.js&ts=");

        content = br.renderProductionContent();
        assertNotContains(content, "body.a-minimal-site{color:#333;");
        assertContains(content, "console.log('a_minimal_site:site.js')");

    }

    @Test
    public void testResourceBundle() {
        ResourceAssetBundleRenderer br;
        String html;
        String content;

        br = new ResourceAssetBundleRenderer("stallion", "stallion-default-assets.bundle.css");
        html = br.renderDebugHtml();
        assertContains(html, "<link rel=\"stylesheet\" href=\"http://localhost:8090/testing/st-resource/stallion/vendor/grids-responsive-min.css.css?bundlePath=%2Fassets%2Fstallion-default-assets.bundle&ts=");
        assertContains(html, "<link rel=\"stylesheet\" href=\"http://localhost:8090/testing/st-resource/stallion/basic/stallion.css.css?bundlePath=%2Fassets%2Fstallion-default-assets.bundle&ts=");
        assertNotContains(html, "jquery");





    }

    @Test
    public void fullTestWithEndpoints() {
        MockResponse response;


        response = client.get("/an-assetful-page");
        assertEquals(200, response.getStatus());
        Log.finer("Page content: {0}", response.getContent());

        assertContains(response.getContent(), "<link rel=\"stylesheet\" href=\"http://localhost:8090/testing/st-assets/site.bundle.css?isBundleFile=true&bundleFilePath=%2Fpure-min.css.css&ts=");
        //assertContains(response.getContent(), "/st-resource/stallion/always/stallion.js");



        response = client.get("/st-assets/site.bundle.css?isBundleFile=true&bundleFilePath=%2Fpure-min.css.css&ts=sdf");
        Log.finer("Local asset content: {0}", response.getContent());
        assertEquals(200, response.getStatus());
        assertTrue(response.getContent().contains("Pure v0.5.0"));

    }

}

