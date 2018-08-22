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

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.stallion.Context;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.reflection.PropertyUtils;
import io.stallion.requests.IRequest;
import io.stallion.services.CloudStorageService;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.UploadStorageType;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.parboiled.common.FileUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.Part;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;

import static io.stallion.utils.Literals.*;


public class UploadRequestProcessor<U extends UploadedFile> {
    private IRequest stRequest;

    private String uploadsFolder;
    private UploadedFileController<U> fileController;
    private String urlToFetch;


    protected IRequest getStRequest() {
        return stRequest;
    }


    protected String getUploadsFolder() {
        return uploadsFolder;
    }

    protected UploadedFileController<U> getFileController() {
        return fileController;
    }

    public UploadRequestProcessor(String uploadsFolder) {
        this(uploadsFolder, UploadedFileController.instance());
    }
    public UploadRequestProcessor(String uploadsFolder, UploadedFileController<U> fileController) {
        this.stRequest = stRequest;
        if (!uploadsFolder.endsWith("/")) {
            uploadsFolder += "/";
        }
        this.uploadsFolder = uploadsFolder;
        this.fileController = fileController;

    }

    public U addFromUrl(String url, boolean isPublic) {
        this.urlToFetch = url;
        U uploaded = newUploadedFileInstance(isPublic);
        File file = downloadExternalToLocalFile(uploaded);
        if ("image".equals(uploaded.getType()) && Settings.instance().getUserUploads().getGenerateImageThumbnails() == true) {
            generateImageSizes(uploaded, file.getAbsolutePath());
        }
        if (Settings.instance().getUserUploads().getStorageType().equals(UploadStorageType.Cloud)) {
            transferAllToS3(uploaded);
        }
        uploaded.setProvisional(false);
        fileController.save(uploaded);
        return uploaded;
    }

