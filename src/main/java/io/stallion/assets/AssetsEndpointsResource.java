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


import io.stallion.Context;
import io.stallion.jerseyProviders.BodyParam;
import io.stallion.requests.RequestWrapper;
import io.stallion.services.Log;
import org.glassfish.jersey.server.ContainerRequest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

@Provider
@Path("/")
public class AssetsEndpointsResource  {

    @javax.ws.rs.core.Context
    private UriInfo info;



    @javax.ws.rs.core.Context
    private Request requestContext;

    @javax.ws.rs.core.Context
    private HttpServletResponse response;

    @Path("/st-inject")
    @POST
    @Consumes("application/json")
    public Object postThis(@BodyParam("foo") String foo, @BodyParam("bar") Long bar) {

        //String foo = "";
        Log.info("Posted foo={0} bar={1}", foo, bar);
        return "Foo is: '" + foo + "' Bar is="+bar;
    }

    /*
    @Path("/st-inject")
    @POST
    @Consumes("application/json")
    public Object postThis(@BodyParam("foo") BodyParam foo, @BodyParam("bar") BodyParam bar) {
        Log.info("Posted foo={0} bar={1}", foo, bar);
        return "Foo is: '" + foo + "' Bar is="+bar;
    }
     */

    @Path("/st-assets/{path:.*}")
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

    @Path("/hello")
    @GET
    public Object hello() {
        return "Hellow, you";
    }

    @Path("/st-resource/{pluginName}/{path:.*}")
    @GET
    public Object stAssets(
            @PathParam("pluginName") String pluginName,
            @PathParam("path") String path,
            @HeaderParam("referer") String referer,
            @QueryParam("isFullResourceBundle") boolean isFullResourceBundle,
            @QueryParam("bundlePath") String bundlePath
    ) throws Exception {
        Log.info("Serve resource for plugin {0} path {1} isFullresourecBundle: {2}", pluginName, path, isFullResourceBundle);
        if (isFullResourceBundle) {
            return new AssetServing(pluginName, path, referer).serveResourceBundle();
        } else {
            return new AssetServing(pluginName, path, referer).serveResourceAsset(bundlePath);
        }
    }
    /*
    public void tryRouteAssetRequest() throws Exception{
        if (request.getPath().startsWith("/st-assets") && "true".equals(request.getParameter("isConcatenatedFileBundle"))) {
            serveFileBundle();
        } else if (request.getPath().startsWith("/st-assets") && "true".equals(request.getParameter("isBundleFile"))) {
            serveFileBundleAsset();
        } else if (request.getPath().startsWith("/st-resource") && "true".equals(request.getParameter("isFullResourceBundle"))) {
            serveResourceBundle();
        } else if (request.getPath().startsWith("/st-resource/")) {
            serveResourceAsset();

        } else if (request.getPath().startsWith("/st-assets/")) {
            serveFolderAsset(request.getPath().substring(11));
            // Deprecated
        } else if (request.getPath().startsWith("/st-resource-bundle/")) {
            serveResourceBundle();
            // Deprecated
        } else if (request.getPath().startsWith("/st-bundle-v2/")) {
            serveResourceBundle();
        }

    }
     */
}
