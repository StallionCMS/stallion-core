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

package io.stallion.settings.childSections;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;
import io.stallion.settings.SettingMeta;
import io.stallion.settings.Settings;
import io.stallion.users.Role;


public class UserUploadSettings implements SettingsSection {
    @SettingMeta(valBoolean = false)
    private Boolean enabled;
    @SettingMeta()
    private UploadStorageType storageType = UploadStorageType.File;
    @SettingMeta(valBoolean = false)
    private Boolean generateImageThumbnails = true;
    @SettingMeta(valInt = 0)
    private Integer resizeImagesToMax;
    @SettingMeta()
    private String uploadsDirectory;
    @SettingMeta()
    private Role minimumRole = Role.STAFF;
    @SettingMeta(valLong = 20000000)
    private Long maxFileSizeBytes;
    @SettingMeta(valBoolean = false)
    private Boolean uploadsArePublic = false;
    @SettingMeta()
    private Integer maxLibraryMegabytesPerUser;
    @SettingMeta()
    private String uploadsPathPrefix;
    @SettingMeta()
    private String uploadsBucket;
    @SettingMeta()
    private String uploadsBucketBaseUrl;
    @SettingMeta(valBoolean = false)
    private Boolean imageLibrarySharedBetweenUsers;

    public Boolean getEnabled() {
        return enabled;
    }

    public UserUploadSettings setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public UploadStorageType getStorageType() {
        return storageType;
    }

    public UserUploadSettings setStorageType(UploadStorageType storageType) {
        this.storageType = storageType;
        return this;
    }

    public Boolean getGenerateImageThumbnails() {
        return generateImageThumbnails;
    }

    public UserUploadSettings setGenerateImageThumbnails(Boolean generateImageThumbnails) {
        this.generateImageThumbnails = generateImageThumbnails;
        return this;
    }

    public Integer getResizeImagesToMax() {
        return resizeImagesToMax;
    }

    public UserUploadSettings setResizeImagesToMax(Integer resizeImagesToMax) {
        this.resizeImagesToMax = resizeImagesToMax;
        return this;
    }

    public String getUploadsDirectory() {
        return uploadsDirectory;
    }

    public UserUploadSettings setUploadsDirectory(String uploadsDirectory) {
        this.uploadsDirectory = uploadsDirectory;
        return this;
    }

    public Role getMinimumRole() {
        return minimumRole;
    }

    public UserUploadSettings setMinimumRole(Role minimumRole) {
        this.minimumRole = minimumRole;
        return this;
    }

    public Long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public UserUploadSettings setMaxFileSizeBytes(Long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
        return this;
    }

    public Boolean getUploadsArePublic() {
        return uploadsArePublic;
    }

    public UserUploadSettings setUploadsArePublic(Boolean uploadsArePublic) {
        this.uploadsArePublic = uploadsArePublic;
        return this;
    }

    public Integer getMaxLibraryMegabytesPerUser() {
        return maxLibraryMegabytesPerUser;
    }

    public UserUploadSettings setMaxLibraryMegabytesPerUser(Integer maxLibraryMegabytesPerUser) {
        this.maxLibraryMegabytesPerUser = maxLibraryMegabytesPerUser;
        return this;
    }

    public String getUploadsPathPrefix() {
        return uploadsPathPrefix;
    }

    public UserUploadSettings setUploadsPathPrefix(String uploadsPathPrefix) {
        this.uploadsPathPrefix = uploadsPathPrefix;
        return this;
    }

    public String getUploadsBucket() {
        return uploadsBucket;
    }

    public UserUploadSettings setUploadsBucket(String uploadsBucket) {
        this.uploadsBucket = uploadsBucket;
        return this;
    }

    public String getUploadsBucketBaseUrl() {
        return uploadsBucketBaseUrl;
    }

    public UserUploadSettings setUploadsBucketBaseUrl(String uploadsBucketBaseUrl) {
        this.uploadsBucketBaseUrl = uploadsBucketBaseUrl;
        return this;
    }

    public Boolean getImageLibrarySharedBetweenUsers() {
        return imageLibrarySharedBetweenUsers;
    }

    public UserUploadSettings setImageLibrarySharedBetweenUsers(Boolean imageLibrarySharedBetweenUsers) {
        this.imageLibrarySharedBetweenUsers = imageLibrarySharedBetweenUsers;
        return this;
    }
}
