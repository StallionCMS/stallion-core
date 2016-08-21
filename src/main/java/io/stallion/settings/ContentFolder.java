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

package io.stallion.settings;

import io.stallion.Context;
import org.apache.commons.lang3.StringUtils;

import java.io.File;


public class ContentFolder {
    private String type = "";
    private String path = "";
    private String itemTemplate = "";
    private String className = "";
    private String relativePath = "";
    private String fullPath = "";
    private Boolean writable = false;
    private boolean listingEnabled = false;
    private String listingRootUrl = "";
    private String listingTemplate = "";
    private String listingTitle = "";
    private String listingMetaDescription = "";
    private int itemsPerPage = 10;


    public String getType() {
        return type;
    }

    public ContentFolder setType(String type) {
        this.type = type;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ContentFolder setPath(String path) {
        this.path = path;
        return this;
    }

    public String getItemTemplate() {
        return itemTemplate;
    }

    public ContentFolder setItemTemplate(String itemTemplate) {
        this.itemTemplate = itemTemplate;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public ContentFolder setClassName(String className) {
        this.className = className;
        return this;
    }

    public ContentFolder hydratePaths() {
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

    public ContentFolder setRelativePath(String relativePath) {
        this.relativePath = relativePath;
        return this;
    }

    public String getFullPath() {
        return fullPath;
    }

    public ContentFolder setFullPath(String fullPath) {
        this.fullPath = fullPath;
        return this;
    }

    public Boolean getWritable() {
        return writable;
    }

    public ContentFolder setWritable(Boolean writable) {
        this.writable = writable;
        return this;
    }

    public boolean isListingEnabled() {
        return listingEnabled;
    }

    public ContentFolder setListingEnabled(boolean listingEnabled) {
        this.listingEnabled = listingEnabled;
        return this;
    }

    public String getListingRootUrl() {
        return listingRootUrl;
    }

    public ContentFolder setListingRootUrl(String listingRootUrl) {
        this.listingRootUrl = listingRootUrl;
        return this;
    }

    public String getListingTemplate() {
        return listingTemplate;
    }

    public ContentFolder setListingTemplate(String listingTemplate) {
        this.listingTemplate = listingTemplate;
        return this;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public ContentFolder setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
        return this;
    }

    public String getListingMetaDescription() {
        return listingMetaDescription;
    }

    public ContentFolder setListingMetaDescription(String listingMetaDescription) {
        this.listingMetaDescription = listingMetaDescription;
        return this;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public ContentFolder setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        return this;
    }
}
