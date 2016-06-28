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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.stallion.Context;
import io.stallion.exceptions.UsageException;
import io.stallion.exceptions.WebException;
import io.stallion.services.Log;
import io.stallion.services.PermaCache;
import io.stallion.settings.Settings;
import io.stallion.utils.ResourceHelpers;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

import static io.stallion.utils.Literals.*;

/**
 * This is class handles all matters having to do with asset bundling. An asset bundle
 * is a collection of asset files that get concatenated together on production, or
 * included separately in development mode. This class takes care of
 * parsing the bundles, defining bundles, keeping track of changes, caching,
 * generating cache busting urls, etc. etc.
 *
 * This class is instantiated once per bundle, with processing results cached in
 * a local static variable.
 *
 *
 */
public class BundleHandler {
    private String path;
    private String query;
    private Boolean isJs = false;
    private Boolean isCss = false;
    private String basePath = "";
    private String baseUrl = "";
    private static final Long startTime = DateTime.now().getMillis();
    private String fileContents;
    private static Set<String> allowedResourcePaths = new HashSet<>();
    private static Map<String, Bundle> bundlePathToBundle = new HashMap<>();
    private static Map<String, String> bundlePathToContent = new HashMap<>();
    private DefinedBundle definedBundle = null;

    public static void clearCache() {
        bundlePathToBundle = new HashMap<>();

        bundlePathToContent = new HashMap<>();
    }

    public BundleHandler(DefinedBundle definedBundle) {
        this(definedBundle.getName() + definedBundle.getExtension(), "");
        this.definedBundle = definedBundle;
    }

    public BundleHandler(String path) {
        this(path, "");
    }


    public BundleHandler(String path, String query) {
        this.query = query;
        this.setPath(path);
        if (path.endsWith(".js")) {
            isJs = true;
            isCss = false;
        } else {
            isCss = true;
            isJs = false;
        }
        basePath = Context.settings().getTargetFolder() + "/assets/";
        baseUrl = Context.settings().getCdnUrl();
    }

    /**
     * Turns the bundle into HTML that will be included on the web page. The HTML
     * will be one or more &lt;script&gt; tags for javascript bundles, or one or more
     * &lt;link&gt; tags for stylesheet bundles.
     *
     * @return
     */
    public String toHtml() {
        boolean bundleDebug = false;
        if (Context.settings().getEnv().equals("local") || Context.settings().getEnv().equals("test")) {
            bundleDebug = true;
        }
        if (Context.getSettings().getDevMode() == true) {
            bundleDebug = true;
        }
        if (Context.getSettings().getBundleDebug() != null) {
            bundleDebug = Context.getSettings().getBundleDebug();
        }

        if (bundleDebug) {
            return toDebugHtml();
        } else {
            return toLiveHtml();
        }
    }

    /**
     * Turns the bundle into HTML that will be included on a deployed instance of the site,
     * in which all the bundle files are concatenated into a single file. This method
     * will return a script tag or link tag to that bundled file.
     *
     * @return
     */
    public String toLiveHtml() {
        Bundle bundle = null;
        if (bundlePathToBundle.containsKey(getFullPathAndQuery())) {
            bundle = bundlePathToBundle.get(getFullPathAndQuery());
        } else {
            bundle = loadBundle();
            bundlePathToBundle.put(getFullPathAndQuery().toString(), bundle);
        }
        if (bundle.getBundleFiles().size() == 0) {
            return "";
        }

        String cdnUrl = Context.settings().getCdnUrl();
        String prefix = "/st-assets/";
        String query = "standard";
        if (definedBundle != null) {
            query = "defined";
        }
        // list the files and add up the timestamps
        if (isJs) {
            return String.format("\n  <script src=\"%s%s%s?stBundle=%s&ts=%s\"></script>\n",
                    baseUrl, prefix, path, query, buildCacheBustingStringForLive(bundle));
        } else {
            return String.format("\n  <link rel=\"stylesheet\" href=\"%s%s%s?stBundle=%s&ts=%s\"/>\n",
                    baseUrl, prefix, path, query, buildCacheBustingStringForLive(bundle));
        }
    }

    /**
     * Creates an MD5 hash based on all the files and their last modified time in the bundle,
     * this hash thus changes any time the bundle is changed.
     *
     * @param bundle
     * @return
     */
    public String buildCacheBustingStringForLive(Bundle bundle) {

        StringBuilder builder = new StringBuilder();
        for (BundleFile bf: bundle.getBundleFiles()) {
            if (bf.getLiveUrl().startsWith("http://") || bf.getLiveUrl().startsWith("https://")) {
                builder.append(bf.getLiveUrl() + "|");
            } else if (empty(bf.getPluginName())) {
                builder.append(getFileTimeStamp(bf.getLiveUrl()).toString() + "|");
            } else {
                builder.append("resource|" + bf.getLiveUrl() + "|" + bf.getPluginName() + "|" + startTime + "|");
            }
        }
        return DigestUtils.md5Hex(builder.toString());
    }

