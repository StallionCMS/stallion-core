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
                // Since MySql driver is GPL, have to package the maria db driver
                //driverClass = "com.mysql.jdbc.Driver";
                driverClass = "org.mariadb.jdbc.Driver";
            } else if (url.contains("jdbc:mariadb")) {
                driverClass = "org.mariadb.jdbc.Driver";
            } else if (url.contains("jdbc:postgresql")) {
                driverClass = "org.postgresql.Driver";
            } else {
                throw new ConfigException("No database driverClass defined, and could not guess driver class from url " + url);
            }
        }
        if (empty(implementationClass) && !empty(url)) {
            if (url.contains("jdbc:mysql") || url.contains("jdbc:mariadb")) {
                implementationClass = "io.stallion.dataAccess.db.mysql.MySqlDbImplementation";
            } else if (url.contains("jdbc:postgresql")) {
                implementationClass = "io.stallion.dataAccess.db.postgres.PostgresDbImplementation";
            } else {
                throw new ConfigException("No database implementation class defined (implements io.stallion.dataAccess.db.DbImplementation), and could not guess class from url " + url);
            }

        }
    }

    /**
     * Get the database username
     * @return
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the JDBC URL connection for the database
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the password for connecting to the database
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Use a custom subclass of io.stallion.dataAccess.db.DB for accessing the database.
     *
     * @return
     */
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

    /**
     * Override the default JDBC driver class for the database URL. This should be a full
     * path including the package and class name.
     *
     * @return
     */
    public DbConfig setDriverClass(String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

    /**
     * The subclass of DbImplementation that DB will use for help with customizing database access for the given
     * type of DB. If you are using something other than MySQL or Postgres, you will need to create a subclass
     * for the engine you are connecting to.
     *
     * @return
     */
    public String getImplementationClass() {
        return implementationClass;
    }

    public DbConfig setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
        return this;
    }
}
