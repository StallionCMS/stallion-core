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

import static io.stallion.utils.Literals.*;

public class UserSettings implements SettingsSection {
    @SettingMeta(val = "/st-users/login")
    private String loginPage;
    @SettingMeta(val = "/st-users/reset-password")
    private String passwordResetPage;
    @SettingMeta(val = "/st-users/verify-email")
    private String verifyEmailPage;

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

    public String getLoginPage() {
        if (empty(loginPage)) {
            return "/st-users/login";
        }
        return loginPage;
    }

    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    public Boolean getNewAccountsAutoApprove() {
        return newAccountsAutoApprove;
    }

    public void setNewAccountsAutoApprove(Boolean newAccountsAutoApprove) {
        this.newAccountsAutoApprove = newAccountsAutoApprove;
    }

    public Boolean getPasswordResetEnabled() {
        return passwordResetEnabled;
    }

    public void setPasswordResetEnabled(Boolean passwordResetEnabled) {
        this.passwordResetEnabled = passwordResetEnabled;
    }

    public Boolean getEnableDefaultEndpoints() {
        return enableDefaultEndpoints;
    }

    public UserSettings setEnableDefaultEndpoints(Boolean enableDefaultEndpoints) {
        this.enableDefaultEndpoints = enableDefaultEndpoints;
        return this;
    }

    public Boolean getNewAccountsAllowCreation() {
        return newAccountsAllowCreation;
    }

    public UserSettings setNewAccountsAllowCreation(Boolean newAccountsAllowCreation) {
        this.newAccountsAllowCreation = newAccountsAllowCreation;
        return this;
    }

    public Boolean getNewAccountsRequireValidEmail() {
        return newAccountsRequireValidEmail;
    }

    public UserSettings setNewAccountsRequireValidEmail(Boolean newAccountsRequireValidEmail) {
        this.newAccountsRequireValidEmail = newAccountsRequireValidEmail;
        return this;
    }

    public String getNewAccountsRole() {
        return newAccountsRole;
    }

    public UserSettings setNewAccountsRole(String newAccountsRole) {
        this.newAccountsRole = newAccountsRole;
        return this;
    }

    public String getNewAccountsDomainRestricted() {
        return newAccountsDomainRestricted;
    }

    public UserSettings setNewAccountsDomainRestricted(String newAccountsDomainRestricted) {
        this.newAccountsDomainRestricted = newAccountsDomainRestricted;
        return this;
    }

    public String getPasswordResetPage() {
        return passwordResetPage;
    }

    public UserSettings setPasswordResetPage(String passwordResetPage) {
        this.passwordResetPage = passwordResetPage;
        return this;
    }

    public String getVerifyEmailPage() {
        return verifyEmailPage;
    }

    public UserSettings setVerifyEmailPage(String verifyEmailPage) {
        this.verifyEmailPage = verifyEmailPage;
        return this;
    }

    public Boolean getSyncAllUsersToMemory() {
        return syncAllUsersToMemory;
    }

    public UserSettings setSyncAllUsersToMemory(Boolean syncAllUsersToMemory) {
        this.syncAllUsersToMemory = syncAllUsersToMemory;
        return this;
    }

    public Integer getLimitSyncUsersToMemoryToCount() {
        return limitSyncUsersToMemoryToCount;
    }

    public UserSettings setLimitSyncUsersToMemoryToCount(Integer limitSyncUsersToMemoryToCount) {
        this.limitSyncUsersToMemoryToCount = limitSyncUsersToMemoryToCount;
        return this;
    }


    public Boolean getAllowValetMode() {
        return allowValetMode;
    }

    public UserSettings setAllowValetMode(Boolean allowValetMode) {
        this.allowValetMode = allowValetMode;
        return this;
    }

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

    public Boolean getDisableStLoginParam() {
        return disableStLoginParam;
    }

    public UserSettings setDisableStLoginParam(Boolean disableStLoginParam) {
        this.disableStLoginParam = disableStLoginParam;
        return this;
    }
}
