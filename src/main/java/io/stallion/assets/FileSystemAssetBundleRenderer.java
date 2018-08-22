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

import io.stallion.assetBundling.AssetBundle;
import io.stallion.assetBundling.AssetFile;
import io.stallion.exceptions.UsageException;
import io.stallion.settings.Settings;
import org.apache.commons.codec.digest.DigestUtils;

import javax.ws.rs.NotFoundException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import static io.stallion.utils.Literals.empty;


public class FileSystemAssetBundleRenderer {
    private String fileSystemPath;
    private String path;
    private String extension;
    private File bundleFile;
    private String bundleRelativePath;

    public FileSystemAssetBundleRenderer(String path) {
        path = AssetsController.ensureSafeAssetsPath(path);
        int i = path.indexOf(".bundle.") + 7;
        if (i <= 6) {
            throw new UsageException("The path - "  + path + " - is not a valid bundle name. Expected to end with .bundle.js, .bundle.head.js, or .bundle.css");
        }
        this.extension = path.substring(i);
        path = path.substring(0, i);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.contains("..")) {
            throw new UsageException("Illegal assets path " + path);
        }
        if (!path.startsWith("assets/")) {
            this.bundleRelativePath = path;
            path = "assets/" + path;
        } else {
            this.bundleRelativePath = path.substring(7);
        }
        this.path = path;
        fileSystemPath = Settings.instance().getTargetFolder() + "/" + path;
        bundleFile = new File(fileSystemPath);
        if (!bundleFile.exists()) {
            throw new javax.ws.rs.NotFoundException("The bundle for path " + path + " was not found in the site /assets folder.");
        }

    }

    public String renderFile(String filePath) {
        AssetBundle bundle = io.stallion.assetBundling.BundleRegistry.instance().getByPath(bundleFile.getAbsolutePath());
        return bundle.renderPath(filePath, true);
    }

    public String renderDebugHtml() {

        AssetBundle bundle = io.stallion.assetBundling.BundleRegistry.instance().getByPath(bundleFile.getAbsolutePath());
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
            String relativePath = af.getRelativePath();
            if (!relativePath.startsWith("/")) {
                relativePath = "/" + relativePath;
            }
            try {
                url = Settings.instance().getCdnUrl() + "/st-assets/" + bundleRelativePath + this.extension
                        + "?isBundleFile=true&bundleFilePath=" + URLEncoder.encode(relativePath + extension, "utf-8")
                        + "&ts=" + af.getHydratedAt();
                //url = Settings.instance().getCdnUrl() + "/st-file-bundle-assets" + relativePath + extension + "?bundlePath=" + URLEncoder.encode(this.path, "utf-8") + extension + "&ts=" + af.getHydratedAt();
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
        String url = Settings.instance().getCdnUrl() + "/st-assets/" + bundleRelativePath + extension + "?isConcatenatedFileBundle=true&hash=" + hash;
        if (".css".equals(extension)) {
            tag = "<link rel=\"stylesheet\" href=\"{0}\">";
        }
        return MessageFormat.format(tag, url);
    }



    public String renderProductionContent() {
        AssetBundle bundle = io.stallion.assetBundling.BundleRegistry.instance().getByPath(bundleFile.getAbsolutePath());
        bundle.hydrateAllFilesIfNeeded(true);
        if (extension.equals(".head.js")) {
            return bundle.getConcatenatedHeadJavaScript();
        } else if (extension.equals(".js")) {
            return bundle.getConcatenatedJavaScript();
        } else if (extension.equals(".css")) {
            return bundle.getConcatenatedCss();
        }
        throw new NotFoundException("Could not find a bundle handler for extension " + extension);

    }
}
