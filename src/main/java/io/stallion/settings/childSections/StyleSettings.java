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
    @SettingMeta
    private String logoText;
    @SettingMeta(val="#333333")
    private String logoTextColor;
    @SettingMeta(val="#E9E9E9")
    private String logoBackgroundColor;

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

    public void setLogoImage(String logoImage) {
        this.logoImage = logoImage;
    }

    public String getLogoText() {
        return logoText;
    }

    public void setLogoText(String logoText) {
        this.logoText = logoText;
    }

    public String getLogoTextColor() {
        return logoTextColor;
    }

    public void setLogoTextColor(String logoTextColor) {
        this.logoTextColor = logoTextColor;
    }

    public String getLogoBackgroundColor() {
        return logoBackgroundColor;
    }

    public void setLogoBackgroundColor(String logoBackgroundColor) {
        this.logoBackgroundColor = logoBackgroundColor;
    }
}
