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

package io.stallion.services;

import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static io.stallion.utils.Literals.*;


public class LogFilter implements Filter {
    private Map<String, Level> packageLogLevelMap = new HashMap<>();
    private Level defaultLevel = Level.INFO;

    public LogFilter(Level defaultLevel, Map<String, String> packageLogLevelMap) {
        for(Map.Entry<String, String> entry: packageLogLevelMap.entrySet()) {
            String packageName = entry.getKey().replace("io.stallion", ".");
            this.packageLogLevelMap.put(packageName, Level.parse(entry.getValue().toUpperCase()));
        }
        this.defaultLevel = defaultLevel;
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        Level overrideLevel = packageLogLevelMap.getOrDefault(toPackageName(record), null);
        if (overrideLevel != null) {
            if (record.getLevel().intValue() >= overrideLevel.intValue()) {
                return true;
            } else {
                return false;
            }
        }
        return record.getLevel().intValue() >= defaultLevel.intValue();
    }

    private String toPackageName(LogRecord record) {
        int i = record.getSourceClassName().lastIndexOf(".");
        if (i == -1) {
            return "";
        }
        return record.getSourceClassName().substring(0, i);
    }
}
