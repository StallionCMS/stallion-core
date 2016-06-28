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

package io.stallion.dataAccess.db;

import org.apache.commons.io.FilenameUtils;


public class SqlMigration {
    private int versionNumber;
    private String appName;
    private String filename;
    private String source;

    public int getVersionNumber() {
        return versionNumber;
    }

    public SqlMigration setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public SqlMigration setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public SqlMigration setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public String getSource() {
        return source;
    }

    public SqlMigration setSource(String source) {
        this.source = source;
        return this;
    }

    public boolean isJavascript() {
        return FilenameUtils.getExtension(filename).equals("js");
    }
}
