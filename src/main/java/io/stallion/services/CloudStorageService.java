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

import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import io.stallion.settings.Settings;

import java.util.Map;

import static io.stallion.utils.Literals.*;


public abstract class CloudStorageService {
    private static CloudStorageService _instance;

    public static CloudStorageService instance() {
        if (_instance == null) {
            load();
        }
        return _instance;
    }

    public static void load() {
        if (Settings.instance().getCloudStorage() == null) {
            throw new ConfigException("[cloudStorage] section of stallion.toml is empty. Cannot initialize cloud storage.");
        }
        if (empty(Settings.instance().getCloudStorage().getJavaClass())) {
            _instance = new S3StorageService();
        } else {
            throw new UsageException("Loading cloud controller dynamically not yet implemented.");
        }

    }

    public static void load(CloudStorageService controller) {
        _instance = controller;
    }


    public abstract String getSignedUploadUrl(String bucket, String fileKey, String contentType, Map headers);
}
