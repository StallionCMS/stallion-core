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

package io.stallion.tests.integration.javaPlugin;

import io.stallion.Context;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.services.Log;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;


public class JavaPluginTest extends AppIntegrationCaseBase {


    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/java_plugin_site");

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Delete the existing folders
        File timingRecordFolder = new File(Context.settings().getTargetFolder() + "/timeit/timing-records");
        if (timingRecordFolder.exists()) {
            FileUtils.deleteDirectory(timingRecordFolder);
        }
        File journalFolder = new File(Context.settings().getTargetFolder() + "/timeit/journal-entries");
        if (journalFolder.exists()) {
            FileUtils.deleteDirectory(journalFolder);
        }

        cleanUpClass();
    }



    //@Test
    public void testPluginEndpoints() {
        //TODO: fix this test
        Log.info("testEndpoints");
        throw new NotImplementedException("uncomment and fix");
        /*
        MockResponse response = client.get("/_stx/timeit/speed/my-key1");
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("ThisIsACustomPerfGETEndpoint Key: my-key1", response.getContent());

        response = client.post("/_stx/timeit/speed/my-key2", new HashMap());
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("ThisIsACustomPerfPOSTEndpoint Key: my-key2", response.getContent());
*/
    }
}
