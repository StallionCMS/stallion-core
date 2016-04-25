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

package io.stallion.plugins;

import io.stallion.Context;
import io.stallion.services.Log;
import io.stallion.settings.ISettings;
import io.stallion.settings.SettingsLoader;

import java.util.Map;

import static io.stallion.utils.Literals.map;

/**
 * A base class for defining plugin settings. Uses hierarchical settings loader.
 */
public abstract class BasePluginSettings implements ISettings {
    private static Map<String, BasePluginSettings> instanceByName = map();

    public static  <T extends BasePluginSettings> T getInstance(Class<T> cls, String pluginName) {
        BasePluginSettings instance = instanceByName.getOrDefault(pluginName, null);
        if (instance == null) {
            SettingsLoader loader = new SettingsLoader();
            try {
                instance = loader.loadSettings(pluginName, cls);
                instanceByName.put(pluginName, instance);
            } catch (Exception e) {
                Log.exception(e, "Error loading plugin settings");
            }
        }
        return (T)instance;
    }
}
