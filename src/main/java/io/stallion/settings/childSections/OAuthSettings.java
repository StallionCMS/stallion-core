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
import io.stallion.users.OAuthClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OAuthSettings implements SettingsSection {

    @SettingMeta(valBoolean = false, help = "Is OAuth enabled for this application.")
    private Boolean enabled;
    @SettingMeta(valBoolean = false)
    private Boolean requireHmac;
    @SettingMeta(valBoolean = false)
    private Boolean enableRefreshTokens;
    @SettingMeta(valBoolean = false, help = "If true, any valid user can use the built-in API's to create an OAuth client application.")
    private Boolean allowClientRegistration;
    @SettingMeta(help = "A list of pre-defined OAuth clients.", cls=ArrayList.class)
    private List<OAuthClient> clients;
    @SettingMeta(valBoolean = true, help = "If true, check if an access token is valid on every single request. If false, an access token will be valid until it expires, or until the user's secret session key is reset (which will expire every single token and access token).")
    private Boolean alwaysCheckAccessTokenValid;
    @SettingMeta(valLong = 43200, help = "How long access tokens live for. If zero they live forever.")
    private Long accessTokenValidSeconds;
    @SettingMeta(valLong = 315360000000L, help = "How long refresh tokens live for. Default is forever.")
    private Long refreshTokenValidSeconds;
    @SettingMeta(help="A map of scope names to a short publicly readable description.", cls=HashMap.class)
    private Map<String, String> scopeDescriptions;


    public Boolean getEnabled() {
        return enabled;
    }

    public OAuthSettings setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Boolean getRequireHmac() {
        return requireHmac;
    }

    public OAuthSettings setRequireHmac(Boolean requireHmac) {
        this.requireHmac = requireHmac;
        return this;
    }

    public Boolean getAllowClientRegistration() {
        return allowClientRegistration;
    }

    public OAuthSettings setAllowClientRegistration(Boolean allowClientRegistration) {
        this.allowClientRegistration = allowClientRegistration;
        return this;
    }

    public List<OAuthClient> getClients() {
        return clients;
    }

    public OAuthSettings setClients(List<OAuthClient> clients) {
        this.clients = clients;
        return this;
    }

    public Boolean getAlwaysCheckAccessTokenValid() {
        return alwaysCheckAccessTokenValid;
    }

    public OAuthSettings setAlwaysCheckAccessTokenValid(Boolean alwaysCheckAccessTokenValid) {
        this.alwaysCheckAccessTokenValid = alwaysCheckAccessTokenValid;
        return this;
    }

    public Long getAccessTokenValidSeconds() {
        return accessTokenValidSeconds;
    }

    public OAuthSettings setAccessTokenValidSeconds(Long accessTokenValidSeconds) {
        this.accessTokenValidSeconds = accessTokenValidSeconds;
        return this;
    }

    public Long getRefreshTokenValidSeconds() {
        return refreshTokenValidSeconds;
    }

    public OAuthSettings setRefreshTokenValidSeconds(Long refreshTokenValidSeconds) {
        this.refreshTokenValidSeconds = refreshTokenValidSeconds;
        return this;
    }

    public Map<String, String> getScopeDescriptions() {
        return scopeDescriptions;
    }

    public OAuthSettings setScopeDescriptions(Map<String, String> scopeDescriptions) {
        this.scopeDescriptions = scopeDescriptions;
        return this;
    }
}
