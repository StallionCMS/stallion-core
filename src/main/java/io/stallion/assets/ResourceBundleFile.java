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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import io.stallion.exceptions.WebException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.DateUtils;
import io.stallion.utils.ResourceHelpers;
import org.parboiled.common.FileUtils;


public class ResourceBundleFile implements BundleFileBase {
    private String productionPath = "";
    private String devPath = "";
    private String plugin = "";
    private String processor = "";
    private String rawContent = null;
    private String processedContent = null;
    private Long loadedAt = 0L;
    private String css = "";
    private String headJavascript = "";
    private String javascript = "";
    private boolean jsInHead = false;
    private AssetType type;

    public ResourceBundleFile() {

    }
    public ResourceBundleFile(String plugin, String productionPath) {
        this(plugin, productionPath, "", "");
    }

    public ResourceBundleFile(String plugin, String productionPath, String devPath) {
        this(plugin, productionPath, devPath, "");
    }


    public ResourceBundleFile(String plugin, String productionPath, String devPath, String processor) {
        this.plugin = plugin;
        this.productionPath = productionPath;
        this.devPath = devPath;
        this.processor = processor;
    }


    public void ensureHydrated() {
        if (rawContent != null && !Settings.instance().getDevMode()) {
            return;
        }
        String currentPath = getCurrentPath();
        if (!currentPath.startsWith("/assets/")) {
            currentPath = "/assets/" + currentPath;
        }

        if (!Settings.instance().getDevMode()) {
            try {
                rawContent = ResourceHelpers.loadAssetResource(plugin, currentPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            File file = ResourceHelpers.findDevModeFileForResource(plugin, currentPath);
            if (!file.exists()) {
                throw new WebException(MessageFormat.format("Could not find bundle resource file for plugin={0} path={1} filePath={2}", plugin, currentPath, file.getAbsolutePath()), 500);
            }
            if (rawContent != null && loadedAt > file.lastModified()) {
                // rawContent is up to date, return
                return;
            }
            rawContent = FileUtils.readAllText(file, Charset.forName("UTF-8"));
            loadedAt = DateUtils.mils();
        }
        hydrateCssAndJavaScriptFromRawContent();
    }

    public void hydrateCssAndJavaScriptFromRawContent() {
        if (!empty(getProcessor())) {
            this.processedContent = processContent(getProcessor(), getRawContent());
        } else {
            this.processedContent = rawContent;
        }
        if (getType().equals(AssetType.CSS)) {
            setCss(processedContent);
        } else if (getType().equals(AssetType.JAVA_SCRIPT)) {
            setJavascript(processedContent);
        } else {
            hydrateComboContent();
        }
    }

    public String processContent(String processor, String content) {
        return content;
    }

    public String getCurrentPath() {
        if (Settings.instance().getDevMode() && !empty(devPath)) {
            return devPath;
        } else {
            return productionPath;
        }
    }


    public String getProductionPath() {
        return productionPath;
    }

    public ResourceBundleFile setProductionPath(String productionPath) {
        this.productionPath = productionPath;
        return this;
    }

    public String getDevPath() {
        return devPath;
    }

    public ResourceBundleFile setDevPath(String devPath) {
        this.devPath = devPath;
        return this;
    }

    public String getPlugin() {
        return plugin;
    }

    public ResourceBundleFile setPlugin(String plugin) {
        this.plugin = plugin;
        return this;
    }

    public String getProcessor() {
        return processor;
    }

    public ResourceBundleFile setProcessor(String processor) {
        this.processor = processor;
        return this;
    }

    public String getRawContent() {
        return rawContent;
    }

    public ResourceBundleFile setRawContent(String rawContent) {
        this.rawContent = rawContent;
        return this;
    }

    public Long getLoadedAt() {
        return loadedAt;
    }

    public ResourceBundleFile setLoadedAt(Long loadedAt) {
        this.loadedAt = loadedAt;
        return this;
    }

    public String getCss() {
        return css;
    }

    public ResourceBundleFile setCss(String css) {
        this.css = css;
        return this;
    }

    public String getHeadJavascript() {
        return headJavascript;
    }

    public ResourceBundleFile setHeadJavascript(String headJavascript) {
        this.headJavascript = headJavascript;
        return this;
    }

    public String getJavascript() {
        return javascript;
    }

    public ResourceBundleFile setJavascript(String javascript) {
        this.javascript = javascript;
        return this;
    }

    public boolean isJsInHead() {
        return jsInHead;
    }

    public ResourceBundleFile setJsInHead(boolean jsInHead) {
        this.jsInHead = jsInHead;
        return this;
    }

    public AssetType getType() {
        if (type == null) {
            if (getCurrentPath().endsWith(".js") || getCurrentPath().endsWith(".ts") || getCurrentPath().endsWith(".coffee")) {
                return AssetType.JAVA_SCRIPT;
            } else if (getCurrentPath().endsWith(".vue")) {
                return AssetType.COMBO;
            } else {
                return AssetType.CSS;
            }
        }
        return type;
    }

    public ResourceBundleFile setType(AssetType type) {
        this.type = type;
        return this;
    }

    public String getProcessedContent() {
        return processedContent;
    }

    public ResourceBundleFile setProcessedContent(String processedContent) {
        this.processedContent = processedContent;
        return this;
    }
}
