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

package io.stallion.templating;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.loader.ResourceLocator;
import com.hubspot.jinjava.loader.ResourceNotFoundException;
import io.stallion.Context;
import io.stallion.services.Log;
import io.stallion.utils.Literals;
import io.stallion.utils.ResourceHelpers;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class JinjaResourceLocator implements ResourceLocator {
    private static Map<String, String> templateSourceCache;

    private String targetDirectory;

    public static void clearCache() {
        if (templateSourceCache != null) {
            templateSourceCache = new HashMap<>();
        }
    }

    public JinjaResourceLocator(String targetDirectory, boolean devMode) {
        if (!targetDirectory.endsWith("/")) {
            targetDirectory = targetDirectory + "/";
        }
        targetDirectory = targetDirectory + "templates/";
        this.targetDirectory = targetDirectory;

        if (!devMode) {
            templateSourceCache = new HashMap<>();
        }
    }

    @Override
    public String getString(String s, Charset charset, JinjavaInterpreter jinjavaInterpreter) throws IOException {
        if (templateSourceCache == null) {
            String source = getStringDirect(s, charset);
            if (source == null) {
                throw new ResourceNotFoundException("Template not found: " + s);
            }
            return source;
        }
        Log.finest("load template: {0}", s);
        String cacheKey = "jinjaTemplate" + Literals.GSEP + s;
        if (templateSourceCache.containsKey(cacheKey) && !Context.getSettings().getDevMode()) {
            return templateSourceCache.get(cacheKey);
        }


        String source = getStringDirect(s, charset);
        if (source == null) {
            throw new ResourceNotFoundException("Template not found: " + s);
        }
        templateSourceCache.put(cacheKey, source);
        return source;

    }

    private String getStringDirect(String s, Charset charset) throws IOException {

        if (s.contains("jar:") || s.contains("file:/")) {
            URL url = new URL(s);
            return ResourceHelpers.loadResourceFromUrl(url);
        } else if (s.contains(":")) {
            String[] parts = s.split(":", 2);
            String pluginName = parts[0];
            String pathName = parts[1];
            if (pathName.startsWith("/")) {
                pathName = pathName.substring(1);
            }
            String resourcePath = "/templates/" + pathName;
            return ResourceHelpers.loadAssetResource(pluginName, resourcePath);
        }
        if (s.startsWith("/")) {
            s = s.substring(1);
        }
        String path = targetDirectory + s;
        File file = new File(path);
        Log.finer("Load jinja template from path: {0}", file.toString());
        if (!file.exists() || !file.isFile()) {
            Log.warn("Jinja template does not exist: {0}", file.toString());
            return null;
        }

        String source = FileUtils.readFileToString(new File(path), charset.name());
        return source;

    }
}
