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

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import io.stallion.exceptions.ConfigException;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.CloudStorageSettings;
import io.stallion.utils.GeneralUtils;

import java.io.File;
import java.sql.Date;
import java.util.Map;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.utcNow;


public class S3StorageService extends CloudStorageService {
    private String accessToken = "";
    private String secret = "";
    private AmazonS3Client client;

    public S3StorageService() {
        CloudStorageSettings settings = Settings.instance().getCloudStorage();
        if (settings == null) {
            throw new ConfigException("You are missing the section [cloudStorage]\naccessToken=...\nsecret=... in your stallion.toml");
        }
        if (empty(settings.getAccessToken())) {
            throw new ConfigException("You are missing the setting accessKey in the stallion.toml section [cloudStorage]");
        }
        if (empty(settings.getSecret())) {
            throw new ConfigException("You are missing the setting secret in the stallion.toml section [cloudStorage]");
        }
        accessToken = settings.getAccessToken();
        secret = settings.getSecret();
        AWSCredentials credentials = new BasicAWSCredentials(accessToken, secret);
        //AmazonS3ClientBuilder.standard().withCredentials(credentials).build();
        client =  new AmazonS3Client(credentials);
    }

    @Override
    public String getBucketBaseUrl(String bucket) {
        return "https://s3.amazonaws.com/" + bucket;
    }

    public void uploadFile(File file, String bucket, String fileKey, boolean isPublic) {
        String contentType = GeneralUtils.guessMimeType(fileKey);
        uploadFile(file, bucket, fileKey, isPublic, contentType, null);
    }

    public void uploadFile(File file, String bucket, String fileKey, boolean isPublic, String contentType, Map<String, String> headers) {
        client.putObject(bucket, fileKey, file);
        PutObjectRequest req = new PutObjectRequest(bucket, fileKey, file);
        if (isPublic) {
            req.withCannedAcl(CannedAccessControlList.PublicRead);
        }
        ObjectMetadata meta = new ObjectMetadata();

        if (headers != null) {
            for (String key: headers.keySet()) {
                meta.setHeader(key, headers.get(key));
            }
        }
        if (!empty(contentType)) {
            meta.setContentType(contentType);
        }
        req.setMetadata(meta);
        client.putObject(req);

    }

    @Override
    public void deleteFile(String bucket, String fileKey) {
        DeleteObjectRequest req = new DeleteObjectRequest(bucket, fileKey);
        client.deleteObject(req);
    }

    public String getSignedDownloadUrl(String bucket, String fileKey) {
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, fileKey);
        req.setExpiration(Date.from(utcNow().plusMinutes(60).toInstant()));
        req.setMethod(HttpMethod.GET);
        return client.generatePresignedUrl(req).toString();
    }

    public String getSignedUploadUrl(String bucket, String fileKey, String contentType, Map headers) {
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, fileKey);
        if (!empty(contentType)) {
            req.setContentType(contentType);
        }
        if  (headers != null) {
            for (Object key: headers.keySet())
            req.addRequestParameter(key.toString(), headers.get(key).toString());
        }
        req.setExpiration(Date.from(utcNow().plusDays(2).toInstant()));
        req.setMethod(HttpMethod.PUT);
        return client.generatePresignedUrl(req).toString();
    }


    public AmazonS3Client getClient() {
        return client;
    }
}
