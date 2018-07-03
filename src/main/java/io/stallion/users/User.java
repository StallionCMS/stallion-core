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

import com.fasterxml.jackson.annotation.JsonView;
import io.stallion.dataAccess.AlternativeKey;
import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;
import io.stallion.dataAccess.db.DefaultSort;
import io.stallion.dataAccess.db.ExtraKeyDefinitions;
import io.stallion.dataAccess.file.ModelWithFilePath;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.RestrictedViews;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static io.stallion.utils.Literals.empty;


@Table(name="stallion_users")
@DefaultSort(field="email", direction = "ASC")
public class User extends ModelBase implements IUser, ModelWithFilePath {
    private String username = "";
    private String displayName = "";
    private String givenName = "";
    private String familyName = "";
    private String email = "";
    private Role role = Role.ANON;
    private Long orgId = 0L;
    private List<IGroup> groups = new ArrayList<IGroup>();
    private String secret = "";
    private String encryptionSecret = "";
    private String timeZoneId = "";
    private String bcryptedPassword;
    private String filePath = "";
    private boolean predefined = false;
    private Boolean approved = false;
    private boolean emailVerified = false;
    private String resetToken = "";
    private Long aliasForId = 0L;
    private boolean disabled = false;
    private boolean optedOut = false;
    private boolean totallyOptedOut = false;
    private String honorific = "";
    private Long createdAt = 0L;


    @Override
    @Column
    @UniqueKey(caseInsensitive = true)
    @JsonView(RestrictedViews.Member.class)
    public String getUsername() {
        return username;
    }

    @Override
    public <U extends IUser> U setUsername(String username) {
        this.username = username;
        return (U)this;
    }

    @Override
    @Column
    @JsonView(RestrictedViews.Public.class)
    public String getDisplayName() {
        return displayName;
    }

    /*
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    */

    @Override
    public <U extends IUser> U setDisplayName(String displayName) {
        this.displayName = displayName;
        return (U)this;
    }


    @Column
    @JsonView(RestrictedViews.Member.class)
    public String getGivenName() {
        return givenName;
    }

    public <U extends IUser> U setGivenName(String givenName) {
        this.givenName = givenName;
        return (U)this;
    }

    @Column
    @JsonView(RestrictedViews.Member.class)
    public String getFamilyName() {
        return familyName;
    }

    public <U extends IUser> U setFamilyName(String familyName) {
        this.familyName = familyName;
        return (U)this;
    }

    @Override
    @Column
    @UniqueKey(caseInsensitive = true)
    @JsonView(RestrictedViews.Member.class)
    public String getEmail() {
        return email;
    }

    @Override
    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    @Column
    @JsonView(RestrictedViews.Owner.class)
    public Role getRole() {
        return role;
    }

    @Override
    public User setRole(Role role) {
        this.role = role;
        return this;
    }


    public User setRoleFromString(String role) {
        this.role = Enum.valueOf(Role.class, role);
        return this;
    }

    @Override
    @Column
    @AlternativeKey
    @JsonView(RestrictedViews.Member.class)
    public Long getOrgId() {
        return orgId;
    }

    @Override
    public User setOrgId(Long orgId) {
        this.orgId = orgId;
        return this;
    }

    @Override
    public List<IGroup> getGroups() {
        return groups;
    }

    @Override
    public <U extends IUser> U setGroups(List<IGroup> groups) {
        this.groups = groups;
        return (U)this;
    }

    /**
     * Generated at user creation, used inside cookies to verify the user's authenticity.
     * Can be reset to force all extant sessions to be invalid.
     * @return
     */
    @Override
    @Column
    @JsonView(RestrictedViews.Internal.class)
    public String getSecret() {
        return secret;
    }

    @Override
    public <U extends IUser> U  setSecret(String secret) {
        this.secret = secret;
        return (U)this;
    }

    @Override
    @JsonView(RestrictedViews.Member.class)
    @Column
    public String getTimeZoneId() {
        return timeZoneId;
    }

