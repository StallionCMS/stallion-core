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

package io.stallion.dataAccess.file;

import java.util.HashMap;


public class StElement {
    private String content = "";
    private String rawInnerContent = "";
    private String tagAttributesString = "";
    private String id = "";
    private String elementType = "";
    private String tag = "";
    private HashMap <String, String> attributes = new HashMap <String, String>();


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRawInnerContent() {
        return rawInnerContent;
    }

    public void setRawInnerContent(String rawInnerContent) {
        this.rawInnerContent = rawInnerContent;
    }

    public String getTagAttributesString() {
        return tagAttributesString;
    }

    public void setTagAttributesString(String tagAttributesString) {
        this.tagAttributesString = tagAttributesString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }
}
