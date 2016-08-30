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

package io.stallion.boot;

import javax.persistence.Column;

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

    private String databaseUrl = "";
    private String databasePassword = "";
    private String databaseUsername = "";

    private String emailUsername;
    private String emailPassword;
    private String adminEmail;
    private String emailHost;
    private Long emailPort;

    private String highlightColor;


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

    public String getEmailUsername() {
        return emailUsername;
    }

    public ProjectSettingsBuilder setEmailUsername(String emailUsername) {
        this.emailUsername = emailUsername;
        return this;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public ProjectSettingsBuilder setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
        return this;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public ProjectSettingsBuilder setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
        return this;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public ProjectSettingsBuilder setEmailHost(String emailHost) {
        this.emailHost = emailHost;
        return this;
    }

    public Long getEmailPort() {
        return emailPort;
    }

    public ProjectSettingsBuilder setEmailPort(Long emailPort) {
        this.emailPort = emailPort;
        return this;
    }

    public String getHighlightColor() {
        return highlightColor;
    }

    public ProjectSettingsBuilder setHighlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
        return this;
    }


    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public ProjectSettingsBuilder setDatabaseUrl(String databaseUrl) {
        if (!databaseUrl.contains("?")) {
            databaseUrl = databaseUrl + "?allowMultiQueries=true&autoReConnect=true";
        }
        this.databaseUrl = databaseUrl;
        return this;
    }


    public String getDatabasePassword() {
        return databasePassword;
    }

    public ProjectSettingsBuilder setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
        return this;
    }


    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public ProjectSettingsBuilder setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
        return this;
    }
}
