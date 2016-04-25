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

import java.util.Set;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class OAuthUserLogin {
    private IUser user;
    private Set<String> scopes;
    private boolean scoped;
    private long approvalId;

    public IUser getUser() {
        return user;
    }

    public OAuthUserLogin setUser(IUser user) {
        this.user = user;
        return this;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public OAuthUserLogin setScopes(Set<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    public boolean isScoped() {
        return scoped;
    }

    public OAuthUserLogin setScoped(boolean scoped) {
        this.scoped = scoped;
        return this;
    }

    public long getApprovalId() {
        return approvalId;
    }

    public OAuthUserLogin setApprovalId(long approvalId) {
        this.approvalId = approvalId;
        return this;
    }
}
