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

import io.stallion.services.Log;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

@Provider
@Path("/st-resource/{pluginName}/{path:.*}")
public class AssetsResourceEndpointsResource {
    @javax.ws.rs.core.Context
    private UriInfo info;



    @javax.ws.rs.core.Context
    private Request requestContext;

    @javax.ws.rs.core.Context
    private HttpServletResponse response;


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

}
