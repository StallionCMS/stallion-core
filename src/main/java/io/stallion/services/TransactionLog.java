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
import io.stallion.dataAccess.db.Converter;
import io.stallion.dataAccess.db.converters.JsonMapConverter;

import javax.persistence.Column;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.Map;

@Table(name="stallion_transaction_log")
public class TransactionLog extends ModelBase {
    private String subject;
    private String body;
    private Long userId;
    private Long orgId;
    private String type;
    private String customKey;
    private String toAddress;
    private Map<String, Object> extra;
    private ZonedDateTime createdAt;

    @Column
    public String getSubject() {
        return subject;
    }

    public TransactionLog setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    @Column(columnDefinition = "longtext")
    public String getBody() {
        return body;
    }

    public TransactionLog setBody(String body) {
        this.body = body;
        return this;
    }

    @Column
    public Long getUserId() {
        return userId;
    }

    public TransactionLog setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    @Column
    public Long getOrgId() {
        return orgId;
    }

    public TransactionLog setOrgId(Long orgId) {
        this.orgId = orgId;
        return this;
    }

    @Column(length = 65)
    public String getType() {
        return type;
    }

    public TransactionLog setType(String type) {
        this.type = type;
        return this;
    }

    @Column(length = 65)
    public String getCustomKey() {
        return customKey;
    }

    public TransactionLog setCustomKey(String customKey) {
        this.customKey = customKey;
        return this;
    }

    @Column
    public String getToAddress() {
        return toAddress;
    }

    public TransactionLog setToAddress(String toAddress) {
        this.toAddress = toAddress;
        return this;
    }

    @Column(columnDefinition = "longtext")
    @Converter(cls=JsonMapConverter.class)
    public Map<String, Object> getExtra() {
        return extra;
    }

    public TransactionLog setExtra(Map<String, Object> extra) {
        this.extra = extra;
        return this;
    }

    @Column
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public TransactionLog setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
