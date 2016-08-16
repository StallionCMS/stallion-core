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

package io.stallion.tools;

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.StallionRunAction;
import io.stallion.boot.ServeCommandOptions;
import io.stallion.requests.RequestHandler;
import io.stallion.sitemaps.SiteMapController;
import io.stallion.sitemaps.SiteMapItem;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.testing.MockRequest;
import io.stallion.testing.MockResponse;
import io.stallion.utils.DateUtils;
import jodd.jerry.Jerry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static io.stallion.utils.Literals.*;


public class ExportToHtml implements StallionRunAction<ServeCommandOptions> {

    @Override
    public String getActionName() {
        return "export-to-html";
    }

    @Override
    public String getHelp() {
        return "Exports the entire web site to plain HTML and static files.";
    }


    @Override
    public ServeCommandOptions newCommandOptions() {
        return new ServeCommandOptions();
    }

    @Override
    public void loadApp(ServeCommandOptions options) {
        options.setDevMode(false);
        options.setLocalMode("false");
        AppContextLoader.loadCompletely(options);
    }

    @Override
    public void execute(ServeCommandOptions options) throws Exception {
        Log.info("EXECUTE EXPORT ACTION!!");
        String exportFolder = Settings.instance().getTargetFolder() + "/export-" + DateUtils.formatNow("yyyy-MM-dd-HH-mm-ss");
        File export = new File(exportFolder);
        if (!export.exists()) {
            export.mkdirs();
        }
        FileUtils.copyDirectory(new File(Settings.instance().getTargetFolder() + "/assets"), new File(exportFolder + "/st-assets"));



        Set<String> assets = new HashSet<>();

        Set<String> allUrlPaths = new HashSet<>();

        for(SiteMapItem item: SiteMapController.instance().getAllItems()) {
            String uri = item.getPermalink();
            Log.info("URI {0}", uri);
            if (!uri.contains("://")) {
                uri = "http://localhost" + uri;
            }
            URL url = new URL(uri);
            allUrlPaths.add(url.getPath());
        }

        allUrlPaths.addAll(ExporterRegistry.instance().exportAll());


        for(String path: allUrlPaths) {
            Log.info("Export page {0}", path);
            MockRequest request = new MockRequest(path, "GET");
            MockResponse response = new MockResponse();
            RequestHandler.instance().handleStallionRequest(request, response);
            response.getContent();

            if (!path.contains(".")) {
                if (!path.endsWith("/")) {
                    path += "/";
                }
                path += "index.html";
            }
            File file = new File(exportFolder + path);
            File folder = new File(file.getParent());
            if (!folder.isDirectory()) {
                folder.mkdirs();
            }
            String html = response.getContent();
            html = html.replace(Settings.instance().getSiteUrl(), "");
            FileUtils.write(file, html, UTF8);
            assets.addAll(findAssetsInHtml(response.getContent()));
        }

        for (String src: assets) {
            Log.info("Asset src: {0}", src);

            MockRequest request = new MockRequest(src, "GET");
            MockResponse response = new MockResponse();
            RequestHandler.instance().handleStallionRequest(request, response);
            int min = 300;
            if (response.getContent().length() < 300) {
                min = response.getContent().length();
            }
            URL url = new URL("http://localhost" + src);
            File file = new File(exportFolder + url.getPath());
            File folder = new File(file.getParent());
            if (!folder.isDirectory()) {
                folder.mkdirs();
            }
            FileUtils.write(file, response.getContent(), UTF8);
        }

    }

    public Set<String> findAssetsInHtml(String html) {
        HashSet<String> assets = new HashSet<>();
        Jerry jerry = Jerry.jerry(html);
        for(Jerry j :jerry.find("script")) {
            String src = j.attr("src");
            Log.info("SCRIPT TAG HTML {0} {1}", j.htmlAll(true), src);
            if (empty(src)) {
                continue;
            }
            Log.info("Add asset {0}", src);
            assets.add(src);
        }
        for(Jerry j :jerry.find("link")) {
            Log.info("LINK TAG HTML {0}", j.htmlAll(true));
            if (!"stylesheet".equals(j.attr("rel"))) {
                continue;
            }
            String src = j.attr("href");
            if (empty(src)) {
                continue;
            }
            assets.add(src);

        }
        for(Jerry j :jerry.find("img")) {
            String src = j.attr("src");
            if (empty(src)) {
                continue;
            }
            assets.add(src);
        }
        HashSet<String> filteredAssets = new HashSet<>();
        Log.info("CDN URL {0}", Settings.instance().getCdnUrl());
        Log.info("Site URL {0}", Settings.instance().getSiteUrl());
        for (String src: assets) {
            if (src.startsWith(Settings.instance().getCdnUrl())) {
                src = StringUtils.replace(src, Settings.instance().getCdnUrl(), "");
                if (!src.startsWith("/")) {
                    src = "/" + src;
                }
            }
            if (src.startsWith(Settings.instance().getSiteUrl())) {
                src = StringUtils.replace(src, Settings.instance().getSiteUrl(), "");
            }
            if (src.startsWith("/")) {
                filteredAssets.add(src);
            }
        }
        Log.info("Asset count {0}", filteredAssets.size());
        return filteredAssets;
    }
}