    public U upload(IRequest stRequest) {
        this.stRequest = stRequest;
        try {
            return doUpload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected U doUpload() throws IOException {
        U uploaded = newUploadedFileInstance("true".equals(stRequest.getQueryParam("stUploadIsPublic", "")));
        File file = writeMultiPartToLocalFile(uploaded);
        if ("image".equals(uploaded.getType()) && Settings.instance().getUserUploads().getGenerateImageThumbnails() == true) {
            generateImageSizes(uploaded, file.getAbsolutePath());
        }
        if (Settings.instance().getUserUploads().getStorageType().equals(UploadStorageType.Cloud)) {
            transferAllToS3(uploaded);
        }
        uploaded.setProvisional(false);
        fileController.save(uploaded);
        return uploaded;
    }

    protected File downloadExternalToLocalFile(U uploaded) {

        URI uri = URI.create(urlToFetch);



        String fullFileName = FilenameUtils.getName(uri.getPath());
        String extension = FilenameUtils.getExtension(fullFileName);
        String fileName = truncate(fullFileName, 85);
        String relativePath = GeneralUtils.slugify(truncate(FilenameUtils.getBaseName(fullFileName), 75)) + "-" + DateUtils.mils() + "." + extension;
        relativePath = "stallion-file-" + uploaded.getId() + "/" + GeneralUtils.secureRandomToken(8) + "/" + relativePath;

        String destPath = uploadsFolder + relativePath;
        try {
            FileUtils.forceMkdir(new File(destPath).getParentFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        uploaded
                .setCloudKey(relativePath)
                .setExtension(extension)
                .setName(fileName)
                .setOwnerId(Context.getUser().getId())
                .setUploadedAt(DateUtils.utcNow())
                .setType(fileController.getTypeForExtension(extension))
        ;

        // Make raw URL
        String url = makeRawUrlForFile(uploaded, "org");
        uploaded.setRawUrl(url);

        fileController.save(uploaded);

        File outFile = new File(destPath);

        try {
            IOUtils.copy(
                    Unirest.get(urlToFetch).asBinary().getRawBody(),
                    org.apache.commons.io.FileUtils.openOutputStream(outFile)
            );
        } catch (UnirestException e) {
            throw new ClientErrorException("Error downloading file from " + url + ": " + e.getMessage(), 400, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outFile;

    }

    protected U newUploadedFileInstance(boolean isPublic) {
        U uploaded = fileController.newModel();
        if (!empty(uploaded.getId())) {
            throw new WebApplicationException("You cannot reuse the same upload file processor twice.");
        }
        Long id = DataAccessRegistry.instance().getTickets().nextId();
        String secret = GeneralUtils.randomTokenBase32(14);
        uploaded
                .setSecret(secret)
                .setPubliclyViewable(isPublic)
                .setPubliclyViewable(Settings.instance().getUserUploads().getUploadsArePublic())
                .setId(id)
                ;
        fileController.save(uploaded);
        return uploaded;
    }

    protected File writeMultiPartToLocalFile(U uploaded) throws IOException {
        stRequest.setAsMultiPartRequest();

        final String path = stRequest.getQueryParam("destination");
        final Part filePart = stRequest.getPart("file");
        String fullFileName = getFileNameFromPart(filePart);
        String extension = FilenameUtils.getExtension(fullFileName);
        String fileName = truncate(fullFileName, 85);
        String relativePath = GeneralUtils.slugify(truncate(FilenameUtils.getBaseName(fullFileName), 75)) + "-" + DateUtils.mils() + "." + extension;
        relativePath = "stallion-file-" + uploaded.getId() + "/" + GeneralUtils.secureRandomToken(8) + "/" + relativePath;

        String destPath = uploadsFolder + relativePath;
        FileUtils.forceMkdir(new File(destPath).getParentFile());

        uploaded
                .setCloudKey(relativePath)
                .setExtension(extension)
                .setName(fileName)
                .setOwnerId(Context.getUser().getId())
                .setUploadedAt(DateUtils.utcNow())
                .setType(fileController.getTypeForExtension(extension))
        ;

        // Make raw URL
        String url = makeRawUrlForFile(uploaded, "org");
        uploaded.setRawUrl(url);

        fileController.save(uploaded);

        OutputStream out = null;
        InputStream filecontent = null;
        boolean failed = true;
        File outFile = new File(destPath);
        Long amountRead = 0L;
        Long maxSize = Settings.instance().getUserUploads().getMaxFileSizeBytes();
        try {
            out = new FileOutputStream(destPath);
            filecontent = filePart.getInputStream();

            int read = 0;
            final byte[] bytes = new byte[1024];
            while ((read = filecontent.read(bytes)) != -1) {
                amountRead += read;
                if (amountRead > maxSize) {
                    throw new ClientErrorException("Uploaded file exceeded max size of " + maxSize + " bytes.", 400);
                }
                out.write(bytes, 0, read);
            }
            failed = false;
            Log.info("File{0}being uploaded to {1}",
                    new Object[]{fileName, path});
            uploaded.setSizeBytes(amountRead);
        } finally {
            if (out != null) {
                out.close();
            }
            if (filecontent != null) {
                filecontent.close();
            }
            if (failed) {
                if (outFile.exists()) {
                    outFile.delete();
                }
            }
        }
        return new File(destPath);
    }

    protected String makeRawUrlForFile(U uploadedFile, String size) {
        String endingSlug = GeneralUtils.slugify(truncate(FilenameUtils.getBaseName(uploadedFile.getName()), 50));
        if (!"org".equals(size)) {
            endingSlug += "." + size;
        }
        if (!empty(uploadedFile.getExtension())) {
            endingSlug += "." + uploadedFile.getExtension();
        }
        String url = "{cdnUrl}/st-user-uploads/view-file/" + uploadedFile.getSecret() + "/" + uploadedFile.getId() + "/org/" + endingSlug + "?ts=" + DateUtils.mils();
        return url;
    }

    protected void transferAllToS3(U uploaded) {
        for(String part: list("", "thumb", "medium", "small")) {
            transferToS3(uploaded, part);
        }
    }

    protected void transferToS3(U uploaded, String part) {
        String cloudKeyProperty = "cloudKey";
        String rawUrlProperty = "rawUrl";
        if (!empty(part)) {
            cloudKeyProperty = part + "CloudKey";
            rawUrlProperty = part + "RawUrl";
        }
        String cloudKey = (String) PropertyUtils.getProperty(uploaded, cloudKeyProperty);
        Log.info("Cloud key for {0} is {1}", cloudKeyProperty, cloudKey);
        if (empty(cloudKey)) {
            return;
        }
        File file = new File(uploadsFolder + cloudKey);
        if (!file.exists()) {
            Log.info("No file {0}", file.getAbsolutePath());
            return;
        }

        String newCloudKey = Settings.instance().getUserUploads().getUploadsPathPrefix() + cloudKey;
        if (newCloudKey.startsWith("/")) {
            newCloudKey = newCloudKey.substring(1);
        }
        Log.info("Upload to s3 file {0} bucket {1} key: {2}", file.getAbsolutePath(), Settings.instance().getUserUploads().getUploadsBucket(), newCloudKey);
        CloudStorageService.instance().uploadFile(
                file,
                Settings.instance().getUserUploads().getUploadsBucket(),
                newCloudKey,
                true
        );
        PropertyUtils.setProperty(uploaded, cloudKeyProperty, newCloudKey);
        String s3url;
        if (uploaded.isPubliclyViewable()) {
            s3url = or(
                    Settings.instance().getUserUploads().getUploadsBucketBaseUrl(),
                    CloudStorageService.instance().getBucketBaseUrl(Settings.instance().getUserUploads().getUploadsBucket()));
            if (!s3url.endsWith("/")) {
                s3url = s3url + "/";

            }
            s3url = s3url + newCloudKey;
        } else {
            s3url = "{cdnUrl}/st-user-uploads/view-cloud-file/" + uploaded.getSecret() + "/" + uploaded.getId() + "/" + part + "/" + newCloudKey + "?ts=" + DateUtils.mils();
        }
        PropertyUtils.setProperty(uploaded, rawUrlProperty, s3url);

        file.delete();



    }


    protected void generateImageSizes(U uploaded, String path) {
        try {
            File originalImageFile = new File(path);
            BufferedImage image = ImageIO.read(originalImageFile);
            if (image == null) {
                throw new ClientErrorException("Could not interpret uploaded file as a valid image file.", 400);
            }
            hydrateHeightAndWidth(uploaded, image);
            if (uploaded.getWidth() > 60) {
                createResized(uploaded, image, path, 120, 60, "thumb");
            }
            if (uploaded.getWidth() > 250) {
                createResized(uploaded, image, path, 550, 250, "small");
            }

            if (uploaded.getWidth() > 900) {
                createResized(uploaded, image, path, 1600, 900, "medium");
            }
            if (Settings.instance().getUserUploads().getResizeImagesToMax() > 0) {
                Scalr.Mode scaleMode = Scalr.Mode.FIT_TO_WIDTH;
                if (uploaded.getWidth() > Settings.instance().getUserUploads().getResizeImagesToMax()) {
                    createResized(
                            uploaded,
                            image,
                            path,
                            Settings.instance().getUserUploads().getResizeImagesToMax(),
                            Settings.instance().getUserUploads().getResizeImagesToMax(),
                            "org",
                            scaleMode
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected void hydrateHeightAndWidth(U uploaded, BufferedImage image) throws IOException {
        uploaded.setHeight(image.getHeight());
        uploaded.setWidth(image.getWidth());
    }
    public void createResized(U uploaded, BufferedImage image, String orgPath, int targetHeight, int targetWidth, String postfix) throws IOException {
        createResized(uploaded, image, orgPath, targetHeight, targetWidth, postfix, Scalr.Mode.FIT_TO_WIDTH);

    }
    public void createResized(U uploaded, BufferedImage image, String orgPath, int targetHeight, int targetWidth, String postfix, Scalr.Mode scalrMode) throws IOException {
        String imageFormat = uploaded.getExtension();

        BufferedImage scaledImg = Scalr.resize(image, Scalr.Method.QUALITY, scalrMode,
                targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int height = scaledImg.getHeight();
        int width = scaledImg.getWidth();
        ImageIO.write(scaledImg, imageFormat, baos);
        baos.flush();
        byte[] scaledImageInByte = baos.toByteArray();
        baos.close();



        String relativePath = FilenameUtils.removeExtension(uploaded.getCloudKey());
        if (!"org".equals(postfix)) {
            relativePath = relativePath + "." + postfix;
        }
        relativePath = relativePath + "." + uploaded.getExtension();
        String thumbnailPath = this.uploadsFolder + relativePath;
        Log.info("Write all byptes to {0}", thumbnailPath);
        FileUtils.writeAllBytes(scaledImageInByte, new File(thumbnailPath));
        Long sizeBytes = new File(thumbnailPath).length();
        //String url = "{cdnUrl}/st-publisher/files/view/" + uploaded.getSecret() + "/" + uploaded.getId() + "/" + postfix + "?ts=" + DateUtils.mils();
        String url = makeRawUrlForFile(uploaded, postfix);
        if (postfix.equals("thumb")) {
            uploaded.setThumbCloudKey(relativePath);
            uploaded.setThumbRawUrl(url);
            uploaded.setThumbHeight(height);
            uploaded.setThumbWidth(width);
        } else if (postfix.equals("small")) {
            uploaded.setSmallCloudKey(relativePath);
            uploaded.setSmallRawUrl(url);
            uploaded.setSmallHeight(height);
            uploaded.setSmallWidth(width);
        } else if (postfix.equals("medium")) {
            uploaded.setMediumCloudKey(relativePath);
            uploaded.setMediumRawUrl(url);
            uploaded.setMediumHeight(height);
            uploaded.setMediumWidth(width);
        } else if (postfix.equals("org")) {
            uploaded.setCloudKey(relativePath);
            uploaded.setRawUrl(url);
            uploaded.setSizeBytes(sizeBytes);
            uploaded.setHeight(height);
            uploaded.setWidth(width);
        }


        //return scaledImageInByte;
    }


    private String getFileNameFromPart(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        Log.info("Part Header = {0}", partHeader);
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(
                        content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

}
