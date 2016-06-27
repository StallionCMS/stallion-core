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

package io.stallion.assets;



import io.stallion.Context;
import io.stallion.assets.processors.AngularCompiler;
import io.stallion.assets.processors.ReactJsxCompiler;
import io.stallion.assets.processors.RiotCompiler;
import io.stallion.exceptions.UsageException;
import io.stallion.fileSystem.FileSystemWatcherService;
import io.stallion.services.Log;
import io.stallion.services.PermaCache;
import io.stallion.settings.Settings;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

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
            FileSystemWatcherService.instance().registerWatcher(
                    new AssetFileChangeEventHandler()
                            .setWatchedFolder(Settings.instance().getTargetFolder() + "/assets")
                            .setWatchTree(true)
            );
        }
        // Load the pre-processors;
        ExternalCommandPreProcessorRegistry.instance();
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
     * Turn a list of additional strings that should be in the Footer section of the
     * page and return as a string
     *
     * @return
     */
    public String pageFooterLiterals() {
        return Context.getResponse().getPageFooterLiterals().stringify();
    }

    /**
     * Turn a list of additional strings that should be in the HEAD section of the
     * page and return as a string
     *
     * @return
     */
    public String pageHeadLiterals() {
        return Context.getResponse().getPageHeadLiterals().stringify();
    }

    /**
     * Get the HTML for a named bundle that is built manually via code, (rather than from
     * a bundle file).
     *
     * @param bundleName
     * @return
     */
    public String definedBundle(String bundleName) {
        return new BundleHandler(DefinedBundle.getByName(bundleName)).toHtml();
    }

    /**
     * Output the HTML required to render a bundle of assets.
     * *
     * @param fileName
     * @return
     */
    public String bundle(String fileName) {
        return new BundleHandler(fileName, "").toHtml();
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

    /**
     * Runs
     * @param preProcessorName
     * @param path
     */
    public void externalPreprocessIfNecessary(String preProcessorName, String path) {
        // Only pre-process if we are in bundle debug mode and local mode
        if (!settings().getBundleDebug() || !settings().getLocalMode()) {
            return;
        }
        ExternalCommandPreProcessorRegistry.instance().preProcessIfNeeded(preProcessorName, path);
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

    /**
     * Converts the source content using the named processor. Useful for converting
     * react.jsx files into javascript, etc.
     *
     * @param processor
     * @param path
     * @param source
     * @return
     */
    public String convertUsingProcessor(String processor, String path, String source) {
        String cacheKey = "convertsource--" + processor + "--" + GeneralUtils.slugify(path) + "-ts--" + getKeyStringForPathSource(path, source);
        String result = PermaCache.get(cacheKey);
        if (result != null) {
            Log.info("Cache hit for {0}", cacheKey);
            return result;
        }
        Log.info("Cache miss for {0}", cacheKey);
        result = convertUsingProcessorNoCache(processor, path, source);
        PermaCache.set(cacheKey, result);
        return result;
    }

    private String getKeyStringForPathSource(String path, String source) {
        Long ts = getCurrentTimeStampForAssetFile(path);
        if (ts == 0 || ts == null) {
            return DigestUtils.md5Hex(source);
        } else {
            return ts.toString();
        }
    }

    public String convertUsingProcessorNoCache(String processor, String path, String source) {
        if (empty(processor)) {
            return source;
        }
        if ("angularHtml".equals(processor)) {
            Log.fine("Convert asset using angular");
            return AngularCompiler.htmlToJs(source, path);
        } else if ("jsx".equals(processor)) {
            Log.info("process JSX {0} {1}", processor, path);
            return ReactJsxCompiler.transform(source);
        } else if ("riot".equals(processor)) {
            return RiotCompiler.transform(source);
        }
        return source;
    }

    public AssetsControllerSafeWrapper getWrapper() {
        if (wrapper == null) {
            wrapper = new AssetsControllerSafeWrapper(this);
        }
        return wrapper;
    }

}
