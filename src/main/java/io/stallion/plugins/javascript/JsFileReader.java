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

package io.stallion.plugins.javascript;

import io.stallion.exceptions.UsageException;
import io.stallion.settings.Settings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class JsFileReader {
    public static String readToString(String file, String pluginFolder) {
        if (!file.startsWith(Settings.instance().getTargetFolder() + "/js")
                && !file.startsWith(Settings.instance().getTargetFolder() + "/plugins")) {
            throw new UsageException("You can cannot access the file " + file + " from this location");
        }
        if (!empty(pluginFolder)) {
            if (!file.startsWith(pluginFolder)) {
                throw new UsageException("File " + file + " is not in the javascript folder " + pluginFolder);
            }
        }
        if (!file.endsWith(".js")) {
            throw new UsageException("This is not a .js file: " + file);
        }
        try {
            return FileUtils.readFileToString(new File(file), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
