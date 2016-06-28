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


public class SessionData {
    private String secret;
    private Object userId;
    private Long expires;

    public String getSecret() {
        return secret;
    }

    public SessionData setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public Object getUserId() {
        return userId;
    }

    public SessionData setUserId(Object userId) {
        this.userId = userId;
        return this;
    }

    public Long getExpires() {
        return expires;
    }

    public SessionData setExpires(Long expires) {
        this.expires = expires;
        return this;
    }
}
