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

package io.stallion.requests;

import io.stallion.Context;
import io.stallion.dataAccess.MappedModel;
import io.stallion.dataAccess.ModelBase;
import io.stallion.utils.rss.RssLink;
import io.stallion.settings.Settings;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.stallion.utils.Literals.*;


public class MetaInformation extends ModelBase {
    private String title = "";
    private String description = "";
    private String author = "";
    private String image = "";
    private String siteName = "";
    private String canonicalUrl = "";
    private Set<RssLink> rssLinks = new HashSet<RssLink>();
    private String ogType = "";
    private List<String> cssClasses = list();
    private String bodyCssId = "";
    private Set<String> footerJavascripts = new LinkedHashSet<>();
    private Set<String> headerStylesheets = new LinkedHashSet<>();
    private Set<String> footerBundles = new LinkedHashSet<>();
    private Set<String> headerBundles = new LinkedHashSet<>();


    public String getTitle() {
        if (empty(title)) {
            return Context.getSettings().getDefaultTitle();
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        if (empty(description)) {
            return Context.getSettings().getMetaDescription();
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getAuthor() {
        if (empty(author)) {
            return Context.getSettings().getAuthorName();
        }
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSiteName() {
        if (empty(siteName)) {
            Context.getSettings().getSiteName();
        }
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getUrl() {
        if (!empty(getCanonicalUrl())) {
            return getCanonicalUrl();
        } else {
            return Context.getRequest().requestUrl();
        }
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public String getOgType() {
        return ogType;
    }

    public void setOgType(String ogType) {
        this.ogType = ogType;
    }

    public List<String> getCssClasses() {
        return cssClasses;
    }

    public void setCssClasses(List<String> cssClasses) {
        this.cssClasses = cssClasses;
    }

    public String getBodyCssId() {
        return bodyCssId;
    }

    public void setBodyCssId(String bodyCssId) {
        this.bodyCssId = bodyCssId;
    }

    public Set<RssLink> getRssLinks() {
        return rssLinks;
    }

    public void setRssLinks(Set<RssLink> rssLinks) {
        this.rssLinks = rssLinks;
    }

    public String getGenerator() {
        return or(Settings.instance().getMetaGenerator(), "Stallion");
    }


}

