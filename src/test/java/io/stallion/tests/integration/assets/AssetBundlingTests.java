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
import io.stallion.testing.JerseyIntegrationBaseCase;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;


public class AssetBundlingTests extends JerseyIntegrationBaseCase {

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
        assertContains(html, "<link rel=\"stylesheet\" href=\"http://localhost:8090/st-assets/site.bundle.css?isBundleFile=true&bundleFilePath=%2Fpure-min.css.css&ts=");
        assertContains(html, "<link rel=\"stylesheet\" href=\"http://localhost:8090/st-assets/site.bundle.css?isBundleFile=true&bundleFilePath=%2Fcustom.css.css&ts=");

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
        assertContains(html, "<script src=\"http://localhost:8090/st-assets/site.bundle.js?isBundleFile=true&bundleFilePath=%2Fsite.js.js&ts=");

        content = br.renderProductionContent();
        assertNotContains(content, "body.a-minimal-site{color:#333;");
        assertContains(content, "console.log('a_minimal_site:site.js')");

    }

    @Test
    public void testResourceBundle() {
        ResourceAssetBundleRenderer br;
        String html;
        String content;

        br = new ResourceAssetBundleRenderer("stallion", "testing.bundle.css");
        html = br.renderDebugHtml();
        assertContains(html, "<link rel=\"stylesheet\" href=\"http://localhost:8090/st-resource/stallion/css/one.css.css?bundlePath=%2Fassets%2Ftesting.bundle&ts=");
        assertContains(html, "<link rel=\"stylesheet\" href=\"http://localhost:8090/st-resource/stallion/css/second.css.css?bundlePath=%2Fassets%2Ftesting.bundle&ts=");
        assertNotContains(html, "jquery");





    }

    @Test
    public void fullTestWithEndpoints() {


        String expectedUrl = "http://localhost:8090/st-assets/site.bundle.css?isBundleFile=true&bundleFilePath=%2Fpure-min.css.css&ts=";

        {
            Response response = GET("/an-assetful-page");
            assertEquals(200, response.getStatus());
            String content = response.readEntity(String.class);

            assertContains(content, "<link rel=\"stylesheet\" href=\"" + expectedUrl + "");
        }

        {
            Response response = target("/st-assets/site.bundle.css")
                    .queryParam("isBundleFile", "true")
                    .queryParam("bundleFilePath", "/pure-min.css.css")
                    .queryParam("ts", "123")
                    .request()
                    .get();
            String content = response.readEntity(String.class);
            assertEquals(200, response.getStatus());
            assertTrue(content.contains("Pure v0.5.0"));
        }

    }

}

