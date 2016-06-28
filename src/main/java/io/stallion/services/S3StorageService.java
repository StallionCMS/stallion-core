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
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import io.stallion.exceptions.ConfigException;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.CloudStorageSettings;

import java.lang.annotation.Documented;
import java.sql.Date;
import java.util.Map;

import static io.stallion.utils.Literals.*;


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
        if (empty(settings.getAccessToken())) {
            throw new ConfigException("You are missing the setting secret in the stallion.toml section [cloudStorage]");
        }
        accessToken = settings.getAccessToken();
        secret = settings.getSecret();
        AWSCredentials credentials = new BasicAWSCredentials(accessToken, secret);
        client =  new AmazonS3Client(credentials);
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

}
