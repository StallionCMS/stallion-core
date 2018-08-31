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

package io.stallion.tests.integration.javaSite;

import com.fasterxml.jackson.annotation.JsonView;
import io.stallion.dataAccess.ModelBase;

import io.stallion.utils.json.RestrictedViews;

import javax.persistence.Column;
import java.time.ZonedDateTime;

public class ExamplePojo extends ModelBase {
    private String displayName;
    private String email = "";
    private String userName = "";
    private Long age;
    private String content;
    private String status = "new";
    private String moderation = "pending";
    private Boolean isSpam = false;
    private String updateMessage = "";
    private Long updated;
    private String internalSecret;
    private ZonedDateTime dueAt;



    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    public String getModeration() {
        return moderation;
    }

    public void setModeration(String moderation) {
        this.moderation = moderation;
    }


    public Boolean getIsSpam() {
        return isSpam;
    }

    public void setIsSpam(Boolean isSpam) {
        this.isSpam = isSpam;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public void setUpdateMessage(String updateMessage) {
        this.updateMessage = updateMessage;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }


    @JsonView(RestrictedViews.Owner.class)
    public String getInternalSecret() {
        return internalSecret;
    }

    public void setInternalSecret(String internalSecret) {
        this.internalSecret = internalSecret;
    }

    public ZonedDateTime getDueAt() {
        return dueAt;
    }

    public ExamplePojo setDueAt(ZonedDateTime dueAt) {
        this.dueAt = dueAt;
        return this;
    }
}
