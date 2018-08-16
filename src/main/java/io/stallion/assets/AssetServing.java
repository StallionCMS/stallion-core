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

import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import javax.ws.rs.NotFoundException;

import static io.stallion.utils.Literals.*;

import io.stallion.assetBundling.AssetHelpers;

import io.stallion.exceptions.WebException;
import io.stallion.jerseyProviders.LocalFileToResponse;
import io.stallion.jerseyProviders.ServletFileSender;
import io.stallion.requests.*;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.ResourceHelpers;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;


public class AssetServing {
    private IRequest request;
    private HttpServletResponse response;

    public AssetServing(IRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }


    public Response serveFileBundleAsset() throws Exception {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        path = path.substring(i + 1);

        FileSystemAssetBundleRenderer br = new FileSystemAssetBundleRenderer(path);
        String filePath = request.getQueryParam("bundleFilePath");
        String content = br.renderFile(filePath);
        return contentToResponse(content, request.getPath());
    }

    public Response serveFileBundle() throws Exception {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        path = path.substring(i + 1);

        FileSystemAssetBundleRenderer br = new FileSystemAssetBundleRenderer(path);
        return contentToResponse(br.renderProductionContent(), path);
    }


    public Response serveResourceAsset() throws Exception  {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        String assetPath = path.substring(i + 1);

        String[] parts = assetPath.split("/", 2);
        String plugin = parts[0];
        assetPath = parts[1];
        if (parts.length < 2) {
            throw new ClientErrorException("Invalid resource path " + assetPath, 400);
        }
        assetPath = "/" + assetPath;

        if (!empty(request.getQueryParam("bundlePath"))) {
            String bundlePath = AssetsController.ensureSafeAssetsPath(request.getQueryParam("bundlePath"));
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
            assetPath = AssetsController.ensureSafeAssetsPath(assetPath);
            if (!empty(request.getQueryParam("processor"))) {
                String content = ResourceHelpers.loadAssetResource(plugin, assetPath);
                //if (!empty(request.getParameter("nocache"))) {
                //content = AssetsController.instance().convertUsingProcessorNoCache(request.getParameter("processor"), path, content);
                //} else {
                //content = AssetsController.instance().convertUsingProcessor(request.getParameter("processor"), assetPath, content);
                //}

                //sendContentResponse(content);
                return new LocalFileToResponse().sendContentResponse(content, assetPath);
            } else {
                URL url = ResourceHelpers.pluginPathToUrl(plugin, assetPath);
                // If not found, and no referer, throw generic 404
                if (url == null && empty(request.getHeader("Referer"))) {
                    throw new NotFoundException("Asset resource not found: " + plugin + ":" + assetPath);
                } else if (url == null) {
                    // If not found, and referer, may mean there is a bug
                    throw new WebException("Requested linked resource that is not found: " + plugin + ":" + assetPath);
                }
                return new LocalFileToResponse().sendResource(url, assetPath);
            }
        }
    }

    public Response serveResourceBundle() throws Exception {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        path = path.substring(i + 1);
        String[] parts = path.split("/", 2);
        String plugin = parts[0];
        path = parts[1];
        path = AssetsController.ensureSafeAssetsPath(path);
        String content = new ResourceAssetBundleRenderer(plugin, path).renderProductionContent();
        return new LocalFileToResponse().sendContentResponse(content, path);
    }

    public Response serveFolderAssetToResponse(String path) throws Exception  {
        String fullPath = Settings.instance().getTargetFolder() + "/assets/" + path;
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
