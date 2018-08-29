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

import io.stallion.Context;
import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.dataAccess.filtering.Pager;
import io.stallion.dataAccess.filtering.QueryToPager;
import io.stallion.jerseyProviders.MinRole;
import io.stallion.jerseyProviders.ServletFileSender;
import io.stallion.jerseyProviders.XSRF;
import io.stallion.requests.ResponseComplete;
import io.stallion.services.CloudStorageService;
import io.stallion.settings.Settings;
import io.stallion.users.Role;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ContainerRequest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import java.io.*;
import java.net.URI;
import java.util.Map;

import static io.stallion.utils.Literals.*;


@MinRole(Role.MEMBER)
@Produces("application/json")
@Path("/st-user-uploads")
public class UploadedFileEndpoints<U extends UploadedFile>  {

    private UploadedFileController<U> fileController;

    public UploadedFileEndpoints() {
        this(UploadedFileController.instance());
    }
    public UploadedFileEndpoints(UploadedFileController fileController) {
        this.fileController = fileController;
    }

    @javax.ws.rs.core.Context
    HttpServletResponse response;

    @javax.ws.rs.core.Context
    ContainerRequest request;

    @GET
    @Path("/library")
    @MinRole(Role.ANON)
    public Object getFiles() {
        return getFilesByType(null);
    }

    @GET
    @Path("/library/:type")
    @MinRole(Role.ANON)
    public Object getFilesByType(@PathParam("type") String type) {
        FilterChain<U> chain = fileController.filterChain();

        if (!Context.getUser().isInRole(Role.STAFF_LIMITED) || true == Settings.instance().getUserUploads().getImageLibrarySharedBetweenUsers()) {
            chain = chain.filter("ownerId", Context.getUser().getId());
        }
        if (!empty(type)) {
            chain = chain.filter("type", type);
        }
        Pager pager = new QueryToPager<U>(Context.getRequest(), fileController, chain)
                .searchFields("name")
                .pageSize(10)
                .setDefaultSort("-uploadedAt")
                .pager();


        Map ctx =  map(val("pager", pager));
        return ctx;
    }

    @POST
    @Path("/upload-file")
    @Produces("application/json")
    @XSRF(false)
    @MinRole(Role.ANON)
    public Object uploadFile(@FormDataParam("file") InputStream fileInputStream,
                             @FormDataParam("file") FormDataContentDisposition fileMetaData) {
        if (!Context.getUser().isInRole(Settings.instance().getUserUploads().getMinimumRole())) {
            throw new ClientErrorException("You need to be at least in the role " + Settings.instance().getUserUploads().getMinimumRole() + " in order to upload files.", 403);
        }

        String folder = Settings.instance().getDataDirectory() + "/uploaded-files/";
        if (!new File(folder).isDirectory()) {
            new File(folder).mkdirs();
        }
        U uf = new UploadRequestProcessor<U>(folder, fileController).upload(request, fileInputStream, fileMetaData);
        return uf;
    }

    @GET
    @Path("/view-cloud-file/{secret}/{fileId}/{size}/{slug}")
    @MinRole(Role.ANON)
    public Object viewCloudFile(@PathParam("secret") String secret, @PathParam("fileId") Long fileId, @PathParam("size") String size) {
        U uf = fileController.forId(fileId);
        String cloudKey = null;
        if (size.equals("thumb")) {
            cloudKey = uf.getThumbCloudKey();
        } else if (size.equals("medium")) {
            cloudKey = uf.getMediumCloudKey();
        } else if (size.equals("small")) {
            cloudKey = uf.getSmallCloudKey();
        } else {
            cloudKey = uf.getCloudKey();
        }
        if (empty(cloudKey)) {
            throw new NotFoundException("File not found.");
        }
        if (!uf.getSecret().equals(secret)) {
            throw new ClientErrorException("Invalid file token.", 400);
        }
        if (!fileController.fileViewable(Context.getUser(), uf)) {
            throw new ClientErrorException("You do not have permission to view this file.", 403);
        }

        String url = CloudStorageService.instance().getSignedDownloadUrl(
                Settings.instance().getUserUploads().getUploadsBucket(),
                uf.getCloudKey()
                );
        throw new RedirectionException(302, URI.create(url));
    }

    @GET
    @Path("/view-file/{secret}/{fileId}/{size}/{slug}")
    @MinRole(Role.ANON)
    public Object viewFile(@PathParam("secret") String secret, @PathParam("fileId") Long fileId, @PathParam("size") String size) throws FileNotFoundException {
        U uf = fileController.forIdOrNotFound(fileId);
        String cloudKey;
        if (size.equals("thumb")) {
            cloudKey = uf.getThumbCloudKey();
        } else if (size.equals("medium")) {
            cloudKey = uf.getMediumCloudKey();
        } else if (size.equals("small")) {
            cloudKey = uf.getSmallCloudKey();
        } else {
            cloudKey = uf.getCloudKey();
        }
        if (empty(cloudKey)) {
            throw new NotFoundException("File not found.");
        }
        if (!uf.getSecret().equals(secret)) {
            throw new ClientErrorException("Invalid file token.", 400);
        }
        if (!fileController.fileViewable(Context.getUser(), uf)) {
            throw new ClientErrorException("You do not have permission to view this file.", 403);
        }

        String folder = Settings.instance().getDataDirectory() + "/uploaded-files/";
        String fullPath = folder + cloudKey;
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new FileNotFoundException("Local file not found cloud file. Local file: " + fullPath + " Cloud file ID: " + fileId);
        }
        sendAssetResponse(file);
        throw new ResponseComplete();
    }


    public void sendAssetResponse(File file) {
        try {
            sendAssetResponse(new FileInputStream(file), file.lastModified(), file.length(), file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAssetResponse(InputStream stream, long modifyTime, long contentLength, String fullPath) throws IOException {
        new ServletFileSender(Context.getRequest(), response).sendAssetResponse(stream, modifyTime, contentLength, fullPath);
    }


}