    /* Bundle concatenation methods */

    /**
     * Concatenates all the bundle files into a single file.
     *
     * @return
     */
    public String toConcatenatedContent() {
        if (bundlePathToContent.containsKey(getFullPathAndQuery())) {
            return bundlePathToContent.get(getFullPathAndQuery());
        }
        Bundle bundle = loadBundle();

        StringBuffer buffer = new StringBuffer();
        for (BundleFile bf: bundle.getBundleFiles()) {
            Log.finest("Bundle file is {0}", bf.getLiveUrl());
            String contents;
            if (bf.getLiveUrl().startsWith("http://") || bf.getLiveUrl().startsWith("https://")) {
                contents = externalUrlToString(bf);
            } else if (empty(bf.getPluginName())) {
                contents = assetFileToString(bf);
            } else {
                contents = resourceFileToString(bf);
            }
            if (!empty(bf.getProcessor())) {
                contents = AssetsController.instance().convertUsingProcessor(bf.getProcessor(), bf.getLiveUrl(), contents);
            }
            buffer.append(contents);
            buffer.append("\n\n");
        }
        String source = buffer.toString();
        // Prevent rogue query strings from making this hashtable grow unbounded
        if (bundlePathToContent.size() < 100) {
            bundlePathToContent.put(getFullPathAndQuery(), source);
        }
        return source;
    }

