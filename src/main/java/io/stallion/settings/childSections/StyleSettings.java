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

package io.stallion.settings.childSections;

import io.stallion.settings.SettingMeta;
import io.stallion.settings.Settings;

import static io.stallion.utils.Literals.*;


public class StyleSettings implements SettingsSection {
    @SettingMeta
    private String logoImage;

    @SettingMeta(val="#F9F9F9")
    private String backgroundColor;

    @SettingMeta(val="#FFFFFF")
    private String mainBodyColor;

    @SettingMeta(val="#333333")
    private String highlightColor;

    @SettingMeta(useField="highlightColor")
    private String linkColor;

    @SettingMeta(useField="highlightColor")
    private String logoTextColor;


    @SettingMeta(useField="highlightColor")
    private String primaryButtonColor;


    @SettingMeta(val="'Helvetica Neue', Helvetica, Arial, sans-serif")
    private String headerFont;

    @SettingMeta(val="'Helvetica Neue', Helvetica, Arial, sans-serif")
    private String bodyFont;

    @SettingMeta
    private String customCss;

    @SettingMeta
    private String footer;




    public String getLogoImageUrl() {
        String url = getLogoImage();
        if (empty(url)) {
            return url;
        }
        if (url.contains("{cdnUrl}")) {
            url = url.replace("{cdnUrl}", Settings.instance().getCdnUrl());
        }
        if (url.contains("{port}")) {

            url = url.replace("{port}", Settings.instance().getPort().toString());
        }
        return url;
    }

    public String getLogoImage() {
        return logoImage;
    }

    public StyleSettings setLogoImage(String logoImage) {
        this.logoImage = logoImage;
        return this;
    }


    public String getBackgroundColor() {
        return backgroundColor;
    }

    public StyleSettings setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public String getLogoTextColor() {
        return logoTextColor;
    }

    public StyleSettings setLogoTextColor(String logoTextColor) {
        this.logoTextColor = logoTextColor;
        return this;
    }

    public String getMainBodyColor() {
        return mainBodyColor;
    }

    public StyleSettings setMainBodyColor(String mainBodyColor) {
        this.mainBodyColor = mainBodyColor;
        return this;
    }

    public String getHighlightColor() {
        return highlightColor;
    }

    public StyleSettings setHighlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
        return this;
    }

    public String getLinkColor() {
        return linkColor;
    }

    public StyleSettings setLinkColor(String linkColor) {
        this.linkColor = linkColor;
        return this;
    }

    public String getPrimaryButtonColor() {
        return primaryButtonColor;
    }

    public StyleSettings setPrimaryButtonColor(String primaryButtonColor) {
        this.primaryButtonColor = primaryButtonColor;
        return this;
    }

    public String getHeaderFont() {
        return headerFont;
    }

    public StyleSettings setHeaderFont(String headerFont) {
        this.headerFont = headerFont;
        return this;
    }

    public String getBodyFont() {
        return bodyFont;
    }

    public StyleSettings setBodyFont(String bodyFont) {
        this.bodyFont = bodyFont;
        return this;
    }

    public String getCustomCss() {
        return customCss;
    }

    public StyleSettings setCustomCss(String customCss) {
        this.customCss = customCss;
        return this;
    }

    public String getFooter() {
        return footer;
    }

    public StyleSettings setFooter(String footer) {
        this.footer = footer;
        return this;
    }
}
