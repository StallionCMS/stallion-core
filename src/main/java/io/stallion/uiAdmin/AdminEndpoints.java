/*
 * Stallion: A Modern Content Management System
 *
 * Copyright (C) 2015 - 2016 Patrick Fitzsimmons.
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

package io.stallion.uiAdmin;

import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.templating.TemplateRenderer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


public class AdminEndpoints implements EndpointResource {

    @GET
    @Path("/settings")
    @Produces("text/html")
    public Object adminSettings() {
        String html = TemplateRenderer.instance().renderTemplate("stallion:admin/admin-settings.jinja");
        return html;
    }

    @GET
    @Path("/riot-example")
    @Produces("text/html")
    public Object riotExample() {
        String html = TemplateRenderer.instance().renderTemplate("stallion:semipublic/riot-example.jinja");
        return html;
    }

    public static void register() {
        EndpointsRegistry.instance().addResource("/st-admin/admin", new AdminEndpoints());
    }
}

