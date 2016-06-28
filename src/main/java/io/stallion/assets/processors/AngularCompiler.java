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

package io.stallion.assets.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * Converts an angular HTML template into javascript.
 */
public class AngularCompiler {

    public static String htmlToJs(String html, String path) {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> htmlMap = new HashMap<>();
        htmlMap.put("html", html);
        String mapJson = null;
        try {
            mapJson = mapper.writeValueAsString(htmlMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing angular template: " + path, e);
        }

        String js = "(function() { var node = document.getElementById('angularHtmlInsertionPoint');\n";
        js += "if (!node) { node = document.getElementsByTagName(\"body\")[0]; }\n";
        js += String.format("var data = %s;\n", mapJson);
        js += "var script = document.createElement('script');\n";
        js += "script.setAttribute('type', 'text/ng-template');\n";
        if (path.contains("?")) {
            path = StringUtils.split(path, "?", 2)[0];
        }
        js += String.format("script.setAttribute('id', '%s');\n", path);
        js += String.format("script.appendChild(document.createTextNode(data.html));\n");
        js += String.format("node.appendChild(script)");
        js += "}());\n";
        return js;
    }
}
