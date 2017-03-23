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

package io.stallion.contentPublishing;


import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.db.Converter;
import io.stallion.dataAccess.db.converters.JsonMapConverter;
import io.stallion.settings.Settings;
import io.stallion.utils.json.JSON;

import javax.persistence.Column;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.Map;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.map;

@Table(name="stallion_uploaded_files")
public class UploadedFile extends ModelBase {
    private String name = "";
    private String rawUrl = "";
    private String cloudKey = "";
    private String type = "";
    private String extension = "";
    private ZonedDateTime uploadedAt;
    private Integer height = 0;
    private Integer width = 0;
    private Long ownerId = 0L;
    private String secret = "";
    private boolean publiclyViewable = false;
    private Long sizeBytes = 0L;
    private boolean provisional = true;

    // Thumb limited to 350px wide or 250px tall
    private String thumbCloudKey = "";
    private String thumbRawUrl = "";
    private Integer thumbWidth = 0;
    private Integer thumbHeight = 0;

    private String smallCloudKey = "";
    private String smallRawUrl = "";
    private Integer smallWidth = 0;
    private Integer smallHeight = 0;


    // Medium is limited to 900px wide
    private String mediumCloudKey = "";
    private Integer mediumWidth = 0;
    private Integer mediumHeight = 0;
    private String mediumRawUrl = "";

    private Map extra = map();

    @Column(length = 100)
    public String getName() {
        return name;
    }

    public UploadedFile setName(String name) {
        this.name = name;
        return this;
    }


    public String getUrl() {
        return rawUrl.replace("{cdnUrl}", Settings.instance().getCdnUrl());
    }

    public String getThumbUrl() {
        if (empty(getThumbRawUrl())) {
            return getUrl();
        } else {
            return getThumbRawUrl().replace("{cdnUrl}", Settings.instance().getCdnUrl());
        }
    }

    public String getSmallUrl() {
        if (empty(getSmallRawUrl())) {
            return getUrl();
        } else {
            return getSmallRawUrl().replace("{cdnUrl}", Settings.instance().getCdnUrl());
        }

    }


    public String getMediumUrl() {
        if (empty(getMediumRawUrl())) {
            return getUrl();
        } else {
            return getMediumRawUrl().replace("{cdnUrl}", Settings.instance().getCdnUrl());
        }



    }

    @Column(nullable = false)
    public Long getSizeBytes() {
        return sizeBytes;
    }

    public UploadedFile setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
        return this;
    }

    @Column(nullable = false)
    public boolean isProvisional() {
        return provisional;
    }

    public UploadedFile setProvisional(boolean provisional) {
        this.provisional = provisional;
        return this;
    }

    @Column
    public Long getOwnerId() {
        return ownerId;
    }

    public UploadedFile setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    @Column
    public String getSecret() {
        return secret;
    }

    public UploadedFile setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    @Column(nullable = false)
    public boolean isPubliclyViewable() {
        return publiclyViewable;
    }

    public UploadedFile setPubliclyViewable(boolean publiclyViewable) {
        this.publiclyViewable = publiclyViewable;
        return this;
    }

    @Column(length = 255)
    public String getRawUrl() {
        return rawUrl;
    }

    public UploadedFile setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
        return this;
    }

    @Column(length = 255)
    public String getCloudKey() {
        return cloudKey;
    }

    public UploadedFile setCloudKey(String cloudKey) {
        this.cloudKey = cloudKey;
        return this;
    }

    @Column(length = 30)
    public String getType() {
        return type;
    }

    public UploadedFile setType(String type) {
        this.type = type;
        return this;
    }

    @Column(length = 15)
    public String getExtension() {
        return extension;
    }

    public UploadedFile setExtension(String extension) {
        this.extension = extension;
        return this;
    }

    @Column
    public ZonedDateTime getUploadedAt() {
        return uploadedAt;
    }

    public UploadedFile setUploadedAt(ZonedDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
        return this;
    }

    @Column
    public Integer getHeight() {
        return height;
    }

    public UploadedFile setHeight(Integer height) {
        this.height = height;
        return this;
    }

    @Column
    public Integer getWidth() {
        return width;
    }

    public UploadedFile setWidth(Integer width) {
        this.width = width;
        return this;
    }

    @Column(length = 255)
    public String getThumbCloudKey() {
        return thumbCloudKey;
    }

    public UploadedFile setThumbCloudKey(String thumbCloudKey) {
        this.thumbCloudKey = thumbCloudKey;
        return this;
    }

    @Column(length = 255)
    public String getThumbRawUrl() {
        return thumbRawUrl;
    }

    public UploadedFile setThumbRawUrl(String thumbRawUrl) {
        this.thumbRawUrl = thumbRawUrl;
        return this;
    }

    @Column
    public Integer getThumbWidth() {
        return thumbWidth;
    }

    public UploadedFile setThumbWidth(Integer thumbWidth) {
        this.thumbWidth = thumbWidth;
        return this;
    }

    @Column
    public Integer getThumbHeight() {
        return thumbHeight;
    }

    public UploadedFile setThumbHeight(Integer thumbHeight) {
        this.thumbHeight = thumbHeight;
        return this;
    }

    @Column(length = 255)
    public String getMediumCloudKey() {
        return mediumCloudKey;
    }

    public UploadedFile setMediumCloudKey(String mediumCloudKey) {
        this.mediumCloudKey = mediumCloudKey;
        return this;
    }

    @Column
    public Integer getMediumWidth() {
        return mediumWidth;
    }

    public UploadedFile setMediumWidth(Integer mediumWidth) {
        this.mediumWidth = mediumWidth;
        return this;
    }

    @Column
    public Integer getMediumHeight() {
        return mediumHeight;
    }

    public UploadedFile setMediumHeight(Integer mediumHeight) {
        this.mediumHeight = mediumHeight;
        return this;
    }

    @Column(length = 255)
    public String getMediumRawUrl() {
        return mediumRawUrl;
    }

    public UploadedFile setMediumRawUrl(String mediumRawUrl) {
        this.mediumRawUrl = mediumRawUrl;
        return this;
    }

    @Column(length = 255)
    public String getSmallCloudKey() {
        return smallCloudKey;
    }

    public UploadedFile setSmallCloudKey(String smallCloudKey) {
        this.smallCloudKey = smallCloudKey;
        return this;
    }

    @Column(length = 255)
    public String getSmallRawUrl() {
        return smallRawUrl;
    }

    public UploadedFile setSmallRawUrl(String smallRawUrl) {
        this.smallRawUrl = smallRawUrl;
        return this;
    }

    @Column
    public Integer getSmallWidth() {
        return smallWidth;
    }

    public UploadedFile setSmallWidth(Integer smallWidth) {
        this.smallWidth = smallWidth;
        return this;
    }

    @Column
    public Integer getSmallHeight() {
        return smallHeight;
    }

    public UploadedFile setSmallHeight(Integer smallHeight) {
        this.smallHeight = smallHeight;
        return this;
    }


    @Column(columnDefinition = "longtext")
    @Converter(cls=JsonMapConverter.class)
    public Map getExtra() {
        return extra;
    }

    public UploadedFile setExtra(Map extra) {
        this.extra = extra;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return JSON.stringify(this).equals(JSON.stringify(obj));
    }
}
