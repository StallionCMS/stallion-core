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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.file.ModelWithFilePath;
import io.stallion.email.Contactable;
import io.stallion.utils.json.RestrictedViews;

import javax.persistence.Column;
import java.time.ZonedDateTime;
import java.util.List;

public interface IUser extends Contactable, Model, ModelWithFilePath {


    public String getUsername();
    public String getDisplayName();
    public String getGivenName();
    public String getFamilyName();
    public Long getId();
    public String getEmail();
    public void setIsNewInsert(Boolean isNew);
    public Boolean getIsNewInsert();
    public String getBcryptedPassword();
    public <U extends IUser> U setBcryptedPassword(String password);
    public String getResetToken();
    public <U extends IUser> U setResetToken(String token);
    public Long getAliasForId();
    public <U extends IUser> U setAliasForId(Long id);
    public Long getCreatedAt();
    public <U extends IUser> U setCreatedAt(Long createdAt);


    @JsonView(RestrictedViews.Unrestricted.class)
    default public Boolean isAuthorized() {
        return !isAnon();
    }



    @JsonIgnore
    default public Boolean isAnon() {
        if (!getApproved()) {
            return true;
        }
        if (getRole() == null || Role.ANON.equals(getRole())) {
            return true;
        } else {
            return false;
        }
    }

    @JsonView(RestrictedViews.Owner.class)
    default public Boolean isStaff() {
        return isInRole(Role.STAFF);
    }

    @JsonIgnore
    default public Boolean isInRole(Role role) {
        if (Role.ANON.equals(role)) {
            return true;
        }
        if (!getApproved()) {
            return false;
        }
        if (getRole().getValue() >= role.getValue()) {
            return true;
        }
        return false;
    }

    public Long getOrgId();
    public List<IGroup> getGroups();
    public String getSecret();
    public <U extends IUser> U setUsername(String username);
    public <U extends IUser> U setDisplayName(String displayName);
    public <U extends IUser> U setEmail(String email);
    public Role getRole();
    public <U extends IUser> U setRole(Role role);
    public <U extends IUser> U setOrgId(Long orgId);
    public <U extends IUser> U setGroups(List<IGroup> groups);
    public <U extends IUser> U setSecret(String secret);
    public String getTimeZoneId();
    public <U extends IUser> U setTimeZoneId(String zoneId);
    public boolean isPredefined();
    public <U extends IUser> U setPredefined(boolean predefined);
    public String getEncryptionSecret();
    public <U extends IUser> U setEncryptionSecret(String secret);

    public <U extends IUser> U setGivenName(String givenName);
    public <U extends IUser> U setFamilyName(String familyName);

    public Boolean getApproved();
    public boolean getEmailVerified();
    public <U extends IUser> U setApproved(Boolean approved);
    public <U extends IUser> U setEmailVerified(boolean verified);
    public <U extends IUser> U setDisabled(boolean disabled);


    @Column(nullable = false)
    boolean isInitialized();

    @Column(nullable = false)
    boolean isFromGdprCountry();

    @Column(nullable = true)
    ZonedDateTime getRightToBeForgottenInvokedAt();

    @Column()
    ZonedDateTime getAcceptedTermsAt();

    @Column(nullable = false)
    String getAcceptedTermsVersion();
}
