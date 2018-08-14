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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static io.stallion.utils.Literals.*;

import io.stallion.assetBundling.AssetHelpers;
import io.stallion.exceptions.ClientException;
import io.stallion.exceptions.NotFoundException;
import io.stallion.exceptions.WebException;
import io.stallion.requests.*;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.ResourceHelpers;
import org.apache.commons.io.IOUtils;


public class AssetServing {
    private IRequest request;
    private StResponse response;

    public AssetServing(IRequest request, StResponse response) {
        this.request = request;
        this.response = response;
    }


    public void serveFileBundleAsset() throws Exception {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        path = path.substring(i + 1);

        FileSystemAssetBundleRenderer br = new FileSystemAssetBundleRenderer(path);
        String filePath = request.getParameter("bundleFilePath");
        String content = br.renderFile(filePath);
        sendContentResponse(content, request.getPath());
    }

    public void serveFileBundle() throws Exception {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        path = path.substring(i + 1);

        FileSystemAssetBundleRenderer br = new FileSystemAssetBundleRenderer(path);
        sendContentResponse(br.renderProductionContent(), path);
    }

    public void serveResourceAsset() throws Exception  {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        String assetPath = path.substring(i + 1);

        String[] parts = assetPath.split("/", 2);
        String plugin = parts[0];
        assetPath = parts[1];
        if (parts.length < 2) {
            throw new ClientException("Invalid resource path " + assetPath);
        }
        assetPath = "/" + assetPath;

        if (request.getQueryParams().containsKey("bundlePath")) {
            String bundlePath = AssetsController.ensureSafeAssetsPath(request.getQueryParams().get("bundlePath"));
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
            sendContentResponse(content, assetPath);
        } else {
            assetPath = AssetsController.ensureSafeAssetsPath(assetPath);
            if (!empty(request.getParameter("processor"))) {
                String content = ResourceHelpers.loadAssetResource(plugin, assetPath);
                //if (!empty(request.getParameter("nocache"))) {
                //content = AssetsController.instance().convertUsingProcessorNoCache(request.getParameter("processor"), path, content);
                //} else {
                //content = AssetsController.instance().convertUsingProcessor(request.getParameter("processor"), assetPath, content);
                //}
                markHandled(200, "resource-asset");
                sendContentResponse(content);
            } else {
                markHandled(200, "resource-asset");
                URL url = ResourceHelpers.pluginPathToUrl(plugin, assetPath);
                // If not found, and no referer, throw generic 404
                if (url == null && empty(request.getHeader("Referer"))) {
                    throw new NotFoundException("Asset resource not found: " + plugin + ":" + assetPath);
                } else if (url == null) {
                    // If not found, and referer, may mean there is a bug
                    throw new WebException("Requested linked resource that is not found: " + plugin + ":" + assetPath);
                }
                new ServletFileSender(request, response).sendResource(url, assetPath);
                complete();
            }
        }
    }

    public void serveResourceBundle() throws Exception {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        path = path.substring(i + 1);
        String[] parts = path.split("/", 2);
        String plugin = parts[0];
        path = parts[1];
        path = AssetsController.ensureSafeAssetsPath(path);
        String content = new ResourceAssetBundleRenderer(plugin, path).renderProductionContent();
        sendContentResponse(content, path);
    }

    public void serveFolderAsset(String path) throws Exception  {
        String fullPath = Settings.instance().getTargetFolder() + "/assets/" + path;
        Log.fine("Asset path={0} fullPath={1}", path, fullPath);
        File file = new File(fullPath);
        String preProcessor = request.getParameter("preprocessor");
        if (!file.isFile() && empty(preProcessor)) {
            notFound("Asset for path " + path + " is not found.");
        }
        if (!empty(request.getParameter("processor"))) {
            String contents = IOUtils.toString(new FileReader(fullPath));
            //contents = AssetsController.instance().convertUsingProcessor(request.getParameter("processor"), path, contents);
            markHandled(200, "folder-asset-with-processor");
            sendContentResponse(contents, file.lastModified(), fullPath);
        } else {
            //if (!empty(preProcessor)) {
            //    AssetsController.instance().externalPreprocessIfNecessary(preProcessor, path);
            //}
            markHandled(200, "folder-asset");
            sendAssetResponse(file);
        }
    }


    private void sendContentResponse(String content) {
        try {
            response.getWriter().print(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        complete();
    }

    public void sendContentResponse(String content, String fullPath)  {
        sendContentResponse(content, 0, fullPath);
    }

    public void sendContentResponse(String content, long modifyTime, String fullPath)  {
        new ServletFileSender(request, response).sendContentResponse(content, modifyTime, fullPath);
        complete();
    }

    public void sendAssetResponse(File file) {
        try {
            sendAssetResponse(new FileInputStream(file), file.lastModified(), file.length(), file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAssetResponse(InputStream stream, long modifyTime, long contentLength, String fullPath)  {
        new ServletFileSender(request, response).sendAssetResponse(stream, modifyTime, contentLength, fullPath);
        complete();
    }

    public void markHandled(int status, String message) {
        response.setStatus(status);
        Log.logForFrame(2, Level.FINE, "status={0} handler=\"{1}\" {2}={3}", status, message, request.getMethod(), request.getPath());
    }

    public void notFound(String message) {
        markHandled(404, message);
        throw new NotFoundException(message);
    }

    public void complete(int status, String message, Object...args) {
        markHandled(status, message);
        throw new ResponseComplete();
    }

    public void complete() {
        throw new ResponseComplete();
    }



}
