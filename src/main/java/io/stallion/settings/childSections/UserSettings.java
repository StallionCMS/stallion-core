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

import io.stallion.exceptions.ConfigException;
import io.stallion.settings.SettingMeta;
import io.stallion.settings.Settings;
import io.stallion.users.Role;

import javax.persistence.Column;

import static io.stallion.utils.Literals.empty;

public class UserSettings implements SettingsSection {
    @SettingMeta(val = "/st-users/login")
    private String loginPage;
    @SettingMeta(val = "/st-users/reset-password")
    private String passwordResetPage;
    @SettingMeta(val = "/st-users/verify-email")
    private String verifyEmailPage;


    @SettingMeta(val = "stallion:email/verify-email-address.jinja")
    private String verifyEmailTemplate;

    @SettingMeta(val = "stallion:email/reset-password.jinja")
    private String resetEmailTemplate;

    @SettingMeta(val = "")
    private String verifyEmailSubject;

    @SettingMeta(val = "")
    private String resetEmailSubject;




    @SettingMeta(valBoolean = true)
    private Boolean syncAllUsersToMemory;

    @SettingMeta(valInt = 10000)
    private Integer limitSyncUsersToMemoryToCount;


    public String getFullLoginUrl() {
        String url = getLoginPage();
        if (url.contains("//")) {
            url = Settings.instance().getSiteUrl() + url;
        }
        return url;
    }


    @SettingMeta(valBoolean = true)
    private Boolean newAccountsAutoApprove;
    @SettingMeta(valBoolean = true)
    private Boolean passwordResetEnabled;
    @SettingMeta(valBoolean = true)
    private Boolean enableDefaultEndpoints;
    @SettingMeta(valBoolean = false)
    private Boolean newAccountsAllowCreation;
    @SettingMeta(valBoolean = true)
    private Boolean newAccountsRequireValidEmail;
    @SettingMeta(val="MEMBER")
    private String newAccountsRole;
    @SettingMeta(val="ANON")
    private String defaultEndpointRole;
    @SettingMeta(valBoolean = false)
    private Boolean allowValetMode;

    @SettingMeta(valBoolean = false)
    private Boolean disableStLoginParam;

    @SettingMeta(val = "")
    private String newAccountsDomainRestricted;

    public void postLoad() {
        if (Role.valueOf(newAccountsRole.toUpperCase())  == null) {
            throw new ConfigException("Invalid role for stallion.toml settings users.newAccountsRole: " + newAccountsRole);
        }
        if (Role.valueOf(defaultEndpointRole.toUpperCase())  == null) {
            throw new ConfigException("Invalid role for stallion.toml settings users.defaultEndpointRole: " + newAccountsRole);
        }
    }

