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

package io.stallion.assets;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.requests.RequestWrapper;
import io.stallion.services.Log;
import org.glassfish.jersey.server.ContainerRequest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

@Path("/st-assets/{path:.*}")
@Provider
public class AssetsFolderEndpointsResource {
    @javax.ws.rs.core.Context
    private UriInfo info;



    @javax.ws.rs.core.Context
    private Request requestContext;

    @javax.ws.rs.core.Context
    private HttpServletResponse response;

    @GET
    public Response stAssets(
            @PathParam("path") String path,
            @HeaderParam("referer") String referer,
            @QueryParam("isConcatenatedFileBundle") boolean isConcatenatedFileBundle,
            @QueryParam("isBundleFile") boolean isBundleFile,
            @QueryParam("bundleFilePath") String bundleFilePath
    ) throws Exception {
        // TODO: Clean up this requests mess
        RequestWrapper request = new RequestWrapper((ContainerRequest)requestContext);
        //request.setPath("/st-assets/" + path);
        AssetServing assetServing = new AssetServing(null, path, referer);
        if (isConcatenatedFileBundle) {
            return assetServing.serveFileBundle();
        } else if (isBundleFile) {
            return assetServing.serveFileBundleAsset(bundleFilePath);
        } else {
            //String path2 = (((ContainerRequest) requestContext).getUriInfo()).getPath().substring(11);
            //assetServing.serveFolderAsset(path);
            return assetServing.serveFolderAssetToResponse();
        }
    }


}
