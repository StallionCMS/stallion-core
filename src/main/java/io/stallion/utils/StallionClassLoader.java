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

package io.stallion.utils;

import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.plugins.PluginRegistry;

public class StallionClassLoader {

    /**
    * Loads a class based on the name, looking in the Stallion jar classpath, and then
    * also looking up via the class loader for all plugins
    */
    public static Class loadClass(String className) {
        Class cls = null;
        try {
            cls = StallionClassLoader.class.getClassLoader().loadClass(className);
            return cls;
        } catch (ClassNotFoundException e) {

        }
        for (StallionJavaPlugin booter: PluginRegistry.instance().getJavaPluginByName().values()) {
            try {
                cls = StallionClassLoader.class.getClassLoader().loadClass(className);
                return cls;
            } catch (ClassNotFoundException e) {

            }
        }
        return null;
    }

    /**
     * Load a class using the class loader of the given plugin
     *
     * @param pluginName
     * @param className
     * @return
     */
    public static Class loadClass(String pluginName, String className) {
        try {
            Class cls = PluginRegistry.instance().getJavaPluginByName().get(pluginName).getClass().getClassLoader().loadClass(className);
            return cls;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