    @Override
    public <U extends IUser> U setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
        return (U)this;
    }

    @Override
    @JsonView(RestrictedViews.Internal.class)
    @Column
    public String getBcryptedPassword() {
        return bcryptedPassword;
    }

    @Override
    public <U extends IUser> U setBcryptedPassword(String bcryptedPassword) {
        this.bcryptedPassword = bcryptedPassword;
        return (U)this;
    }

    @Override
    public String generateFilePath() {
        String name = getFamilyName() + "-" + getGivenName();
        if (empty(getFamilyName()) && empty(getGivenName())) {
            name = email;
        }
        return GeneralUtils.slugify(name + "---" + getId().toString()) + ".json";
    }

    @Override
    @JsonView(RestrictedViews.Internal.class)
    public String getFilePath() {
        return filePath;
    }

    @Override
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Used to encrypt the user's cookie. Set this to a new value to make all
     * extant sessions invalid.
     *
     * @return
     */
    @JsonView(RestrictedViews.Internal.class)
    @Column
    public String getEncryptionSecret() {
        return encryptionSecret;
    }

    public <U extends IUser> U setEncryptionSecret(String encryptionSecret) {
        this.encryptionSecret = encryptionSecret;
        return (U)this;
    }



    @Override
    public boolean isPredefined() {
        return predefined;
    }

    @Override
    public User setPredefined(boolean predefined) {
        this.predefined = predefined;
        return this;
    }

    /**
     * Has the user been approved or auto-approved by a staff? If false, user will not be treated
     * as authorized.
     * @return
     */
    @Column
    @JsonView(RestrictedViews.Member.class)
    @Override
    public Boolean getApproved() {
        return approved;
    }

    public <U extends IUser> U setApproved(Boolean approved) {
        this.approved = approved;
        return (U)this;
    }

    /**
     * Has the email address of the user been verified?
     * @return
     */
    @Column
    @JsonView(RestrictedViews.Member.class)
    public boolean getEmailVerified() {
        return emailVerified;
    }


    public <U extends IUser> U setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        return (U)this;
    }

    /**
     * Token emailed to the user when resetting the password. Gets a new value every time
     * a password reset is performed.
     * @return
     */
    @Override
    @Column
    @JsonView(RestrictedViews.Internal.class)
    public String getResetToken() {
        return resetToken;
    }

    public <U extends IUser> U setResetToken(String resetToken) {
        this.resetToken = resetToken;
        return (U)this;
    }

    /**
     * The User ID of the user that this user object is an alias for. Used so that multiple email addresses can
     * point to the same user. There will be one master user object, and the other user objects will be empty
     * except for this aliasForId which will point to the master user object. When loading by username or email,
     * if this aliasForId is set, will look up the user of that ID and return it.
     * @return
     */
    @Column
    @AlternativeKey
    @JsonView(RestrictedViews.Internal.class)
    public Long getAliasForId() {
        return aliasForId;
    }


    @Override
    public <U extends IUser> U setAliasForId(Long aliasForId) {
        this.aliasForId = aliasForId;
        return (U)this;
    }

    /**
     * Mr, Mrs, Dr, Honorable, etc.
     * @return
     */
    @Column
    @Override
    @JsonView(RestrictedViews.Member.class)
    public String getHonorific() {
        return honorific;
    }

    public User setHonorific(String honorific) {
        this.honorific = honorific;
        return this;
    }

    /**
     * Will block sending all emails via subclasses of ContactableEmailer, even transactional emails
     * like password resets.
     * @return
     */
    @Column
    @JsonView(RestrictedViews.Owner.class)
    public boolean isTotallyOptedOut() {
        return totallyOptedOut;
    }

    public User setTotallyOptedOut(boolean totallyOptedOut) {
        this.totallyOptedOut = totallyOptedOut;
        return this;
    }

    /**
     * Will block all non-transactional emails sent via ContactableEmailer. Will still send emails such as password resets.
     * @return
     */
    @Column
    @Override
    @JsonView(RestrictedViews.Owner.class)
    public boolean isOptedOut() {
        return optedOut;
    }

    public User setOptedOut(boolean optedOut) {
        this.optedOut = optedOut;
        return this;
    }

    /**
     * If true, user won't be allowed to log in and access the site.
     * @return
     */
    @Column
    @JsonView(RestrictedViews.Owner.class)
    public boolean isDisabled() {
        return disabled;
    }

    public User setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @Override
    @Column
    @JsonView(RestrictedViews.Owner.class)
    public Long getCreatedAt() {
        return createdAt;
    }

    public User setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}


