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

import io.stallion.dal.base.ModelBase;
import io.stallion.dal.base.UniqueKey;
import io.stallion.dal.db.Converter;

import javax.persistence.Column;

import java.util.*;

import static io.stallion.utils.Literals.*;


public class OAuthClient extends ModelBase {
    private String name = "";
    private String publicName = "";
    private String publicAuthor = "";
    private String publicDescription = "";
    private String logoUrl = "";
    private String fullClientId = "";
    private String clientSecret = "";
    private boolean requiresSecret = true;
    private boolean allowProvidedCode = false;
    private Set<String> allowedRedirectUris = set();
    private Long creatorId = 0L;
    private Set<Long> ownerIds = set();
    private Set<String> scopes = set();
    private boolean scoped = false;
    private Long accessTokenValiditySeconds = 0L;
    private Long refreshTokenValiditySeconds = 0L;
    private Set<String> authorizedGrantTypes = new HashSet<>();
    private String defaultRedirectUri = "";
    private Boolean autoApprove = false;
    private Map additionalInformation = new HashMap<>();
    private boolean disabled = false;
    private boolean pendingApproval = false;

    @Column
    public String getName() {
        return name;
    }

    public OAuthClient setName(String name) {
        this.name = name;
        return this;
    }

    public String getPublicAuthor() {
        return publicAuthor;
    }

    public OAuthClient setPublicAuthor(String publicAuthor) {
        this.publicAuthor = publicAuthor;
        return this;
    }

    @UniqueKey
    @Column
    public String getFullClientId() {
        return fullClientId;
    }

    public OAuthClient setFullClientId(String fullClientId) {
        this.fullClientId = fullClientId;
        return this;
    }

    @Column
    public String getClientSecret() {
        return clientSecret;
    }

    public OAuthClient setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    @Column
    @Converter(name="io.stallion.dal.db.converters.JsonSetConverter")
    public Set<String> getAllowedRedirectUris() {
        return allowedRedirectUris;
    }

    public OAuthClient setAllowedRedirectUris(Set<String> allowedRedirectUris) {
        this.allowedRedirectUris = allowedRedirectUris;
        return this;
    }

    @Column
    public Long getCreatorId() {
        return creatorId;
    }

    public OAuthClient setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    @Column
    @Converter(name="io.stallion.dal.db.converters.JsonSetConverter")
    public Set<Long> getOwnerIds() {
        return ownerIds;
    }

    public OAuthClient setOwnerIds(Set<Long> ownerIds) {
        this.ownerIds = ownerIds;
        return this;
    }

    @Column
    public Set<String> getScopes() {
        return scopes;
    }

    public OAuthClient setScopes(Set<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    @Column
    public boolean isScoped() {
        return scoped;
    }

    public OAuthClient setScoped(boolean scoped) {
        this.scoped = scoped;
        return this;
    }

    @Column
    public Long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    public OAuthClient setAccessTokenValiditySeconds(Long accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        return this;
    }

    @Column
    public Long getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    public OAuthClient setRefreshTokenValiditySeconds(Long refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        return this;
    }

    @Column
    public Set<String> getAuthorizedGrantTypes() {
        return authorizedGrantTypes;
    }

    public OAuthClient setAuthorizedGrantTypes(Set<String> authorizedGrantTypes) {
        this.authorizedGrantTypes = authorizedGrantTypes;
        return this;
    }

    @Column
    public String getDefaultRedirectUri() {
        return defaultRedirectUri;
    }

    public OAuthClient setDefaultRedirectUri(String defaultRedirectUri) {
        this.defaultRedirectUri = defaultRedirectUri;
        return this;
    }

    @Column
    public Boolean getAutoApprove() {
        return autoApprove;
    }

    public OAuthClient setAutoApprove(Boolean autoApprove) {
        this.autoApprove = autoApprove;
        return this;
    }

    @Column
    @Converter(name="io.stallion.dal.db.converters.JsonMapConverter")
    public Map getAdditionalInformation() {
        return additionalInformation;
    }

    public OAuthClient setAdditionalInformation(Map additionalInformation) {
        this.additionalInformation = additionalInformation;
        return this;
    }

    @Column
    public String getPublicName() {
        return publicName;
    }

    public OAuthClient setPublicName(String publicName) {
        this.publicName = publicName;
        return this;
    }

    @Column
    public String getPublicDescription() {
        return publicDescription;
    }

    public OAuthClient setPublicDescription(String publicDescription) {
        this.publicDescription = publicDescription;
        return this;
    }

    @Column
    public String getLogoUrl() {
        return logoUrl;
    }

    public OAuthClient setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
        return this;
    }

    @Column
    public boolean isDisabled() {
        return disabled;
    }

    public OAuthClient setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public boolean hasGrantType(GrantType gt) {
        String checkGrant = gt.toString().toLowerCase();
        for (String grant: getAuthorizedGrantTypes()) {
            if (grant.toUpperCase().equals(checkGrant)) {
                return true;
            }
        }
        return false;
    }

    @Column
    public boolean isPendingApproval() {
        return pendingApproval;
    }

    public OAuthClient setPendingApproval(boolean pendingApproval) {
        this.pendingApproval = pendingApproval;
        return this;
    }

    @Column
    public boolean isRequiresSecret() {
        return requiresSecret;
    }

    public OAuthClient setRequiresSecret(boolean requiresSecret) {
        this.requiresSecret = requiresSecret;
        return this;
    }

    @Column
    public boolean isAllowProvidedCode() {
        return allowProvidedCode;
    }

    public OAuthClient setAllowProvidedCode(boolean allProvidedCode) {
        this.allowProvidedCode = allProvidedCode;
        return this;
    }
}
