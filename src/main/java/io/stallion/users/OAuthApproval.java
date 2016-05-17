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

package io.stallion.users;

import io.stallion.dataAccess.AlternativeKey;
import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;
import io.stallion.dataAccess.db.Converter;

import javax.persistence.Column;

import java.util.HashSet;
import java.util.Set;


public class OAuthApproval extends ModelBase {
    private long accessTokenExpiresAt = 0;
    private long refreshTokenExpiresAt = 0;
    private String refreshToken;
    private String accessToken;
    private long createdAt = 0;
    private String code;
    private boolean verified = false;
    private long userId = 0;
    private String clientId = "";
    private Set<String> scopes = new HashSet();
    private boolean scoped = false;
    private String internalSecret = "";
    private boolean revoked = false;


    @Column
    public long getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public OAuthApproval setAccessTokenExpiresAt(long accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        return this;
    }

    @Column
    public long getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public OAuthApproval setRefreshTokenExpiresAt(long refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        return this;
    }

    @Column
    @UniqueKey
    public String getRefreshToken() {
        return refreshToken;
    }

    public OAuthApproval setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    @Column
    @UniqueKey
    public String getAccessToken() {
        return accessToken;
    }

    public OAuthApproval setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    @Column
    public long getCreatedAt() {
        return createdAt;
    }

    public OAuthApproval setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Column
    @UniqueKey
    public String getCode() {
        return code;
    }

    public OAuthApproval setCode(String code) {
        this.code = code;
        return this;
    }

    @Column
    public boolean isVerified() {
        return verified;
    }

    public OAuthApproval setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    @Column
    @AlternativeKey
    public long getUserId() {
        return userId;
    }

    public OAuthApproval setUserId(long userId) {
        this.userId = userId;
        return this;
    }


    public String getClientId() {
        return clientId;
    }

    public OAuthApproval setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public String getBucket() {
        return "oauth_approvals";
    }

    @Column
    @Converter(name="io.stallion.dataAccess.db.converters.JsonSetConverter")
    public Set<String> getScopes() {
        return scopes;
    }

    public OAuthApproval setScopes(Set<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    public String getInternalSecret() {
        return internalSecret;
    }

    public OAuthApproval setInternalSecret(String internalSecret) {
        this.internalSecret = internalSecret;
        return this;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public OAuthApproval setRevoked(boolean revoked) {
        this.revoked = revoked;
        return this;
    }

    public boolean isScoped() {
        return scoped;
    }

    public OAuthApproval setScoped(boolean scoped) {
        this.scoped = scoped;
        return this;
    }
}
