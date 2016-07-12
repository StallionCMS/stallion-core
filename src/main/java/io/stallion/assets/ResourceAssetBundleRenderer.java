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

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import io.stallion.Context;
import io.stallion.assetBundling.*;
import io.stallion.exceptions.UsageException;
import io.stallion.plugins.PluginRegistry;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URLEncodedUtils;


public class ResourceAssetBundleRenderer {

    private String path;
    private String plugin;
    private String extension;
    private Class resourceClass;
    private URL resourceUrl;

    public ResourceAssetBundleRenderer(String plugin, String path) {
        this.plugin = or(plugin, "stallion");
        path = AssetsController.ensureSafeAssetsPath(path);
        int i = path.indexOf(".bundle.") + 7;
        if (i == -1) {
            throw new UsageException("The path - "  + path + " - is not a valid bundle name. Expected to end with .bundle.js, .bundle.head.js, or .bundle.css");
        }
        this.extension = path.substring(i);
        this.path = path.substring(0, i);
        this.resourceClass = ResourceAssetBundleRenderer.class;
        if (!empty(plugin) && !plugin.equals("stallion")) {
            this.resourceClass = PluginRegistry.instance().getJavaPluginByName().get(plugin).getClass();
        }
        resourceUrl = resourceClass.getResource(this.path);
        if (resourceUrl == null) {
            throw new UsageException("The bundle you are trying to render - " + this.path + " - does not exist in the plugin with class " + resourceClass.getCanonicalName());
        }

    }

    public String renderDebugHtml() {
        if (!resourceUrl.getProtocol().equals("file")) {
            return renderProductionHtml();
        }
        String bundleFilePath = null;
        try {
            bundleFilePath = resourceUrl.toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        File debugVersion = new File(bundleFilePath.replace("/target/classes/", "/src/main/resources/"));
        if (debugVersion.exists()) {
            bundleFilePath = debugVersion.getAbsolutePath();
        }
        AssetBundle bundle = io.stallion.assetBundling.BundleRegistry.instance().getByPath(bundleFilePath);
        bundle.hydrateFilesIfNeeded(true);
        StringBuilder builder = new StringBuilder();
        for (AssetFile af: bundle.getFiles()) {
            af.hydrateIfNeeded(true);
            String tag = "<script src=\"{0}\" type=\"text/javascript\"></script>";
            if (".head.js".equals(extension)) {
                if (empty(af.getHeadJavaScript())) {
                    continue;
                }
            } else if (".css".equals(extension)) {
                if (empty(af.getCss())) {
                    continue;
                }
                tag = "<link rel=\"stylesheet\" href=\"{0}\">";
            } else if (".js".equals(extension)) {
                if (empty(af.getJavaScript())) {
                    continue;
                }
            }
            String url;
            try {
                String relativePath = af.getRelativePath();
                if (!relativePath.startsWith("/")) {
                    relativePath = "/" + relativePath;
                }
                url = Settings.instance().getCdnUrl() + "/st-resource/" + plugin + relativePath + extension + "?bundlePath=" + URLEncoder.encode(this.path, "utf-8") + "&ts=" + af.getHydratedAt();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            builder.append("    " + MessageFormat.format(tag, url) + "\n");

        }
        return builder.toString();

    }

    public String renderProductionHtml() {
        String content = renderProductionContent();
        String hash = DigestUtils.md5Hex(content);
        String tag = "<script src=\"{0}\" type=\"text/javascript\"></script>";
        String url = Settings.instance().getCdnUrl() + "/st-bundle-v2/" + plugin + path + extension + "?hash=" + hash;
        if (".css".equals(extension)) {
            tag = "<link rel=\"stylesheet\" href=\"{0}\">";
        }
        return MessageFormat.format(tag, url);
    }



    public String renderProductionContent() {
        URL url = resourceClass.getResource(path + ".min" + extension);
        if (url == null) {
            url = resourceClass.getResource(path + extension);
        }
        if (url == null) {
            throw new UsageException("Could not find compiled bundle for path: " + path);
        }
        Context.response().addHeader("X-Sourcemap", "/st-resource/" + path + ".min" + extension + ".map");
        try {
            return IOUtils.toString(url, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
