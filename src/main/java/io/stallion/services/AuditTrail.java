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

import javax.persistence.Column;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Table(name="stallion_audit_trail")
public class AuditTrail extends ModelBase {
    private String table;
    private Long objectId;
    private String objectData;
    private Long userId;
    private String userEmail;
    private String remoteIp;
    private String userAgent;
    private Long valetId;
    private String valetEmail;
    private Long orgId;
    private ZonedDateTime createdAt;
    private boolean keepLongTerm = false;

    @Column
    public String getTable() {
        return table;
    }

    public AuditTrail setTable(String table) {
        this.table = table;
        return this;
    }

    @Column
    public Long getObjectId() {
        return objectId;
    }

    public AuditTrail setObjectId(Long objectId) {
        this.objectId = objectId;
        return this;
    }

    @Column(columnDefinition = "longtext")
    public String getObjectData() {
        return objectData;
    }

    public AuditTrail setObjectData(String objectData) {
        this.objectData = objectData;
        return this;
    }

    @Column
    public Long getUserId() {
        return userId;
    }

    public AuditTrail setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    @Column
    public String getUserEmail() {
        return userEmail;
    }

    public AuditTrail setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    @Column(length = 50)
    public String getRemoteIp() {
        return remoteIp;
    }

    public AuditTrail setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
        return this;
    }

    @Column
    public String getUserAgent() {
        return userAgent;
    }

    public AuditTrail setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Column
    public Long getValetId() {
        return valetId;
    }

    public AuditTrail setValetId(Long valetId) {
        this.valetId = valetId;
        return this;
    }

    @Column
    public String getValetEmail() {
        return valetEmail;
    }

    public AuditTrail setValetEmail(String valetEmail) {
        this.valetEmail = valetEmail;
        return this;
    }

    @Column
    public Long getOrgId() {
        return orgId;
    }

    public AuditTrail setOrgId(Long orgId) {
        this.orgId = orgId;
        return this;
    }

    @Column
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public AuditTrail setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Column
    public boolean isKeepLongTerm() {
        return keepLongTerm;
    }

    public AuditTrail setKeepLongTerm(boolean keepLongTerm) {
        this.keepLongTerm = keepLongTerm;
        return this;
    }
}
