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


import io.stallion.Context;
import io.stallion.requests.ResponseComplete;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.services.Log;
import io.stallion.users.User;
import org.glassfish.jersey.internal.inject.CustomAnnotationLiteral;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

@Provider
@Path("/")
public class AssetsEndpointsResource  {

    @javax.ws.rs.core.Context
    private UriInfo info;

    @javax.ws.rs.core.Context
    private HttpServletRequest servletRequest;

    @javax.ws.rs.core.Context
    private HttpServletResponse servletResponse;

    @javax.ws.rs.core.Context
    private ServletContext servletContext;

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
    public Object stAssets(
            @PathParam("path") String path,
            @QueryParam("isConcatenatedFileBundle") boolean isConcatenatedFileBundle,
            @QueryParam("isBundleFile") boolean isBundleFile
    ) throws Exception {
        AssetServing assetServing = new AssetServing(Context.getRequest(), Context.getResponse());
        if (isConcatenatedFileBundle) {
            assetServing.serveFileBundle();
        } else if (isBundleFile) {
            assetServing.serveFileBundleAsset();
        } else {
            assetServing.serveFolderAsset(Context.getRequest().getPath().substring(11));
        }
        throw new ResponseComplete();
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
            @QueryParam("isFullResourceBundle") boolean isFullResourceBundle
    ) throws Exception {
        if (isFullResourceBundle) {
            new AssetServing(Context.getRequest(), Context.getResponse()).serveResourceBundle();
        } else {
            new AssetServing(Context.getRequest(), Context.getResponse()).serveResourceAsset();
        }
        throw new ResponseComplete();
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
