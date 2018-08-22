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

package io.stallion.utils;


import io.stallion.exceptions.UsageException;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.parboiled.common.FileUtils;

import javax.ws.rs.NotFoundException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.list;


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

    public static boolean resourceExists(String pluginName, String path) {
        URL url = null;
        try {
            url = pluginPathToUrl(pluginName, path);
        } catch (FileNotFoundException e) {
            return false;
        }
        if (url == null) {
            return false;
        } else {
            return true;
        }
    }

    public static URL getUrlOrNotFound(String pluginName, String path) {
        try {
            URL url = pluginPathToUrl(pluginName, path);
            if (url == null) {
                throw new NotFoundException("Resource not found: " + pluginName + ":"  + path);
            }
            return url;
        } catch(FileNotFoundException e) {
            throw new NotFoundException("Resource not found: " + pluginName + ":"  + path);
        }
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

    public static List<String> listFilesInDirectory(String plugin, String path) {
        String ending = "";
        String starting = "";
        if (path.contains("*")) {
            String[] parts = StringUtils.split(path, "*", 2);
            String base = parts[0];
            if (!base.endsWith("/")) {
                path = new File(base).getParent();
                starting = FilenameUtils.getName(base);
            } else {
                path = base;
            }
            ending = parts[1];
        }

        Log.info("listFilesInDirectory Parsed Path {0} starting:{1} ending:{2}", path, starting, ending);
        URL url = PluginRegistry.instance().getJavaPluginByName().get(plugin).getClass().getResource(path);
        Log.info("URL: {0}", url);

        List<String> filenames = new ArrayList<>();
        URL dirURL = getClassForPlugin(plugin).getResource(path);
        Log.info("Dir URL is {0}", dirURL);
        // Handle file based resource folder
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            String fullPath = dirURL.toString().substring(5);
            File dir = new File(fullPath);
            // In devMode, use the source resource folder, rather than the compiled version
            if (Settings.instance().getDevMode()) {
                String devPath = fullPath.replace("/target/classes/", "/src/main/resources/");
                File devFolder = new File(devPath);
                if (devFolder.exists()){
                    dir = devFolder;
                }
            }
            Log.info("List files from folder {0}", dir.getAbsolutePath());
            List<String> files = list();
            for (String name: dir.list()) {
                if (!empty(ending) && !name.endsWith(ending)) {
                    continue;
                }
                if (!empty(starting) && !name.endsWith("starting")) {
                    continue;
                }
                // Skip special files, hidden files
                if (name.startsWith(".") || name.startsWith("~") || name.startsWith("#") || name.contains("_flymake.")) {
                    continue;
                }
                filenames.add(path + name);
            }
            return filenames;
        }


        if (dirURL.getProtocol().equals("jar")) {
        /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = null;
            try {
                jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                Log.finer("Jar file entry: {0}", name);
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    if (!empty(ending) && !name.endsWith(ending)) {
                        continue;
                    }
                    if (!empty(starting) && !name.endsWith("starting")) {
                        continue;
                    }
                    // Skip special files, hidden files
                    if (name.startsWith(".") || name.startsWith("~") || name.startsWith("#") || name.contains("_flymake.")) {
                        continue;
                    }
                    result.add(entry);
                }
            }
            return new ArrayList<>(result);
        }
        throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
        /*
        try {
            URL url1 = getClassForPlugin(plugin).getResource(path);
            Log.info("URL1 {0}", url1);
            if (url1 != null) {
                Log.info("From class folder contents {0}", IOUtils.toString(url1));
                Log.info("From class folder contents as stream {0}", IOUtils.toString(getClassForPlugin(plugin).getResourceAsStream(path)));
            }
            URL url2 = getClassLoaderForPlugin(plugin).getResource(path);
            Log.info("URL1 {0}", url2);
            if (url2 != null) {
                Log.info("From classLoader folder contents {0}", IOUtils.toString(url2));
                Log.info("From classLoader folder contents as stream {0}", IOUtils.toString(getClassLoaderForPlugin(plugin).getResourceAsStream(path)));
            }

        } catch (IOException e) {
            Log.exception(e, "error loading path " + path);
        }
        //  Handle jar based resource folder
        try(
                InputStream in = getResourceAsStream(plugin, path);
                BufferedReader br = new BufferedReader( new InputStreamReader( in ) ) ) {
            String resource;
            while( (resource = br.readLine()) != null ) {
                Log.finer("checking resource for inclusion in directory scan: {0}", resource);
                if (!empty(ending) && !resource.endsWith(ending)) {
                    continue;
                }
                if (!empty(starting) && !resource.endsWith("starting")) {
                    continue;
                }
                // Skip special files, hidden files
                if (resource.startsWith(".") || resource.startsWith("~") || resource.startsWith("#") || resource.contains("_flymake.")) {
                    continue;
                }
                Log.finer("added resource during directory scan: {0}", resource);
                filenames.add(path + resource);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return filenames;
        */
    }

    private static InputStream getResourceAsStream(String plugin, String resource ) {

        if (empty(plugin) || plugin.equals("stallion")) {
            return ResourceHelpers.class.getClassLoader().getResourceAsStream(resource);
        } else {
            return  PluginRegistry.instance().getJavaPluginByName().get(plugin).getClass().getClassLoader().getResourceAsStream(resource);
        }
    }

    private static ClassLoader getClassLoaderForPlugin(String plugin) {
        if (empty(plugin) || "stallion".equals(plugin)) {
            return ResourceHelpers.class.getClassLoader();
        } else {
            return PluginRegistry.instance().getJavaPluginByName().get(plugin).getClass().getClassLoader();
        }
    }

    private static Class getClassForPlugin(String plugin) {
        if (empty(plugin) || "stallion".equals(plugin)) {
            return ResourceHelpers.class;
        } else {
            return PluginRegistry.instance().getJavaPluginByName().get(plugin).getClass();
        }
    }


    public static URL pluginPathToUrl(String pluginName, String path) throws FileNotFoundException {
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
            return IOUtils.toString(resourceUrl, Charset.forName("UTF-8"));

        }
    }

    public static File findDevModeFileForResource(String plugin, String resourcePath) {
        if (!Settings.instance().getDevMode()) {
            throw new UsageException("You can only call this method in dev mode!");
        }
        try {
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            // We find the URL of the root, not the actual file, since if the file was just created, we want it to be
            // accessible even if mvn hasn't recompiled the project. This allows us to iterate more quickly.
            URL url = pluginPathToUrl(plugin, resourcePath);
            // Maybe the file was just created? We'll try to find the root folder path, and then add the file path
            if (url == null) {
                url = new URL(StringUtils.stripStart(url.toString(), "/") + resourcePath);
            }
            return urlToFileMaybe(url, plugin);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            paths.add(System.getProperty("user.home") + "/st/" + pluginName + "/src/main/resources" + relativePath);
            paths.add(System.getProperty("user.home") + "/stallion/" + pluginName + "/src/main/resources" + relativePath);
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
