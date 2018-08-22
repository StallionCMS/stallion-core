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

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

import static io.stallion.utils.Literals.*;

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.MainRunner;
import io.stallion.boot.StallionServer;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.Response;


public abstract class JerseyIntegrationBaseCase  {
    private static InnerJerseyTest jerseyTest;
    private static Feature[] features;

    static {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("jersey.config.test.container.factory",
                "org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory");
    }

    public InnerJerseyTest getJerseyTest() {
        return this.jerseyTest;
    }

    public WebTarget target() {
        return this.jerseyTest.target();
    }

    public WebTarget target(String path) {
        return this.jerseyTest.target(path);
    }

    public Client client() {

        return this.jerseyTest.client();
    }

    public void close(Response ...responses) {
        this.jerseyTest.close(responses);
    }


    public Response GET(String path) {
        return target(path)
                .request()
                .header("X-Requested-By", "XmlHttpRequest")
                .get();
    }

    public Response POST(String path, Object entity) {
        return target(path)
                .request()
                .header("X-Requested-By", "XmlHttpRequest")
                .post(
                        Entity.json(entity)
                );
    }


    protected void extraConfigure(ResourceConfig rc) {

    }

    public static void startApp(String folderName) throws Exception {
        startApp(folderName, false, new Feature[0], new StallionJavaPlugin[0]);
    }

    public static void startApp(String folderName,  Feature ...features) throws Exception {
        startApp(folderName, false, features, new StallionJavaPlugin[0]);
    }

    public static void startApp(String folderName,  StallionJavaPlugin ...plugins) throws Exception {
        startApp(folderName, false,  new Feature[0], plugins);
    }


    public static void startApp(String folderName, Boolean watchFolders, Feature[] theFeatures, StallionJavaPlugin ...plugins) throws Exception {
        Log.info("setUpClass client and app");
        Settings.shutdown();
        String path;
        if (new File(folderName).exists()) {
            path = folderName;
        } else {
            URL resourceUrl = AppIntegrationCaseBase.class.
                    getResource(folderName);
            Path resourcePath = Paths.get(resourceUrl.toURI());
            path = resourcePath.toString();
        }
        Log.fine("--------------------------------------------------------------------------------------------------");
        Log.info("Booting app from folder: {0} ", path);
        Log.fine("--------------------------------------------------------------------------------------------------");
        AppContextLoader.loadAndStartForTests(path);
        String level = System.getenv("stallionLogLevel");
        if (!empty(level)) {
            Log.setLogLevel(Level.parse(level.toUpperCase()));
        }
        for(StallionJavaPlugin plugin: plugins) {
            PluginRegistry.instance().loadPluginFromBooter(plugin);
            plugin.boot();
        }

        if (theFeatures != null) {
            features = theFeatures;
        } else {
            features = new Feature[0];
        }
        jerseyTest = new InnerJerseyTest();
        jerseyTest.setUp();


        //client = new TestClient(AppContextLoader.instance());
        Log.fine("--------------------------------------------------------------------------------------------------");
        Log.info("App booted for folder: {0} ", path);
        Log.fine("--------------------------------------------------------------------------------------------------");

    }



    public static class InnerJerseyTest extends JerseyTest {


        @Override
        protected Application configure() {
            enable(TestProperties.LOG_TRAFFIC);
            enable(TestProperties.DUMP_ENTITY);
            enable("trace");


            ResourceConfig rc = new StallionServer().buildResourceConfig();
            if (features != null) {
                for (Feature feature : features) {
                    rc.register(feature);
                }
            }

            return rc;
        }
    }

    private static Map<Class, SelfMocking> mocks = new HashMap<>();

    public static void mockClass(Class<SelfMocking> cls) {
        if (mocks.containsKey(cls)) {
            return;
        }
        try {
            SelfMocking sm = cls.newInstance();
            mocks.put(cls, sm);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public static <T> T getMockResults(Class<SelfMocking> cls) {
        return mocks.get(cls).onSelfMockingGetResults();
    }

    @BeforeClass
    public static void baseBeforeClass() throws Exception {


        /* Uncomment for detailed logging of Jersey matching the request  *
        Logger logger = Logger.getLogger("org.glassfish.jersey.tracing.general");
        logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        // */

        for(SelfMocking sm: mocks.values()) {
            sm.onSelfMockingBeforeClass();
        }
    }



    @Before
    public void baseBefore() throws Exception {
        for(SelfMocking sm: mocks.values()) {
            sm.onSelfMockingBeforeTest();
        }
    }

    @After
    public void baseAfter() throws Exception {
        for(SelfMocking sm: mocks.values()) {
            sm.onSelfMockingAfter();
        }
    }

    @AfterClass
    public static void baseAfterClass() throws Exception {
        features = new Feature[0];
        jerseyTest.tearDown();
        jerseyTest = null;


        for(SelfMocking sm: mocks.values()) {
            sm.onSelfMockingAfterClass();
        }


        cleanUpClass();

    }


    public static void cleanUpClass() {
        AppContextLoader.shutdown();
        Settings.shutdown();
        mocks = new HashMap<>();
        Stubbing.reset();
    }

    public void assertContains(String content, String expected) {
        if (!content.contains(expected)) {
            Log.warn("Content ''{0}'' does not contain string ''{1}''!!", content, expected);
        }
        assert content.contains(expected);
    }

    public void assertNotContains(String content, String unexpected) {
        if (content.contains(unexpected)) {
            Log.warn("Content ''{0}'' erroneously contains string ''{1}''!!", content, unexpected);
        }
        assert !content.contains(unexpected);
    }

    public void assertResponseDoesNotContain(Response response, String content) {
        assertResponseDoesNotContain(response, content, 200);
    }

    public void assertResponseDoesNotContain(Response response, String content, int status) {
        String out = response.readEntity(String.class);
        if (!out.contains(content)) {
            Log.warn("Unexpected string {0} found in response content!!\n{1}\n\n", content, out);
        }
        assert !out.contains(content);
    }



    public void assertResponseSucceeded(Response response) {
        if (response.getStatus() != 200) {
            throw new AssertionError("Response status was: " + response.getStatus() + " Content: " + response.getEntity().toString());
        }
    }
    public void assertResponseContains(Response response, String content) {
        assertResponseContains(response, content, 200);
    }

    public void assertResponseContains(Response response, String content, int status) {
        String out = response.readEntity(String.class);
        if (!out.contains(content)) {
            Log.warn("String {0} not found in response content!!\n{1}\n\n", content, out);
        }
        if (response.getStatus() != status) {
            Log.warn("Bad response status! expected={0} actual={1}", status, response.getStatus());
            assert response.getStatus() == status;
        }
        assert out.contains(content);
    }


}
