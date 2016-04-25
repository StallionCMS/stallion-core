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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.or;


public class EmailSettings implements SettingsSection {
    @SettingMeta
    private String host;
    @SettingMeta
    private String username;
    @SettingMeta
    private String password;
    @SettingMeta(valBoolean = true)
    private Boolean tls;
    @SettingMeta(valLong = 587L)
    private Long port;
    @SettingMeta
    private String defaultFromAddress;
    @SettingMeta
    private String canSpamText;
    @SettingMeta(cls=ArrayList.class)
    private List<String> adminEmails;
    @SettingMeta()
    private Boolean restrictOutboundEmails;
    @SettingMeta(cls=ArrayList.class)
    private List<String> allowedOutboundEmails;
    @SettingMeta(cls=ArrayList.class)
    private List<String> allowedTestingOutboundEmailPatterns;
    @SettingMeta(cls=ArrayList.class)
    private List<Pattern> allowedTestingOutboundEmailCompiledPatterns;
    @SettingMeta()
    private String outboundEmailTestAddress;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getTls() {
        return tls;
    }

    public void setTls(Boolean tls) {
        this.tls = tls;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public String getDefaultFromAddress() {
        return defaultFromAddress;
    }

    public void setDefaultFromAddress(String defaultFromAddress) {
        this.defaultFromAddress = defaultFromAddress;
    }

    public String getCanSpamText() {
        return or(canSpamText, "");
    }

    public void setCanSpamText(String canSpamText) {
        this.canSpamText = canSpamText;
    }

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public EmailSettings setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
        return this;
    }

    public Boolean getRestrictOutboundEmails() {
        return restrictOutboundEmails;
    }

    public EmailSettings setRestrictOutboundEmails(Boolean restrictOutboundEmails) {
        this.restrictOutboundEmails = restrictOutboundEmails;
        return this;
    }

    public List<String> getAllowedTestingOutboundEmailPatterns() {
        return allowedTestingOutboundEmailPatterns;
    }

    public EmailSettings setAllowedTestingOutboundEmailPatterns(List<String> allowedTestingOutboundEmailPatterns) {
        this.allowedTestingOutboundEmailPatterns = allowedTestingOutboundEmailPatterns;
        return this;
    }

    public List<String> getAllowedOutboundEmails() {
        return allowedOutboundEmails;
    }

    public EmailSettings setAllowedOutboundEmails(List<String> allowedOutboundEmails) {
        this.allowedOutboundEmails = allowedOutboundEmails;
        return this;
    }

    public List<Pattern> getAllowedTestingOutboundEmailCompiledPatterns() {
        return allowedTestingOutboundEmailCompiledPatterns;
    }

    public EmailSettings setAllowedTestingOutboundEmailCompiledPatterns(List<Pattern> allowedTestingOutboundEmailCompiledPatterns) {
        this.allowedTestingOutboundEmailCompiledPatterns = allowedTestingOutboundEmailCompiledPatterns;
        return this;
    }

    public String getOutboundEmailTestAddress() {
        return outboundEmailTestAddress;
    }

    public EmailSettings setOutboundEmailTestAddress(String outboundEmailTestAddress) {
        this.outboundEmailTestAddress = outboundEmailTestAddress;
        return this;
    }
}
