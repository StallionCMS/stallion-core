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

import io.stallion.settings.SettingMeta;
import io.stallion.settings.Settings;

import static io.stallion.utils.Literals.*;

/**
 * Inserted into the global template context with the "styles" variable. This is used to customize the colors and
 * look and feel of default templates, such as the 404 page, the password reset template, etc.
 */
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


    /**
     * The full URL of the logo, with {cdnUrl} or {port} token converted to the actual CDN URL
     * @return
     */
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
        if (!url.contains("//")) {
            if (!url.startsWith("/")) {
                url = "/st-assets/" + url;
            }
        }
        if (!url.contains("://")) {
            url = Settings.instance().getSiteUrl() + url;
        }

        return url;
    }

    /**
     * The path or URL for the logo image. Use {cdnUrl} to put in a the CDN Url, or {port} to put in the port number
     *
     * @return
     */
    public String getLogoImage() {
        return logoImage;
    }

    public StyleSettings setLogoImage(String logoImage) {
        this.logoImage = logoImage;
        return this;
    }

    /**
     * The background color of the default pages, in hex form
     *
     * @return
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    public StyleSettings setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * The logo text color in hex form, used if the logo image is missing
     *
     * @return
     */
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


    /**
     * A secondary color used for separation lines, buttons, etc.
     *
     * @return
     */
    public String getHighlightColor() {
        return highlightColor;
    }

    public StyleSettings setHighlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
        return this;
    }

    /**
     * The color used for links
     *
     * @return
     */
    public String getLinkColor() {
        return linkColor;
    }

    public StyleSettings setLinkColor(String linkColor) {
        this.linkColor = linkColor;
        return this;
    }

    /**
     * The color for primary buttons.
     *
     * @return
     */
    public String getPrimaryButtonColor() {
        return primaryButtonColor;
    }

    public StyleSettings setPrimaryButtonColor(String primaryButtonColor) {
        this.primaryButtonColor = primaryButtonColor;
        return this;
    }

    /**
     * The font family to use for h1, h2, etc tags.
     * @return
     */
    public String getHeaderFont() {
        return headerFont;
    }

    public StyleSettings setHeaderFont(String headerFont) {
        this.headerFont = headerFont;
        return this;
    }

    /**
     * The font family to use for regular body text.
     * @return
     */
    public String getBodyFont() {
        return bodyFont;
    }

    public StyleSettings setBodyFont(String bodyFont) {
        this.bodyFont = bodyFont;
        return this;
    }

    /**
     * Custom CSS of arbitrary length.
     *
     * @return
     */
    public String getCustomCss() {
        return customCss;
    }

    public StyleSettings setCustomCss(String customCss) {
        this.customCss = customCss;
        return this;
    }

    /**
     * Text to include in the footer of the templates.
     *
     * @return
     */
    public String getFooter() {
        return footer;
    }

    public StyleSettings setFooter(String footer) {
        this.footer = footer;
        return this;
    }
}
