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


public interface VueBundleFile extends BundleFileBase {


    public default void hydrateVueComboContent() {
        Log.finer("Hydrate style & script for Vue file " + getCurrentPath());

        String content = getProcessedContent();
        String css = "";

        int index = content.indexOf("<style>");
        int lastIndex = content.indexOf("</style>", index + 7);

        if (index != -1 && lastIndex > index) {
            css = content.substring(index + 7, lastIndex);
        }
        setCss(css);


        index = content.indexOf("<template>");
        lastIndex = content.lastIndexOf("</template>");
        String template = "";

        if (index != -1 && lastIndex > index) {
            template = content.substring(index + 10, lastIndex);
        }

        index = content.indexOf("<script>");
        lastIndex = content.lastIndexOf("</script>");
        String script = "";
        int linesToAdd = 0;
        if (index != -1 && lastIndex > index) {
            script = content.substring(index + 8, lastIndex);
            String before = content.substring(0, index);
            linesToAdd = StringUtils.countMatches(before, "\n") - 1;
            if (linesToAdd < 0) {
                linesToAdd = 0;
            }
        }

        String tag = FilenameUtils.removeExtension(FilenameUtils.getName(getCurrentPath()));
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
