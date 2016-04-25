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

package io.stallion.plugins.javascript;

import com.moandjiezana.toml.Toml;
import io.stallion.services.Log;
import io.stallion.settings.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class Sandbox {

    public static Sandbox allPermissions() {
        return new Sandbox()
                .setCanReadAllData(true)
                .setCanWriteAllData(true)
                .setUsers(new Users().setCanAccess(true).setCanWriteDb(true))
                .setWhitelist(new Whitelist())
                ;
    }

    public static Sandbox forPlugin(String plugin) {
        return fromPath(Settings.instance().getTargetFolder() + "/plugins/" + plugin + "/sandbox.toml");
    }

    public static Sandbox fromPath(String path) {
        return fromFile(new File(path));
    }
    public static Sandbox fromFile(File file) {
        Log.info("Look for sandbox.toml for file {0}", file.getAbsolutePath());
        if (!file.isFile()) {
            return null;
        }
        Log.info("Loadding sandbox.toml for file {0}", file.getAbsolutePath());
        Toml boxToml = new Toml().read(file);
        Sandbox box = boxToml.to(Sandbox.class);
        Toml users = boxToml.getTable("users");
        if (empty(users)) {
            box.setUsers(new Users());
        } else {
            box.setUsers(users.to(Users.class));
        }
        Toml whitelist = boxToml.getTable("whitelist");
        if (empty(whitelist)) {
            box.setWhitelist(new Whitelist());
        } else {
            box.setWhitelist(whitelist.to(Whitelist.class));
        }
        return box;
    }

    private boolean canWriteAllData = false;
    private boolean canReadAllData = false;
    private Users users;
    private Whitelist whitelist;


    public boolean isCanReadAllData() {
        return canReadAllData;
    }

    public Sandbox setCanReadAllData(boolean canReadAllData) {
        this.canReadAllData = canReadAllData;
        return this;
    }

    public boolean isCanWriteAllData() {
        return canWriteAllData;
    }

    public Sandbox setCanWriteAllData(boolean canWriteAllData) {
        this.canWriteAllData = canWriteAllData;
        return this;
    }

    public Users getUsers() {
        return users;
    }

    public Sandbox setUsers(Users users) {
        this.users = users;
        return this;
    }

    public Whitelist getWhitelist() {
        return whitelist;
    }

    public Sandbox setWhitelist(Whitelist whitelist) {
        this.whitelist = whitelist;
        return this;
    }

    public static class Whitelist {
        private List<String> cookies = list();
        private List<String> headers = list();
        private List<String> classes = list();
        private List<String> readBuckets = list();
        private List<String> writeBuckets = list();

        public List<String> getCookies() {
            return cookies;
        }

        public Whitelist setCookies(List<String> cookies) {
            this.cookies = cookies;
            return this;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public Whitelist setHeaders(List<String> headers) {
            this.headers = headers;
            return this;
        }

        public List<String> getClasses() {
            return classes;
        }

        public Whitelist setClasses(List<String> classes) {
            this.classes = classes;
            return this;
        }

        public List<String> getReadBuckets() {
            return readBuckets;
        }

        public Whitelist setReadBuckets(List<String> readBuckets) {
            this.readBuckets = readBuckets;
            return this;
        }

        public List<String> getWriteBuckets() {
            return writeBuckets;
        }

        public Whitelist setWriteBuckets(List<String> writeBuckets) {
            this.writeBuckets = writeBuckets;
            return this;
        }
    }


    public static class Users {
        private boolean canAccess = false;
        private boolean canReadDb = false;
        private boolean canWriteDb = false;

        public boolean isCanAccess() {
            return canAccess;
        }

        public Users setCanAccess(boolean canAccess) {
            this.canAccess = canAccess;
            return this;
        }

        public boolean isCanReadDb() {
            return canReadDb;
        }

        public Users setCanReadDb(boolean canReadDb) {
            this.canReadDb = canReadDb;
            return this;
        }

        public boolean isCanWriteDb() {
            return canWriteDb;
        }

        public Users setCanWriteDb(boolean canWriteDb) {
            this.canWriteDb = canWriteDb;
            return this;
        }
    }
}