    /**
     * Get the page where not-logged in users will be redirected to to login. Defaults to /st-users/login
     *
     * @return
     */
    public String getLoginPage() {
        if (empty(loginPage)) {
            return "/st-users/login";
        }
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    /**
     * If true, people can create accounts and it will be automatically approved without
     * admin intervention.
     *
     * @return
     */
    public Boolean getNewAccountsAutoApprove() {
        return newAccountsAutoApprove;
    }

    public void setNewAccountsAutoApprove(Boolean newAccountsAutoApprove) {
        this.newAccountsAutoApprove = newAccountsAutoApprove;
    }

    /**
     * True by default. Users can use the default password reset screens and endpoints in order
     * to reset their password over email. Set this to false if you want to implement your own
     * custom reset path, which may be needed for more secure sites.
     *
     * @return
     */
    public Boolean getPasswordResetEnabled() {
        return passwordResetEnabled;
    }

    public void setPasswordResetEnabled(Boolean passwordResetEnabled) {
        this.passwordResetEnabled = passwordResetEnabled;
    }

    /**
     * True by default. If false, none of the built-in user login, account creation, password reset,
     * and user management endpoints will be available. It will be up to the application developer
     * to implement them.
     *
     * @return
     */
    public Boolean getEnableDefaultEndpoints() {
        return enableDefaultEndpoints;
    }

    public UserSettings setEnableDefaultEndpoints(Boolean enableDefaultEndpoints) {
        this.enableDefaultEndpoints = enableDefaultEndpoints;
        return this;
    }

    /**
     * If true, enables the endpoint to allow public visitors to create
     * their own account.
     *
     * @return
     */
    public Boolean getNewAccountsAllowCreation() {
        return newAccountsAllowCreation;
    }

    public UserSettings setNewAccountsAllowCreation(Boolean newAccountsAllowCreation) {
        this.newAccountsAllowCreation = newAccountsAllowCreation;
        return this;
    }

    /**
     * If true, new accounts will need to verify their email address first.
     *
     * @return
     */
    public Boolean getNewAccountsRequireValidEmail() {
        return newAccountsRequireValidEmail;
    }

    public UserSettings setNewAccountsRequireValidEmail(Boolean newAccountsRequireValidEmail) {
        this.newAccountsRequireValidEmail = newAccountsRequireValidEmail;
        return this;
    }

    /**
     * The Role that new accounts will be assigned to. Defaults to "MEMBER".
     * @return
     */
    public String getNewAccountsRole() {
        return newAccountsRole;
    }

    public UserSettings setNewAccountsRole(String newAccountsRole) {
        this.newAccountsRole = newAccountsRole;
        return this;
    }

    /**
     * This should be a domain name. If it is non-empty, users will only be able to create new accounts
     * if they have an email address from this domain.
     *
     * @return
     */
    public String getNewAccountsDomainRestricted() {
        return newAccountsDomainRestricted;
    }

    public UserSettings setNewAccountsDomainRestricted(String newAccountsDomainRestricted) {
        this.newAccountsDomainRestricted = newAccountsDomainRestricted;
        return this;
    }

    /**
     * The path to the password reset page.
     *
     * @return
     */
    public String getPasswordResetPage() {
        return passwordResetPage;
    }

    public UserSettings setPasswordResetPage(String passwordResetPage) {
        this.passwordResetPage = passwordResetPage;
        return this;
    }

    /**
     * The path to the email verify page
     *
     * @return
     */
    public String getVerifyEmailPage() {
        return verifyEmailPage;
    }

    public UserSettings setVerifyEmailPage(String verifyEmailPage) {
        this.verifyEmailPage = verifyEmailPage;
        return this;
    }

    /**
     * If true, all users in the data store will be synced into memory.
     *
     * @return
     */
    public Boolean getSyncAllUsersToMemory() {
        return syncAllUsersToMemory;
    }

    public UserSettings setSyncAllUsersToMemory(Boolean syncAllUsersToMemory) {
        this.syncAllUsersToMemory = syncAllUsersToMemory;
        return this;
    }

    /**
     * If set to X, only the most recently updated X users will be synced to memory on boot.
     *
     * @return
     */
    public Integer getLimitSyncUsersToMemoryToCount() {
        return limitSyncUsersToMemoryToCount;
    }

    public UserSettings setLimitSyncUsersToMemoryToCount(Integer limitSyncUsersToMemoryToCount) {
        this.limitSyncUsersToMemoryToCount = limitSyncUsersToMemoryToCount;
        return this;
    }

    /**
     * If true, admin's can switch to non-admin accounts in valet mode, which allows them to act as that user.
     *
     * @return
     */
    public Boolean getAllowValetMode() {
        return allowValetMode;
    }

    public UserSettings setAllowValetMode(Boolean allowValetMode) {
        this.allowValetMode = allowValetMode;
        return this;
    }

    /**
     * If set, all endpoints by default will only be accessible to users of the given role or higher
     *
     * @return
     */
    public String getDefaultEndpointRole() {
        return defaultEndpointRole;
    }
    public Role getDefaultEndpointRoleObj() {
        return Role.valueOf(defaultEndpointRole);
    }

    public UserSettings setDefaultEndpointRole(String defaultEndpointRole) {
        this.defaultEndpointRole = defaultEndpointRole;
        return this;
    }

    /**
     * If true, putting stLogin=true in the URL will not redirect unlogged in users to the login page.
     *
     * @return
     */
    public Boolean getDisableStLoginParam() {
        return disableStLoginParam;
    }

    public UserSettings setDisableStLoginParam(Boolean disableStLoginParam) {
        this.disableStLoginParam = disableStLoginParam;
        return this;
    }


    public String getVerifyEmailTemplate() {
        return verifyEmailTemplate;
    }

    public UserSettings setVerifyEmailTemplate(String verifyEmailTemplate) {
        this.verifyEmailTemplate = verifyEmailTemplate;
        return this;
    }

    public String getResetEmailTemplate() {
        return resetEmailTemplate;
    }

    public UserSettings setResetEmailTemplate(String resetEmailTemplate) {
        this.resetEmailTemplate = resetEmailTemplate;
        return this;
    }

    public String getVerifyEmailSubject() {
        return verifyEmailSubject;
    }

    public UserSettings setVerifyEmailSubject(String verifyEmailSubject) {
        this.verifyEmailSubject = verifyEmailSubject;
        return this;
    }

    public String getResetEmailSubject() {
        return resetEmailSubject;
    }

    public UserSettings setResetEmailSubject(String resetEmailSubject) {
        this.resetEmailSubject = resetEmailSubject;
        return this;
    }
}
