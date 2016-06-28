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
import io.stallion.utils.StallionClassLoader;

import java.util.Set;

import static io.stallion.utils.Literals.set;


public class SandboxedClassLoader {

    private Sandbox sandbox;

    public SandboxedClassLoader(Sandbox box) {
        this.sandbox = box;
    }

    public Class loadClass(String className) {
        boolean allowed = false;
        if (DEFAULT_WHITE_LIST.contains(className)) {
            allowed = true;
        }
        if (sandbox.getWhitelist().getClasses().contains(className)) {
            allowed = true;
        }
        if (allowed) {
            return StallionClassLoader.loadClass(className);
        }
        throw new UsageException("Requested class " + className + "was not on white list");
    }

    public static final Set<String> DEFAULT_WHITE_LIST = set(
            "java.lang.Long",
            "java.lang.Integer",
            "java.lang.String",
            "java.lang.Boolean",
            "io.stallion.exceptions.ClientException",
            "io.stallion.exceptions.UsageException",
            "io.stallion.exceptions.WebException",
            "io.stallion.exceptions.ConfigException",
            "io.stallion.utils.json.JSON"
    );

}
