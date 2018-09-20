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

package io.stallion.assets;


import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.stallion.Context;
import io.stallion.exceptions.UsageException;
import io.stallion.settings.Settings;
import io.stallion.utils.ResourceHelpers;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;

import static io.stallion.utils.Literals.empty;

/**
 * Manages assets that can be included on web pages. Takes care of cache-busting URL's
 * and asset bundling
 */
public class AssetsController {

    private static AssetsController _instance;

    public static AssetsController instance() {
        if (_instance == null) {
            throw new UsageException("Must call load() before accessing the AssetsController instance");
        }
        return _instance;
    }

    public static String ensureSafeAssetsPath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.startsWith("/assets/")) {
            path = "/assets" + path;
        }
        if (path.contains("..")) {
            throw new UsageException("Invalid asset path, illegal characters: " + path);
        }
        return path;
    }

    /**
     * Get a wrapper for the asset controller with limited methods. This is used by
     * the template context and other sandboxed situations.
     *
     * @return
     */
    public static AssetsControllerSafeWrapper wrapper() {
        return instance().getWrapper();
    }



    public static AssetsController load() {
        _instance = new AssetsController();
        if (new File(Settings.instance().getTargetFolder() + "/assets").isDirectory()) {
            /*
            FileSystemWatcherService.instance().registerWatcher(
                    new AssetFileChangeEventHandler()
                            .setWatchedFolder(Settings.instance().getTargetFolder() + "/assets")
                            .setWatchTree(true)
            );*/
        }
        // Load the pre-processors;
        //ExternalCommandPreProcessorRegistry.instance();
        return _instance;
    }

    public static void shutdown() {
        _instance = null;
    }

    private AssetsControllerSafeWrapper wrapper;


    private static HashMap<String, Long> timeStampByPath = new HashMap<String, Long>();

    public static HashMap<String, Long> getTimeStampByPath() {
        return timeStampByPath;
    }

    public static void setTimeStampByPath(HashMap<String, Long> timeStampByPath) {
        AssetsController.timeStampByPath = timeStampByPath;
    }

    /**
     * Loads a resource from the main stallion jar.
     *
     * @param path
     * @return
     */
    public String resource(String path) {
        return resource(path, "stallion");
    }

    public String resource(String path, String plugin) {
        return resource(path, plugin, "");
    }

    /**
     * Get the URL to access an asset file that is bundled in the jar as a resource.
     *
     * @param path - the path, relative to the assets folder in side the resource directory
     * @param plugin - the plugin from which you are loading the resource, use "stallion" to load from the main jar
     * @param developerUrl - an alternative URL to use in development mode. Useful if you want to point
     *                     you local nginx directly at the file, so you can see your changes without recompiling.
     * @return
     */
    public String resource(String path, String plugin, String developerUrl) {
        if (Context.getSettings().getDevMode() && !empty(developerUrl)) {
            return developerUrl;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return Context.settings().getCdnUrl() + "/st-resource/" + plugin + "/" + path;
    }


    /**
     * Gets the resource file and returns it at the string for direct writing into the HTML
     * of the page.
     *
     * @param path
     * @param plugin
     * @return
     */
    public String writeResource(String path, String plugin) throws IOException {
        return writeResource(path, plugin, false);
    }

    public String writeResource(String path, String plugin, boolean base64encode) throws IOException {
        if (path.contains("..")) {
            throw new UsageException("Invalid path.");
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.startsWith("/assets/")) {
            path = "/assets" + path;
        }
        if (base64encode) {
            return Base64.encode(ResourceHelpers.loadBinaryResource(plugin, path));
        } else {
            return ResourceHelpers.loadAssetResource(plugin, path);
        }
    }



    public String bundle(String plugin, String path) {
        if (Settings.instance().getBundleDebug()) {
            return new ResourceAssetBundleRenderer(plugin, path).renderDebugHtml();
        } else {
            return new ResourceAssetBundleRenderer(plugin, path).renderProductionHtml();
        }
    }


    /**
     * Output the HTML required to render a bundle of assets.
     * *
     * @param fileName
     * @return
     */
    public String bundle(String fileName) {
        if (Settings.instance().getBundleDebug()) {
            return new FileSystemAssetBundleRenderer(fileName).renderDebugHtml();
        } else {
            return new FileSystemAssetBundleRenderer(fileName).renderProductionHtml();
        }
    }


    /**
     * Get the URL for an asset file, with a timestamp added for cache busting.
     * @param path
     * @return
     */
    public String url(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String url = Context.settings().getCdnUrl() + "/st-assets/" + path;
        if (url.contains("?")) {
            url = url + "&";
        } else {
            url = url + "?";
        }
        url = url + "ts=" +  getTimeStampForAssetFile(path).toString();
        return url;
    }



    public Long getTimeStampForAssetFile(String path) {
        String filePath = Context.settings().getTargetFolder() + "/assets/" + path;
        if (getTimeStampByPath().containsKey(filePath)) {
            Long ts = getTimeStampByPath().get(filePath);
            if (ts > 0) {
                return ts;
            }
        }

        Path pathObj = FileSystems.getDefault().getPath(filePath);
        File file = new File(filePath);
        Long ts = file.lastModified();
        getTimeStampByPath().put(filePath, ts);
        return ts;
    }

    public Long getCurrentTimeStampForAssetFile(String path) {
        String filePath = Context.settings().getTargetFolder() + "/assets/" + path;
        Path pathObj = FileSystems.getDefault().getPath(filePath);
        File file = new File(filePath);
        Long ts = file.lastModified();
        return ts;
    }


    private String getKeyStringForPathSource(String path, String source) {
        Long ts = getCurrentTimeStampForAssetFile(path);
        if (ts == 0 || ts == null) {
            return DigestUtils.md5Hex(source);
        } else {
            return ts.toString();
        }
    }


    public AssetsControllerSafeWrapper getWrapper() {
        if (wrapper == null) {
            wrapper = new AssetsControllerSafeWrapper(this);
        }
        return wrapper;
    }

}
