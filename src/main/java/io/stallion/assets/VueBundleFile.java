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
import java.util.List;
import java.util.Map;

import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.ResourceHelpers;
import io.stallion.utils.json.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;


public class VueBundleFile extends ComboBundleFile {

    @Override
    protected void process() {
        boolean debugMode = Settings.instance().getBundleDebug();
        String url = getLiveUrl();
        String query;
        String rawContent = "";
        String css = "";

        if (debugMode) {
            url = getDebugUrl();
        }
        if (url.contains(":")) {
            String[] parts = StringUtils.split(url, ":", 2);
            setPluginName(parts[0]);
            url = parts[1];
        }
        if (url.contains("?")) {
            String[] parts = StringUtils.split(url, "?", 2);
            url = parts[0];
            query = parts[1];
            setQuery(query);
        }



        try {
            if (empty(getPluginName())) {
                String fullPath = Settings.instance().getTargetFolder() + url;
                rawContent = FileUtils.readFileToString(new File(fullPath), "utf-8");
            } else {
                rawContent = ResourceHelpers.loadAssetResource(getPluginName(), "/assets/" + url);
            }
        } catch (IOException e) {
            Log.exception(e, "Error loading bundle file " + url);
            throw new RuntimeException(e);
        }
        setRawContent(rawContent);

        int index = rawContent.indexOf("<style>");
        int lastIndex = rawContent.indexOf("</style>", index + 7);

        if (index != -1 && lastIndex > index) {
            css = rawContent.substring(index + 7, lastIndex);
        }
        setCss(css);


        index = rawContent.indexOf("<template>");
        lastIndex = rawContent.lastIndexOf("</template>");
        String template = "";

        if (index != -1 && lastIndex > index) {
            template = rawContent.substring(index + 10, lastIndex);
        }

        index = rawContent.indexOf("<script>");
        lastIndex = rawContent.lastIndexOf("</script>");
        String script = "";
        int linesToAdd = 0;
        if (index != -1 && lastIndex > index) {
            script = rawContent.substring(index + 8, lastIndex);
            String before = rawContent.substring(0, index);
            linesToAdd = StringUtils.countMatches(before, "\n") - 1;
            if (linesToAdd < 0) {
                linesToAdd = 0;
            }
        }

        String tag = FilenameUtils.removeExtension(FilenameUtils.getName(url));
        String templateJson = JSON.stringify(template.trim());

        script = "(function() {" +
                "var module = {exports: {}};\n" +
                StringUtils.repeat("\n", linesToAdd) +
                script + "\n";
        script += "module.exports.template = " + templateJson + ";\n";
        script += "window.vueComponents = window.vueComponents || {};\n";
        script += "window.vueComponents['"+ tag + "'] = Vue.component('" + tag + "', module.exports);\n";

        script += "})();";
        setJavascript(script);


    }

}
