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

package io.stallion.utils;

import io.stallion.exceptions.UsageException;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.plugins.PluginRegistry;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.apache.commons.io.IOUtils;
import org.parboiled.common.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static io.stallion.utils.Literals.*;


public class ResourceHelpers {

    public static String loadAssetResource(String pluginName, String path) throws IOException {
        if (path.contains("..") || (!path.startsWith("/assets/") && path.startsWith("assets"))) {
            throw new UsageException("Invalid path: " + path);
        }
        return loadResource(pluginName, path);
    }

    public static String loadTemplateResource(String pluginName, String path) throws IOException {
        if (path.contains("..") || (!path.startsWith("/assets/") && path.startsWith("assets"))) {
            throw new UsageException("Invalid path: " + path);
        }
        return loadResource(pluginName, path);
    }


    public static String loadResource(String pluginName, String path) {
        try {
            URL url = pluginPathToUrl(pluginName, path);
            if (url == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            return loadResourceFromUrl(url, pluginName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static URL pluginPathToUrl(String pluginName, String path) throws FileNotFoundException {
        URL url = null;
        if ("stallion".equals(pluginName) || empty(pluginName)) {
            url = ResourceHelpers.class.getResource(path);
        } else {
            StallionJavaPlugin booter = PluginRegistry.instance().getJavaPluginByName().get(pluginName);
            if (booter == null) {
                throw new FileNotFoundException("No plugin found: " + pluginName);
            }
            url = booter.getClass().getResource(path);
        }
        return url;
    }

    public static byte[] loadBinaryResource(String pluginName, String path) {
        try {
            URL url = pluginPathToUrl(pluginName, path);
            if (url == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            return loadBinaryResource(url, pluginName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] loadBinaryResource(URL resourceUrl, String pluginName)  {

        try {
            File file = urlToFileMaybe(resourceUrl, pluginName);
            if (file == null) {
                return IOUtils.toByteArray(resourceUrl.openStream());
            } else {
                return FileUtils.readAllBytes(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String loadResourceFromUrl(URL resourceUrl) throws IOException {
        return loadResourceFromUrl(resourceUrl, "");
    }

    public static String loadResourceFromUrl(URL resourceUrl, String pluginName) throws IOException {
        File file = urlToFileMaybe(resourceUrl, pluginName) ;
        if (file != null) {
            return FileUtils.readAllText(file, Charset.forName("UTF-8"));
        } else {
            return IOUtils.toString(resourceUrl);

        }
    }

    private static File urlToFileMaybe(URL resourceUrl, String pluginName) {
        Log.finer("Load resource URL={0} plugin={1}", resourceUrl, pluginName);
        if (!Settings.instance().getDevMode()) {
            return null;
        }

        // If the resourceUrl points to a local file, and the file exists in the file system, then use that file
        Log.finer("Resource URL  {0}", resourceUrl);
        if (resourceUrl.toString().startsWith("file:/")) {
            String path = resourceUrl.toString().substring(5).replace("/target/classes/", "/src/main/resources/");
            File file = new File(path);
            if (file.isFile()) {
                Log.finest("Load resource from source path {0}", path);
                return file;
            }
        }

        String[] parts = resourceUrl.toString().split("!", 2);
        if (parts.length < 2) {
            return null;
        }
        String relativePath = parts[1];
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        if (relativePath.contains("..")) {
            throw new UsageException("Invalid characters in the URL path: " + resourceUrl.toString());
        }

        List<String> paths = list();
        if (empty(pluginName) || "stallion".equals(pluginName)) {
            paths.add(System.getProperty("user.home") + "/st/core/src/main/resources" + relativePath);
            paths.add(System.getProperty("user.home") + "/stallion/core/src/main/resources" + relativePath);
        } else {
            paths.add(System.getProperty("user.home") + "/st/plugins/" + pluginName + "/src/main/resources" + relativePath);
            paths.add(System.getProperty("user.home") + "/stallion/plugins/" + pluginName + "/src/main/resources" + relativePath);
        }
        boolean isText = true;

        for (String path: paths) {
            File file = new File(path);
            if (file.isFile()) {
                Log.finest("Load resource from guessed path {0}", path);
                return file;
            }
        }

        // All else fails, just return it based on the resourc
        Log.finest("Could not find a devMode version for resource path {0}", resourceUrl.toString());
        return null;

    }
}