    /**
     * Converts a single bundle file into a string.
     *
     * @param bundleFile
     * @return
     */
    protected String assetFileToString(BundleFile bundleFile) {
        String[] parts = bundleFile.getLiveUrl().split("\\?", 2);
        String path = parts[0];
        if (parts.length > 1 && !empty(parts[1])) {
            String query = parts[1];
            Map<String, String> query_pairs = new HashMap<String, String>();
            String[] pairs = query.split("&");

                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx == -1) {
                        query_pairs.put(pair, "");
                        continue;
                    }
                    try {
                        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                    } catch(UnsupportedEncodingException e) {
                        continue;
                    }
                }
            bundleFile.setQueryParams(query_pairs);
        }

        String fullPath = basePath + path;
        String contents = null;
        try {
            contents = FileUtils.readFileToString(new File(fullPath), Charset.forName("UTF-8"));
            //contents = IOUtils.toString(new FileReader(fullPath)., );
        } catch (IOException e) {
            throw new UsageException("Bundle file does not exist: " + fullPath);
        }
        return contents;
    }

    /**
     * Converts a bundle that is including a java jar resource into a String.
     *
     * @param bundleFile
     * @return
     */
    protected String resourceFileToString(BundleFile bundleFile) {
        URL resourceUrl = null;
        if (empty(bundleFile.getPluginName())) {
            throw new UsageException("Plugin name is empty for bundle file!");
        }
        try {
            return ResourceHelpers.loadAssetResource(bundleFile.getPluginName(), "/assets/" + bundleFile.getLiveUrl());
        } catch (IOException e) {
            throw new WebException("Error loading resource " + bundleFile.getLiveUrl() + " plugin: " + bundleFile.getPluginName() , 500, e);
        }
    }

    /**
     * Loads a bundle file that is an external URL.
     *
     * @param bundleFile
     * @return
     */
    protected  String externalUrlToString(BundleFile bundleFile) {
        String cacheKey = "url:" + bundleFile.getLiveUrl();
        String contents = PermaCache.get(bundleFile.getLiveUrl());
        if (contents != null) {
            return contents;
        }
        HttpResponse<String> response = null;
        try {
            response = Unirest.get(bundleFile.getLiveUrl()).asString();
        } catch (UnirestException e) {
            Log.exception(e, "Error downloading " + bundleFile.getLiveUrl());
            PermaCache.setInMemoryOnly(cacheKey, "");
            return "";
        }
        if (response.getStatus() >= 200 && response.getStatus() < 300) {
            contents = response.getBody();
            PermaCache.set(bundleFile.getLiveUrl(), contents);
            return contents;
        } else {
            PermaCache.setInMemoryOnly(cacheKey, "");
            Log.warn("Fetch of {0} had status code {1}", bundleFile.getLiveUrl(), response.getStatus());
            return "";
        }
    }




    /* Debug version methods */

    /**
     * Outputs the HTML to the web page with script or link tags to all the files in the bundle,
     * for either development or debug mode.
     *
     * @return
     */
    public String toDebugHtml() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n");
        for(BundleFile bf: loadBundle().getBundleFiles()) {
            String line = "";
            if (isJs) {
                buffer.append(String.format(
                        "  <script type=\"text/javascript\" src=\"%s\"></script>\n",
                        getDebugUrl(bf)));
            } else {
                buffer.append(String.format(
                        "  <link rel=\"stylesheet\" href=\"%s\" />\n",
                        getDebugUrl(bf)));
            }
        }
        return buffer.toString();
    }

    /**
     * Get the URL to the debug version of a bundle file.
     *
     * @param bf
     * @return
     */
    public String getDebugUrl(BundleFile bf) {
        String url = bf.getDebugUrl();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            url = appendCacheBusting(
                    bf.getDebugUrl(),
                    DigestUtils.md5Hex(url));
        } else if (empty(bf.getPluginName())) {
            url = appendCacheBusting(
                    baseUrl + "/st-assets/" + url,
                    getFileTimeStamp(url));
        } else {
            // Register this path as an allowed resource path
            addAllowedPath(bf.getPluginName(), url);
            url = appendCacheBusting(
                    baseUrl + "/st-resource/" + bf.getPluginName() + "/" + url,
                    startTime.toString());
        }
        if (!empty(bf.getProcessor()) && !url.contains("processor=")) {
            url += "&processor=" + bf.getProcessor();
        }
        return url;
    }

    public String appendCacheBusting(String url, String cacheBust) {
        if (!url.contains("?")) {
            url = url + "?";
        } else if (!url.endsWith("&")) {
            url = url + "&";
        }
        url = url + "vstring=" + cacheBust;
        return url;
    }

    public String getFileTimeStamp(String path) {
        return AssetsController.instance().getTimeStampForAssetFile(path).toString();
    }

    /* Common methods */

    /**
     * Load the bundle from the name.bundle.js or name.bundle.css bundle file in the
     * file system and convert it to a Java object.
     *
     * @return
     */
    public Bundle loadBundle() {
        if (definedBundle != null) {
            return definedBundle;
        }
        Bundle bundle = new Bundle();
        List<BundleFile> paths = new ArrayList<>();
        HashSet<String> keys = new HashSet<>();
        String contents = getFileContents();
        Log.finest("Bundle contents is: {0}", contents);
        for(String line: contents.split("\\n")) {
            line = line.trim();
            if (!line.startsWith("//=")) {
                continue;
            }
            line = line.substring(3).trim();
            if (line.startsWith("/")) {
                line = line.substring(1);
            }
            BundleFile bf = new BundleFile();
            ArrayList<String> parts = new ArrayList(Arrays.asList(line.split("\\|")));
            for(Object i: safeLoop(10)) {
                if (parts.size() == 0) {
                    break;
                }
                String part = parts.remove(0);
                if (part.startsWith("key:")) {
                    bf.setAssetKey(part.substring(4).toLowerCase());
                } else if (part.startsWith("resource:")) {
                    bf.setPluginName(part.substring(9));
                } else {
                    if (empty(bf.getLiveUrl())) {
                        bf.setLiveUrl(part);
                    } else if (empty(bf.getDebugUrl())){
                        bf.setDebugUrl(part);
                    } else {
                        bf.setDeveloperUrl(part);
                    }
                }
            }
            if (empty(bf.getDebugUrl())) {
                bf.setDebugUrl(bf.getLiveUrl());
            }

            if (Context.getSettings().getDevMode() == true && !empty(bf.getDeveloperUrl())) {
                bf.setDebugUrl(bf.getDeveloperUrl());
            }

            if (!empty(bf.getAssetKey())) {
                if (keys.contains(bf.getAssetKey())) {
                    continue;
                }
                keys.add(bf.getAssetKey());
            }
            bundle.getBundleFiles().add(bf);
        }
        return bundle;
    }

    @Deprecated
    public static void addAllowedPath(String pluginName, String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Log.finest("add allowed path pluginName={0} path={1}", pluginName, path);
        allowedResourcePaths.add(pluginName + "|/" + path);
    }

    @Deprecated
    public static boolean resourceIsAllowed(String pluginName, String path) {
        Log.finest("Check resource allowed: plugin={0} path={1}", pluginName, path);
        if (allowedResourcePaths.contains(pluginName + "|" + path)) {
            return true;
        } else {
            return false;
        }
    }

    /* getters and setters */

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    private String getFullPathAndQuery() {
        return path + "?" + query;
    }

    /**
     * Load the bundle as text from either a ClassPath Resource or from the file system.
     * @return
     */
    public String getFileContents() {
        if (fileContents == null) {
            if (this.path.startsWith("stallion:")) {
                try {
                    fileContents = IOUtils.toString(getClass().getResourceAsStream("/assets/" + this.path.substring(9, this.path.length())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                File f = new File(this.path);
                if (!f.exists()) {
                    f = new File(Context.settings().getTargetFolder() + "/assets/" + this.path);
                }
                if (!f.exists()) {
                    Log.warn("Bundle file does not exist {0}", f.toString());
                    fileContents = "";
                } else {
                    try {
                        fileContents = FileUtils.readFileToString(f);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return fileContents;
    }

    public void setFileContents(String fileContents) {
        this.fileContents = fileContents;
    }
}
