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

package io.stallion.sitemaps;

import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.settings.SecondaryDomain;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.util.List;
import java.util.Map;

import static io.stallion.Context.request;
import static io.stallion.utils.Literals.*;


public class SiteMapEndpoints implements EndpointResource {

    @GET
    @Path("/sitemap.xml")
    @Produces("text/xml")
    public String siteMap() {
        Map<String, Object> context = map();
        List<SiteMapItem> items = SiteMapController.instance().getAllItemsForDomain(request().getHost());
        context.put("items", items);
        return TemplateRenderer.instance().renderTemplate(getClass().getResource("/templates/sitemap.xml.jinja"), context);
    }

    public static void registerEndpoints() {
        EndpointsRegistry.instance().addResource("", new SiteMapEndpoints());
        for(SecondaryDomain sd: Settings.instance().getSecondaryDomains()) {
            SiteMapEndpoints sme = new SiteMapEndpoints();
            /*
            BaseRestEndpoint endpoint = new JavaRestEndpoint()
                    .setMethodName("siteMap")
                    .setResource(sme)
                    .setRole(Role.ANON)
                    .setMethod("GET")
                    .setRoute(sd.getRewriteRoot() + "/sitemap.xml");
             */
            EndpointsRegistry.instance().addResource(sd.getRewriteRoot(), new SiteMapEndpoints());

        }
    }

}
