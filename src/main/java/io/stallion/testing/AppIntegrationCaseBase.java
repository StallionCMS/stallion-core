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

package io.stallion.testing;

import io.stallion.boot.AppContextLoader;

import io.stallion.services.Log;
import io.stallion.settings.Settings;

import org.junit.AfterClass;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import static io.stallion.utils.Literals.empty;


public abstract class AppIntegrationCaseBase {

    public static TestClient client;

    public static void startApp(String folderName) throws Exception {
        startApp(folderName, false);
    }

    public static void startApp(String folderName, Boolean watchFolders) throws Exception {
        Log.info("setUpClass client and app");
        Settings.shutdown();
        URL resourceUrl = AppIntegrationCaseBase.class.
                getResource(folderName);
        Path resourcePath = Paths.get(resourceUrl.toURI());
        String path = resourcePath.toString();
        Log.fine("--------------------------------------------------------------------------------------------------");
        Log.info("Booting app from folder: {0} ", path);
        Log.fine("--------------------------------------------------------------------------------------------------");
        AppContextLoader.loadAndStartForTests(path);
        String level = System.getenv("stallionLogLevel");
        if (!empty(level)) {
            Log.setLogLevel(Level.parse(level.toUpperCase()));
        }
        client = new TestClient(AppContextLoader.instance());
        Log.fine("--------------------------------------------------------------------------------------------------");
        Log.info("App booted for folder: {0} ", path);
        Log.fine("--------------------------------------------------------------------------------------------------");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        cleanUpClass();
    }


    public static void cleanUpClass() {
        AppContextLoader.shutdown();
        Settings.shutdown();
        client = null;
        Stubbing.reset();
    }

    public void assertContains(String content, String expected) {
        if (!content.contains(expected)) {
            Log.warn("Content ''{0}'' does not contain string ''{1}''!!", content, expected);
        }
        assert content.contains(expected);
    }

    public void assertResponseDoesNotContain(MockResponse response, String content) {
        assertResponseDoesNotContain(response, content, 200);
    }

    public void assertResponseDoesNotContain(MockResponse response, String content, int status) {
        if (response.getContent().contains(content)) {
            Log.warn("Unexpected string {0} found in response content!!\n{1}\n\n", content, response.getContent());
        }
        assert !response.getContent().contains(content);
    }

    public void assertResponseContains(MockResponse response, String content) {
        assertResponseContains(response, content, 200);
    }

    public void assertResponseSucceeded(MockResponse response) {
        if (response.getStatus() != 200) {
            throw new AssertionError("Response status was: " + response.getStatus() + " Content: " + response.getContent());
        }
    }

    public void assertResponseContains(MockResponse response, String content, int status) {
        if (!response.getContent().contains(content)) {
            Log.warn("String {0} not found in response content!!\n{1}\n\n", content, response.getContent());
        }
        if (response.getStatus() != status) {
            Log.warn("Bad response status! expected={0} actual={1}", status, response.getStatus());
            assert response.getStatus() == status;
        }
        assert response.getContent().contains(content);
    }

}
