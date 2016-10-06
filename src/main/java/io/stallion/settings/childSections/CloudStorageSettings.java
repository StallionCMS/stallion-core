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

package io.stallion.settings.childSections;

import io.stallion.services.S3StorageService;
import io.stallion.settings.SettingMeta;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class CloudStorageSettings implements SettingsSection {
    @SettingMeta
    private String accessToken;
    @SettingMeta
    private String secret;
    @SettingMeta(val="")
    private String javaClass;

    /**
     * The secret token or password used to connect to the cloud service.
     *
     * @return
     */
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * The access token or user name used to connect to the cloud service
     * @return
     */
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * The Java subclass of io.stallion.services.CloudStorageService used to connect to the cloud service.
     *
     * @return
     */
    public String getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }
}
