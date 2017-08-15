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

package io.stallion.forum;import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.services.Log;
import io.stallion.templating.TemplateRenderer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


public class ForumEndpoints implements EndpointResource {
    private String baseTemplate = "stallion:forum-base.jinja";

    public ForumEndpoints() {

    }

    public ForumEndpoints(String template) {
        this.baseTemplate = template;
    }

    @GET
    @Path("/:slug")
    public String frontPage(@PathParam("slug") String slug) {
        Map ctx = map(val("baseForumTemplate", baseTemplate));
        return TemplateRenderer.instance().renderTemplate("stallion:forum/forum-front-page.jinja", ctx);
    }

    @GET
    @Path("/thread/:threadId/:slug")
    public String innerPage(@PathParam("threadId") Long threadId, @PathParam("slug") String slug) {
        Map ctx = map(val("baseForumTemplate", baseTemplate));
        return TemplateRenderer.instance().renderTemplate("stallion:forum/forum-inner-page.jinja", ctx);
    }



}
