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

import io.stallion.exceptions.ConfigException;
import io.stallion.settings.SettingMeta;

import static io.stallion.utils.Literals.empty;


public class DbConfig implements SettingsSection {
    @SettingMeta()
    private String username;
    @SettingMeta
    private String url;
    @SettingMeta
    private String password;
    @SettingMeta()
    private String driverClass;
    @SettingMeta()
    private String implementationClass;

    @Override
    public void postLoad() {
        if (empty(driverClass) && !empty(url)) {

            if (url.contains("jdbc:mysql")) {
                driverClass = "com.mysql.jdbc.Driver";
            } else if (url.contains("jdbc:postgresql")) {
                driverClass = "org.postgresql.Driver";
            } else {
                throw new ConfigException("No database driverClass defined, and could not guess driver class from url " + url);
            }
        }
        if (empty(implementationClass) && !empty(url)) {
            if (url.contains("jdbc:mysql")) {
                implementationClass = "io.stallion.dataAccess.db.mysql.MySqlDbImplementation";
            } else if (url.contains("jdbc:postgresql")) {
                implementationClass = "io.stallion.dataAccess.db.postgres.PostgresDbImplementation";
            } else {
                throw new ConfigException("No database implementation class defined (implements io.stallion.dataAccess.db.DbImplementation), and could not guess class from url " + url);
            }

        }
    }

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

    public String getImplementationClass() {
        return implementationClass;
    }

    public DbConfig setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
        return this;
    }
}
