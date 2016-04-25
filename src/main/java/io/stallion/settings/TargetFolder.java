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

package io.stallion.settings;

import io.stallion.Context;
import org.apache.commons.lang3.StringUtils;

import java.io.File;


public class TargetFolder {
    private String type = "";
    private String path = "";
    private String defaultTemplate = "";
    private String className = "";
    private String relativePath = "";
    private String fullPath = "";
    private Boolean writable = false;

    public String getType() {
        return type;
    }

    public TargetFolder setType(String type) {
        this.type = type;
        return this;
    }

    public String getPath() {
        return path;
    }

    public TargetFolder setPath(String path) {
        this.path = path;
        return this;
    }

    public String getDefaultTemplate() {
        return defaultTemplate;
    }

    public TargetFolder setDefaultTemplate(String defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public TargetFolder setClassName(String className) {
        this.className = className;
        return this;
    }

    public TargetFolder hydratePaths() {
        if (StringUtils.isEmpty(path)) {
            return this;
        }
        if (path.startsWith("/")) {
            setFullPath(path);
            setRelativePath(new File(path).getName());
        } else {
            if (StringUtils.isEmpty(getFullPath())) {
                setFullPath(Context.settings().getTargetFolder() + "/" + path);
            }
            setRelativePath(path);
        }
        return this;
    }


    public String getRelativePath() {
        return relativePath;
    }

    public TargetFolder setRelativePath(String relativePath) {
        this.relativePath = relativePath;
        return this;
    }

    public String getFullPath() {
        return fullPath;
    }

    public TargetFolder setFullPath(String fullPath) {
        this.fullPath = fullPath;
        return this;
    }

    public Boolean getWritable() {
        return writable;
    }

    public TargetFolder setWritable(Boolean writable) {
        this.writable = writable;
        return this;
    }
}
