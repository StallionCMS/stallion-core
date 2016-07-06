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


public interface BundleFileBase {


    public default void hydrateComboContent() {

    };
    public void ensureHydrated();

    //public void hydrateCssAndJavaScriptFromRawContent();


    //public String processContent(String processor, String content);

    public default String getCurrentPath() {
        if (Settings.instance().getDevMode() && !empty(getDevPath())) {
            return getDevPath();
        } else {
            return getProductionPath();
        }
    }


    public String getProductionPath();

    public ResourceBundleFile setProductionPath(String productionPath);

    public String getDevPath();

    public ResourceBundleFile setDevPath(String devPath);


    public String getPlugin();

    public ResourceBundleFile setPlugin(String plugin);

    public String getProcessor();

    public ResourceBundleFile setProcessor(String processor);

    public String getRawContent();

    public ResourceBundleFile setRawContent(String rawContent);

    public Long getLoadedAt();

    public ResourceBundleFile setLoadedAt(Long loadedAt);

    public String getCss();

    public ResourceBundleFile setCss(String css);

    public String getHeadJavascript();

    public ResourceBundleFile setHeadJavascript(String headJavascript);
    public String getJavascript();

    public ResourceBundleFile setJavascript(String javascript);

    public boolean isJsInHead();

    public ResourceBundleFile setJsInHead(boolean jsInHead);

    public AssetType getType();

    public ResourceBundleFile setType(AssetType type);

    public String getProcessedContent();

    public ResourceBundleFile setProcessedContent(String processedContent);
}
