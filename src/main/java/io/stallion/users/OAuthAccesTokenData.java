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

import java.util.HashSet;
import java.util.Set;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class OAuthAccesTokenData {
    private Long userId;
    private Long expires;
    private Long approvalId;
    private Set<String> scopes = new HashSet<>();
    private Long clientId = 0L;
    private boolean scoped;

    public Long getUserId() {
        return userId;
    }

    public OAuthAccesTokenData setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getExpires() {
        return expires;
    }

    public OAuthAccesTokenData setExpires(Long expires) {
        this.expires = expires;
        return this;
    }

    public Long getApprovalId() {
        return approvalId;
    }

    public OAuthAccesTokenData setApprovalId(Long approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public OAuthAccesTokenData setScopes(Set<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    public Long getClientId() {
        return clientId;
    }

    public OAuthAccesTokenData setClientId(Long clientId) {
        this.clientId = clientId;
        return this;
    }

    public boolean isScoped() {
        return scoped;
    }

    public OAuthAccesTokenData setScoped(boolean scoped) {
        this.scoped = scoped;
        return this;
    }
}
