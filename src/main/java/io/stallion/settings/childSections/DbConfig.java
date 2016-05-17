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

package io.stallion.settings.childSections;

import io.stallion.settings.SettingMeta;


public class DbConfig implements SettingsSection {
    @SettingMeta()
    private String username;
    @SettingMeta
    private String url;
    @SettingMeta
    private String password;
    @SettingMeta(val = "com.mysql.jdbc.Driver")
    private String driverClass;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbAccessorClass() {
        return dbAccessorClass;
    }

    public DbConfig setDbAccessorClass(String dbAccessorClass) {
        this.dbAccessorClass = dbAccessorClass;
        return this;
    }

    @SettingMeta(val = "io.stallion.dataAccess.db.DB")
    private String dbAccessorClass;

    public String getDriverClass() {
        return driverClass;
    }

    public DbConfig setDriverClass(String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

}
