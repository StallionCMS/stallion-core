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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import io.stallion.dataAccess.ModelBase;
import io.stallion.utils.json.JSON;

import javax.persistence.Column;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.HashMap;

import static io.stallion.utils.Literals.empty;


@Table(name="stallion_temp_tokens")
public class TempToken extends ModelBase {
    private String customKey = "";
    private ZonedDateTime expiresAt;
    private String token = "";
    private String userType = "";
    private String targetKey = "";
    private HashMap<String, Object> data = new HashMap<>();
    private ZonedDateTime usedAt;
    private ZonedDateTime createdAt;

    @Column
    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey;
    }

    @Column
    public ZonedDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(ZonedDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Column
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @JsonIgnore
    public String getData() throws Exception {
        return JSON.stringify(data);
    }

    @JsonIgnore
    public void setData(String data) throws Exception {
        if (!empty(data)) {
            this.data = (HashMap<String, Object>) JSON.parse(data, new TypeReference<HashMap<String, Object>>() {
            });
        } else {
            this.data = new HashMap<>();
        }
    }


    @JsonProperty
    public HashMap<String, Object> getDataObj() {
        return data;
    }

    @JsonProperty
    public void setDataObj(HashMap<String, Object> data) {
        this.data = data;
    }

    @Column
    public ZonedDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(ZonedDateTime usedAt) {
        this.usedAt = usedAt;
    }

    @Column
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Column
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Column
    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }
}
