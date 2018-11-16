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

package io.stallion.users;

import io.stallion.dataAccess.ModelBase;

import javax.persistence.Column;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmptyUser extends ModelBase implements IUser {
    private String username = "";
    private String displayName = "";
    private String givenName = "";
    private String familyName = "";
    private String email = "";
    private Role role = Role.ANON;
    private Long orgId = 0L;
    private List<IGroup> groups = new ArrayList<IGroup>();
    private String secret = "";
    private String timeZoneId = "";
    private boolean initialized = false;
    private boolean fromGdprCountry = false;
    private ZonedDateTime rightToBeForgottenInvokedAt = null;
    private ZonedDateTime acceptedTermsAt = null;
    private String acceptedTermsVersion = "";


    public String getUsername() {
        return username;
    }

    public EmptyUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getHonorific() {
        return "";
    }

    @Override
    public boolean isOptedOut() {
        return true;
    }

    @Override
    public boolean isTotallyOptedOut() {
        return true;
    }

    @Override
    public boolean isDisabled() {
        return true;
    }

    public EmptyUser setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getBcryptedPassword() {
        return null;
    }

    @Override
    public EmptyUser setBcryptedPassword(String password) {
        return null;
    }

    @Override
    public String getResetToken() {
        return null;
    }

    @Override
    public EmptyUser setResetToken(String token) {
        return null;
    }

    @Override
    public Long getAliasForId() {
        return 0L;
    }

    @Override
    public Long getCreatedAt() {
        return 0L;
    }

    @Override
    public <U extends IUser> U setCreatedAt(Long createdAt) {
        return null;
    }

    @Override
    public EmptyUser setAliasForId(Long id) {
        return this;
    }

    public EmptyUser setEmail(String email) {
        this.email = email;
        return this;
    }


    public Role getRole() {
        return role;
    }



    public EmptyUser setRole(Role role) {
        this.role = role;
        return this;
    }

    public Long getOrgId() {
        return orgId;
    }

    public EmptyUser setOrgId(Long orgId) {
        this.orgId = orgId;
        return this;
    }

    public List<IGroup> getGroups() {
        return groups;
    }

    @Override
    public <U extends IUser> U setGroups(List<IGroup> groups) {
        this.groups = groups;
        return (U)this;
    }

    public String getSecret() {
        return secret;
    }

    public EmptyUser setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    @Override
    public String getTimeZoneId() {
        return timeZoneId;
    }

    @Override
    public EmptyUser setTimeZoneId(String zoneId) {
        this.timeZoneId = zoneId;
        return this;
    }

    @Override
    public boolean isPredefined() {
        return false;
    }

    @Override
    public EmptyUser setPredefined(boolean predefined) {
        return null;
    }

    @Override
    public String getEncryptionSecret() {
        return "";
    }

    @Override
    public EmptyUser setEncryptionSecret(String secret) {
        return null;
    }

    @Override
    public Boolean getApproved() {
        return false;
    }

    @Override
    public boolean getEmailVerified() {
        return false;
    }

    @Override
    public <U extends IUser> U setDisabled(boolean disabled) {
        return (U)this;
    }

    @Override
    public EmptyUser setApproved(Boolean approved) {
        return null;
    }

    @Override
    public EmptyUser setEmailVerified(boolean verified) {
        return null;
    }

    public String getGivenName() {
        return givenName;
    }

    public EmptyUser setGivenName(String givenName) {
        this.givenName = givenName;return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public EmptyUser setFamilyName(String familyName) {
        this.familyName = familyName;return this;
    }

    @Override
    public String generateFilePath() {
        return null;
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public void setFilePath(String filePath) {

    }

    @Override
    @Column(nullable = false)
    public boolean isInitialized() {
        return initialized;
    }

    public EmptyUser setInitialized(boolean initialized) {
        this.initialized = initialized;
        return this;
    }

    @Override
    @Column(nullable = false)
    public boolean isFromGdprCountry() {
        return fromGdprCountry;
    }

    public EmptyUser setFromGdprCountry(boolean fromGdprCountry) {
        this.fromGdprCountry = fromGdprCountry;
        return this;
    }

    @Override
    @Column(nullable = false)
    public ZonedDateTime getRightToBeForgottenInvokedAt() {
        return rightToBeForgottenInvokedAt;
    }

    public EmptyUser setRightToBeForgottenInvokedAt(ZonedDateTime rightToBeForgottenInvokedAt) {
        this.rightToBeForgottenInvokedAt = rightToBeForgottenInvokedAt;
        return this;
    }

    @Override
    @Column(nullable = false)
    public ZonedDateTime getAcceptedTermsAt() {
        return acceptedTermsAt;
    }

    public EmptyUser setAcceptedTermsAt(ZonedDateTime acceptedTermsAt) {
        this.acceptedTermsAt = acceptedTermsAt;
        return this;
    }

    @Override
    @Column(nullable = false)
    public String getAcceptedTermsVersion() {
        return acceptedTermsVersion;
    }

    public EmptyUser setAcceptedTermsVersion(String acceptedTermsVersion) {
        this.acceptedTermsVersion = acceptedTermsVersion;
        return this;
    }
}
