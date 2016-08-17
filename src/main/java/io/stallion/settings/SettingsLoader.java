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

package io.stallion.settings;

import com.moandjiezana.toml.Toml;
import io.stallion.Context;
import io.stallion.boot.CommandOptionsBase;
import io.stallion.reflection.PropertyUtils;
import io.stallion.secrets.SecretsVault;
import io.stallion.services.Log;
import io.stallion.settings.childSections.SettingsSection;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.empty;


public class SettingsLoader  {

    private Map<String, String> secrets;

    public <T extends ISettings> T loadSettings(String baseName, Class<T> settingsClass) {
        String env = Context.settings().getEnv();
        return loadSettings(env, Context.settings().getTargetFolder(), baseName, settingsClass);
    }

    public <T extends ISettings> T loadSettings(String env, String targetFolder, String baseName, Class<T> settingsClass)  {
        return loadSettings(env, targetFolder, baseName, settingsClass, null);
    }

    public <T extends ISettings> T loadSettings(String env, String targetFolder, String baseName, Class<T> settingsClass, CommandOptionsBase options)  {
        String basePath = targetFolder + "/conf/" + baseName;

        // Load the default settings
        Log.fine("Loading default toml settings.");
        String userName = System.getProperty("user.name");
        if (userName == null) {
            userName = "emptyuser";
        }


        // Settings load order, later settings override earlier settings
        // 1. stallion.toml
        // 2. stallion.user-(username).toml
        // 3. stallion.env.toml
        // 4. stallion.env.user-(username).toml
        File baseFile = new File(basePath + ".toml");
        File userFile = new File(basePath + ".user-" + userName + ".toml");
        File envFile = new File(basePath + "." + env + ".toml");
        File envUserFile = new File(basePath + "." + env + ".user-" + userName + ".toml");

        T settings = null;
        try {
            settings = (T)settingsClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }




        Toml toml = new Toml();
        Toml tomlOrg = null;
        if (baseFile.exists()) {
            Log.info("Loading base settings file: {0}", baseFile.getPath());
            toml = new Toml().read(baseFile);
            tomlOrg = toml;
        }



        if (envFile.exists()) {
            Log.finer("Env file exists {0}, merging", envFile.getPath());
            toml = new Toml(toml).read(envFile);
        }


        settings = toml.to(settingsClass);

        // Hack because the toml library can only do one level of merging
        // toml files. Thus, need to manually merge this third file.
        if (envUserFile.exists()) {
            Toml localToml = new Toml().read(envUserFile);
            T localSettings = localToml.to(settingsClass);
            for(Map.Entry<String, Object> entry: localToml.entrySet()) {
                PropertyUtils.setProperty(
                        settings, entry.getKey(),
                        PropertyUtils.getPropertyOrMappedValue(localSettings, entry.getKey()));
            }
        }


        if (settings instanceof Settings) {
            ((Settings)settings).setEnv(env);
            ((Settings)settings).setTargetFolder(targetFolder);
            SecretsVault.init(targetFolder, ((Settings) settings).getSecrets());
        }

        if (options != null && settings instanceof  Settings) {
            options.hydrateSettings((Settings)settings);
        }


        settings.assignDefaults();

        try {
            assignDefaultsFromAnnotations(settings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Log.finer("Settings Loaded. {0}", settings);

        return settings;
    }

    public void assignDefaultsFromAnnotations(Object settings) throws IllegalAccessException, InstantiationException {
        Class cls = settings.getClass();
        for(Field field: cls.getDeclaredFields()) {
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            if (SettingsSection.class.isAssignableFrom(field.getType())) {
                Object value = field.get(settings);
                if (value == null) {
                    value = field.getType().newInstance();
                    field.set(settings, value);
                }
                assignDefaultsFromAnnotations(value);
                field.setAccessible(isAccessible);
                ((SettingsSection)value).postLoad();
                continue;
            }
            SettingMeta[] metas = field.getAnnotationsByType(SettingMeta.class);
            if (metas.length == 0) {
                field.setAccessible(isAccessible);
                continue;
            }
            SettingMeta meta = metas[0];
            Object value = field.get(settings);
            Class type = field.getType();

            if (value instanceof String && value != null && ((String) value).startsWith("secret:::")) {
                String secretName = ((String) value).substring(9);
                Log.info("Load secret {0} ", secretName);
                value = SecretsVault.getAppSecrets().getOrDefault(secretName, null);
                field.set(settings, value);
            }


            if (value == null) {
                if (!empty(meta.useField())) {
                    field.set(settings, PropertyUtils.getProperty(settings, meta.useField()));
                } else if (type == String.class) {
                    field.set(settings, meta.val());
                } else if (type == Integer.class) {
                    field.set(settings, meta.valInt());
                } else if (type == Long.class) {
                    field.set(settings, meta.valLong());
                } else if (type == Boolean.class) {
                    field.set(settings, meta.valBoolean());
                } else if (meta.cls() != null) {
                    Object o = meta.cls().newInstance();
                    field.set(settings, o);
                } else {
                    Log.warn("Field " + field.getName() + " on settings class " + settings.getClass() + " is null and has no matching class initializer.");
                }
            }
            field.setAccessible(isAccessible);
        }
    }

    /**
     * Merges all values defined in as not-null in overrides, into defaults
     * Operates recursively
     * @param existing
     * @param overrides
     */
    public void mergeObjects(Object existing, Object overrides) {
        if (existing instanceof Map && overrides instanceof Map) {
            Map<String, Object> prevMap = (Map<String, Object>)existing;
            Map<String, Object> newMap = (Map<String, Object>)overrides;
            for (Map.Entry<String, Object> item: newMap.entrySet()) {
                prevMap.put(item.getKey(), item.getValue());
            }
            return;
        } else {
            for (Map.Entry<String, Object> entry : PropertyUtils.getProperties(overrides).entrySet()) {
                // If the override was not defined, we continue
                Object overrideValue = entry.getValue();
                if (overrideValue == null) {
                    continue;
                }
                if (!(overrideValue instanceof SettingsSection)) {
                    PropertyUtils.setProperty(existing, entry.getKey(), entry.getValue());
                    continue;
                }

                Object existingValue = PropertyUtils.getProperty(existing, entry.getKey());
                // The existingValue is either null or the default
                if (existingValue == null) {
                    PropertyUtils.setProperty(existing, entry.getKey(), entry.getValue());
                    continue;
                }
                // If the default never defined the section, we set it, otherwise we merge
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> vMap = (Map<String, Object>) entry.getValue();
                    for (Map.Entry<String, Object> subEntry : vMap.entrySet()) {
                        Map currentMap = (Map) existingValue;
                        currentMap.put(subEntry.getKey(), subEntry.getValue());
                    }
                    continue;
                }
                mergeObjects(existingValue, overrideValue);

            }
        }
    }
}
