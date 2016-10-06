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

    /**
     * The SMTP host
     * @return
     */
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * The username with which to connect to the SMTP server
     * @return
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * The password with which to connect to the SMTP server
     * @return
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Whether to use TLS to connect, defaults to true
     * @return
     */
    public Boolean getTls() {
        return tls;
    }

    public void setTls(Boolean tls) {
        this.tls = tls;
    }

    /**
     * The SMTP port to try to connect to, defaults to 587
     * @return
     */
    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    /**
     * The default email address for the "From" field.
     *
     * @return
     */
    public String getDefaultFromAddress() {
        return defaultFromAddress;
    }

    public void setDefaultFromAddress(String defaultFromAddress) {
        this.defaultFromAddress = defaultFromAddress;
    }

    /**
     * The name and postal address of the sender, for compliance with the CAN-SPAM law
     * @return
     */
    public String getCanSpamText() {
        return or(canSpamText, "");
    }

    public void setCanSpamText(String canSpamText) {
        this.canSpamText = canSpamText;
    }

    /**
     * A list of email addresses for admins, these addresses will get exception emails
     * and other other system emails.
     *
     * @return
     */
    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public EmailSettings setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
        return this;
    }

    /**
     * If true, will restrict outbound email address sending to a whitelist of addresses,
     * all other emails will be converted into a white-listed form using the "+" section.
     * So if this is true, and the outboundEmailTestAddress email is admin@stallion.io, and you are trying to email
     * barack@whitehouse.gov, then the to address will be converted to admin+barack-whitehouse-gov@stallion.io
     *
     * This is setting is true in local and QA mode. The purpose of this setting is to prevent accidentally
     * sending real emails to people from the development mode.
     *
     * @return
     */
    public Boolean getRestrictOutboundEmails() {
        return restrictOutboundEmails;
    }

    public EmailSettings setRestrictOutboundEmails(Boolean restrictOutboundEmails) {
        this.restrictOutboundEmails = restrictOutboundEmails;
        return this;
    }

    /**
     * A list of regualar expressions, email addresses matching the patterns will be permitted outbound email
     * addresses even if restrictOutboundEmails is true.
     *
     * @return
     */
    public List<String> getAllowedTestingOutboundEmailPatterns() {
        return allowedTestingOutboundEmailPatterns;
    }

    public EmailSettings setAllowedTestingOutboundEmailPatterns(List<String> allowedTestingOutboundEmailPatterns) {
        this.allowedTestingOutboundEmailPatterns = allowedTestingOutboundEmailPatterns;
        return this;
    }

    /**
     * A list of whitelisted outbound email addresses
     */
    public List<String> getAllowedOutboundEmails() {
        return allowedOutboundEmails;
    }

    public EmailSettings setAllowedOutboundEmails(List<String> allowedOutboundEmails) {
        this.allowedOutboundEmails = allowedOutboundEmails;
        return this;
    }

    /**
     * Compiled regular expressions for getAllowedTestingOutboundEmailPatterns()
     * @return
     */
    public List<Pattern> getAllowedTestingOutboundEmailCompiledPatterns() {
        return allowedTestingOutboundEmailCompiledPatterns;
    }

    public EmailSettings setAllowedTestingOutboundEmailCompiledPatterns(List<Pattern> allowedTestingOutboundEmailCompiledPatterns) {
        this.allowedTestingOutboundEmailCompiledPatterns = allowedTestingOutboundEmailCompiledPatterns;
        return this;
    }

    /**
     * The email address that all outbound emails are routed to in local and QA mode.
     *
     * @return
     */
    public String getOutboundEmailTestAddress() {
        return outboundEmailTestAddress;
    }

    public EmailSettings setOutboundEmailTestAddress(String outboundEmailTestAddress) {
        this.outboundEmailTestAddress = outboundEmailTestAddress;
        return this;
    }
}
