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

package io.stallion.services;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.db.DB;
import io.stallion.settings.Settings;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


public class DynamicSettings {
    private static DynamicSettings _instance;

    public static DynamicSettings instance() {
        return _instance;
    }

    public static void load() {
        if (_instance != null) {
            return;
        }
        _instance = new DynamicSettings();
        _instance.initGroup("core");
        // Query the database for records
    }
    public static void shutdown() {
        _instance = null;
    }

    private boolean dbAvailable = false;
    private Map<String, Map<String, Object>> settingsMap = map();
    private Map<String, Map<String, Object>> settingsParsedObjectMap = map();
    private ZonedDateTime lastSyncAt = null;
    private String directory;

    DynamicSettings() {
        dbAvailable = DB.available();
        directory = Settings.instance().getDataDirectory() + "/dynamic-settings";
        new File(directory).mkdirs();
        loadAll();
    }

    public void initGroup(String group) {
        group = GeneralUtils.slugify(group);
        if (!settingsMap.containsKey(group)) {
            settingsMap.put(group, map());
        }
        if (!settingsParsedObjectMap.containsKey(group)) {
            settingsParsedObjectMap.put(group, map());
        }
    }

    public DynamicSettings put(String group, String name, String value) {
        group = GeneralUtils.slugify(group);
        if (!settingsMap.containsKey(group)) {
            synchronized (settingsMap) {
                if (!settingsMap.containsKey(group)) {
                    settingsMap.put(group, map());
                    settingsParsedObjectMap.put(group, map());
                }

            }
        }
        if (!settingsParsedObjectMap.containsKey(group)) {
            settingsParsedObjectMap.put(group, map());
            synchronized (settingsParsedObjectMap) {
                if (!settingsParsedObjectMap.containsKey(group)) {
                    settingsParsedObjectMap.put(group, map());
                }
            }
        }

        settingsMap.get(group).put(name, value);
        settingsParsedObjectMap.get(group).put(name, null);

        persist(group, name, value);

        return this;
    }

    private void persist(String group, String name, String value) {
        if (dbAvailable) {
            persistToDatabase(group, name, value);
        } else {
            persistToFile(group, name, value);
        }
    }

    private void persistToDatabase(String group, String name, String value) {
        // Save to the database or to file
        DB.instance().execute(
                "INSERT INTO stallion_dynamic_settings (`group`, `name`, `value`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `value`=VALUES(`value`)",
                group, name, value
        );
    }

    private void persistToFile(String group, String name, String value) {
        String fileName = directory + "/" + GeneralUtils.slugify(group) + ".json";
        try {
            FileUtils.write(new File(fileName), JSON.stringify(settingsMap.get(group)), "UTF8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getParsedObject(String group, String name) {
        group = GeneralUtils.slugify(group);
        if (!settingsParsedObjectMap.containsKey(group)) {
            return null;
        }
        return settingsParsedObjectMap.get(group).getOrDefault(name, null);
    }

    public DynamicSettings stashParsedObject(String group, String name, Object value) {
        group = GeneralUtils.slugify(group);
        if (!settingsParsedObjectMap.containsKey(group)) {
            settingsParsedObjectMap.put(group, map());
        }
        settingsParsedObjectMap.get(group).put(name, value);
        return this;
    }

    public String get(String group, String name) {
        group = GeneralUtils.slugify(group);
        checkLoadUpdated();
        if (!settingsMap.containsKey(group)) {
            return null;
        }
        Map<String, Object> groupMap = settingsMap.get(group);
        Object val = groupMap.getOrDefault(name, null);
        if (val != null) {
            if (val instanceof String) {
                return (String)val;
            } else {
                return val.toString();
            }
        } else {
            return null;
        }
    }


    public void checkLoadUpdated() {
        checkLoadUpdated(utcNow());
    }

    public void checkLoadUpdated(ZonedDateTime now) {
        if (!dbAvailable) {
            return;
        }
        if (lastSyncAt == null || lastSyncAt.isBefore(now.minusSeconds(90))) {
            synchronized (this) {
                if (lastSyncAt == null || lastSyncAt.isBefore(now.minusSeconds(90))) {
                    ZonedDateTime since = lastSyncAt;
                    loadUpdated(since);
                    lastSyncAt = now;
                }
            }
        }
    }

    public void loadUpdated(ZonedDateTime since) {
        List<DynamicSetting> settings = list();
        if (since == null) {
            settings = DB.instance().queryBean(
                    DynamicSetting.class,
                    "SELECT * FROM stallion_dynamic_settings"
            );
        } else {
            settings = DB.instance().queryBean(
                    DynamicSetting.class,
                    "SELECT * FROM stallion_dynamic_settings WHERE row_updated_at>=?",
                    DateUtils.SQL_FORMAT.format(since)
            );
        }

        for (DynamicSetting hs: settings) {
            if (!settingsMap.containsKey(hs.getGroup())) {
                settingsMap.put(hs.getGroup(), map());
            }
            if (!settingsParsedObjectMap.containsKey(hs.getGroup())) {
                settingsParsedObjectMap.put(hs.getGroup(), map());
            }
            Object original = settingsMap.get(hs.getGroup()).getOrDefault(hs.getName(), null);
            settingsMap.get(hs.getGroup()).put(hs.getName(), hs.getValue());

            if (original == null || !original.equals(hs.getValue())) {
                settingsParsedObjectMap.get(hs.getGroup()).put(hs.getName(), null);
            }
        }
    }

    public void loadAll() {
        if (dbAvailable) {
            loadUpdated(null);
        } else {
            File[] files = new File(directory).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".json");
                }
            });
            for (File file: files) {
                try {
                    String group = FilenameUtils.getBaseName(file.getAbsolutePath());
                    String json = FileUtils.readFileToString(file, "UTF8");
                    settingsMap.put(group, JSON.parseMap(json));
                    settingsParsedObjectMap.put(group, map());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
