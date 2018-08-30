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

import io.stallion.assetBundling.AssetHelpers;
import io.stallion.jerseyProviders.LocalFileToResponse;
import io.stallion.requests.IRequest;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.ResourceHelpers;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URL;

import static io.stallion.utils.Literals.empty;


public class AssetServing {
    private String plugin;
    private String path;
    private String referer;

    public AssetServing(String plugin, String path, String referer) {
        this.path = AssetsController.ensureSafeAssetsPath(path);
        this.plugin = plugin;
        this.referer = referer;
    }


    public Response serveFileBundleAsset(String bundleFilePath) throws Exception {

        FileSystemAssetBundleRenderer br = new FileSystemAssetBundleRenderer(path);
        String content = br.renderFile(bundleFilePath);
        return contentToResponse(content, path);
    }

    public Response serveFileBundle() throws Exception {
        FileSystemAssetBundleRenderer br = new FileSystemAssetBundleRenderer(path);
        return contentToResponse(br.renderProductionContent(), path);
    }


    public Response serveResourceAsset(String bundlePath) throws Exception  {
        String assetPath = path;


        if (!empty(bundlePath)) {
            if (assetPath.startsWith("/assets/")) {
                assetPath = assetPath.substring(8);
            }
            bundlePath = AssetsController.ensureSafeAssetsPath(bundlePath);
            URL url = ResourceHelpers.getUrlOrNotFound(plugin, bundlePath);
            String content = null;
            if (Settings.instance().getDevMode()) {
                File file = ResourceHelpers.findDevModeFileForResource(plugin, bundlePath);
                if (file != null) {
                    content = AssetHelpers.renderDebugModeBundleFileByPath(file.getAbsolutePath(), assetPath);
                }
            }
            if (empty(content)) {
                content = AssetHelpers.renderDebugModeBundleFileByPath(url.getPath(), assetPath);
            }
            return new LocalFileToResponse().sendContentResponse(content, assetPath);
        } else {
            URL url = ResourceHelpers.pluginPathToUrl(plugin, assetPath);
            // If not found, and no referer, throw generic 404
            if (url == null && empty(referer)) {
                throw new NotFoundException("Asset resource not found: " + plugin + ":" + assetPath);
            } else if (url == null) {
                // If not found, and referer, may mean there is a bug
                throw new ServerErrorException("Requested linked resource that is not found: " + plugin + ":" + assetPath, 500);
            }
            return new LocalFileToResponse().sendResource(url, assetPath);
        }
    }

    public Response serveResourceBundle() throws Exception {
        Log.info("path : {0} plugin: {1}", path, plugin);

        String content = new ResourceAssetBundleRenderer(plugin, path).renderProductionContent();
        return new LocalFileToResponse().sendContentResponse(content, path);
    }

    public Response serveFolderAssetToResponse() throws Exception  {
        String fullPath = Settings.instance().getTargetFolder() + path;
        Log.fine("Asset path={0} fullPath={1}", path, fullPath);
        File file = new File(fullPath);
        if (!file.isFile()) {
            throw new NotFoundException("Asset for path " + path + " is not found.");
        }

        return new LocalFileToResponse().sendAssetResponse(file);

    }



    private Response contentToResponse(String s, String path) {
        return new LocalFileToResponse().sendContentResponse(s, path);
    }


    private Response contentToResponse(String s, long modifyTime, String path) {
        return new LocalFileToResponse().sendContentResponse(s, modifyTime, path);
    }





}
