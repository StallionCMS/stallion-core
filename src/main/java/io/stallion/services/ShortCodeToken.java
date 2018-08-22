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

package io.stallion.services;

import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.UniqueKey;

import javax.persistence.Column;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Table(name="stallion_short_code_tokens")
public class ShortCodeToken extends ModelBase {
    private String key = "";
    private ZonedDateTime expiresAt;
    private String code = "";
    private ZonedDateTime usedAt;
    private ZonedDateTime createdAt;
    private String actionKey = "";

    @Column
    @UniqueKey
    public String getKey() {
        return key;
    }

    public ShortCodeToken setKey(String key) {
        this.key = key;
        return this;
    }

    @Column
    public ZonedDateTime getExpiresAt() {
        return expiresAt;
    }

    public ShortCodeToken setExpiresAt(ZonedDateTime expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    @Column
    public String getCode() {
        return code;
    }

    public ShortCodeToken setCode(String code) {
        this.code = code;
        return this;
    }

    @Column
    public ZonedDateTime getUsedAt() {
        return usedAt;
    }

    public ShortCodeToken setUsedAt(ZonedDateTime usedAt) {
        this.usedAt = usedAt;
        return this;
    }

    @Column
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ShortCodeToken setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Column
    public String getActionKey() {
        return actionKey;
    }

    public ShortCodeToken setActionKey(String actionKey) {
        this.actionKey = actionKey;
        return this;
    }
}
