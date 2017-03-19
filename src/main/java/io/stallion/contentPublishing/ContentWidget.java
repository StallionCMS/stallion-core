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

package io.stallion.contentPublishing;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;


public class ContentWidget {
    private String content = "";
    private String originalContent = "";
    private String name = "";
    private String guid = "";
    private Map<String, Object> data = map();
    private Map<String, Object> meta = map();
    private String type = "";
    private List<ContentWidget> widgets = list();

    public String getContent() {
        return content;
    }

    public ContentWidget setContent(String content) {
        this.content = content;
        return this;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public ContentWidget setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
        return this;
    }

    public String getName() {
        return name;
    }

    public ContentWidget setName(String name) {
        this.name = name;
        return this;
    }

    public String getGuid() {
        return guid;
    }

    public ContentWidget setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public ContentWidget setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public ContentWidget setMeta(Map<String, Object> meta) {
        this.meta = meta;
        return this;
    }

    public String getType() {
        return type;
    }

    public ContentWidget setType(String type) {
        this.type = type;
        return this;
    }

    public List<ContentWidget> getWidgets() {
        return widgets;
    }

    public ContentWidget setWidgets(List<ContentWidget> widgets) {
        this.widgets = widgets;
        return this;
    }
}
