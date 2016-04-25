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

package io.stallion.boot;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


class ProjectSettingsBuilder {
    // Ask for site name
    // Ask for site description
    // Ask for default site title
    // Ask for production site URL?
    private String siteName;
    private String siteDescription;
    private String title;
    private String siteUrl;
    private String healthCheckSecret;

    public String getSiteName() {
        return siteName;
    }

    public ProjectSettingsBuilder setSiteName(String siteName) {
        this.siteName = siteName;
        return this;
    }

    public String getSiteDescription() {
        return siteDescription;
    }

    public ProjectSettingsBuilder setSiteDescription(String siteDescription) {
        this.siteDescription = siteDescription;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ProjectSettingsBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public ProjectSettingsBuilder setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
        return this;
    }

    public String getHealthCheckSecret() {
        return healthCheckSecret;
    }

    public ProjectSettingsBuilder setHealthCheckSecret(String healthCheckSecret) {
        this.healthCheckSecret = healthCheckSecret;
        return this;
    }
}
