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

package io.stallion.requests;


public class Site {
    private String url;
    private String name;
    private String title;
    private String metaDescription;

    public String getUrl() {
        return url;
    }

    public Site setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getName() {
        return name;
    }

    public Site setName(String name) {
        this.name = name;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Site setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return getMetaDescription();
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public Site setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
        return this;
    }
}
