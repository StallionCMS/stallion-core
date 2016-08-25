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

package io.stallion.dataAccess.file;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.Context;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.DisplayableModelController;
import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.dataAccess.filtering.Pager;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.services.Log;
import io.stallion.settings.ContentFolder;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


public class ListingEndpoints implements EndpointResource {

    public static void register() {
        for(ContentFolder config: Settings.instance().getFolders()) {
            if (config.isListingEnabled()) {
                EndpointsRegistry.instance().addResource("", new ListingEndpoints(config));
            }
        }
    }

    private ContentFolder config;

    public ListingEndpoints(ContentFolder config) {
        this.config = config;
    }

    public Map<String, Object> makeContext() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("blogConfig", config);
        context.put("postsFilter", filterChain());
        if (!empty(config.getListingTitle())) {
            Context.getResponse().getMeta().setTitle(config.getListingTitle());
        }
        if (!empty(config.getListingMetaDescription())) {
            Context.getResponse().getMeta().setDescription(config.getListingMetaDescription());
        }
        String blogRoot = GeneralUtils.slugify(config.getListingRootUrl());
        if (empty(blogRoot) || blogRoot.equals("-")) {
            blogRoot = "root";
        } else if (blogRoot.startsWith("-")) {
            blogRoot = blogRoot.substring(1);
        }
        Context.getResponse().getMeta().setBodyCssId("flatBlog-" + blogRoot);
        Context.getResponse().getMeta().getCssClasses().add("st-flatBlog-" + blogRoot);
        return context;
    }

    private DisplayableModelController<TextItem> postsController() {
        return (DisplayableModelController<TextItem>)DataAccessRegistry.instance().get(config.getPath());
    }

    private FilterChain<TextItem> filterChain() throws Exception {
        return postsController().filter("published", true);
    }

    @GET
    @Path("/")
    @Produces("text/html")
    public String listHome() throws Exception {
        return listHome(0);
    }

    @GET
    @Path("/page/:page/")
    @Produces("text/html")
    public String listHome(@PathParam("page") Integer page) throws Exception {
        Map<String, Object> context = makeContext();
        Pager pager = filterChain()
                .sort("publishDate", "desc")
                .pager(page, config.getItemsPerPage());
        context.put("postsPager", pager);
        if (pager.getItems().size() == 0) {
            Context.getResponse().setStatus(404);
        }
        return TemplateRenderer.instance().renderTemplate(config.getListingTemplate(), context);
    }

    @GET
    @Path("/feed/")
    @Produces("text/xml")
    public String feed() throws Exception {
        return rss();
    }

    @GET
    @Path("/rss.xml")
    @Produces("text/xml")
    public String rss() throws Exception  {
        Map<String, Object> context = makeContext();
        Pager pager = filterChain()
                .sort("publishDate", "desc")
                .pager(0, 20);
        context.put("postsPager", pager);
        context.put("blogUrl", Context.getSettings().getSiteUrl() + config.getFullPath());
        ZonedDateTime buildTime = ZonedDateTime.of(2015, 1, 1, 12, 0, 0, 0, GeneralUtils.UTC);
        if (pager.getItems().size() > 0) {
            TextItem item = (TextItem) pager.getItems().get(0);
            buildTime = item.getPublishDate().plusMinutes(1);
        }
        context.put("generator", Settings.instance().getMetaGenerator());
        context.put("lastBuildDate", DateUtils.formatLocalDateFromZonedDate(buildTime, "EEE, dd MMM yyyy HH:mm:ss Z"));
        return TemplateRenderer.instance().renderTemplate(
                getClass().getResource("/templates/rss.jinja").toString(),
                context);
    }



    @GET
    @Path("/archives/:year/:month/")
    public String listByDate(@PathParam("year") String year, @PathParam("month") String month) throws Exception {
        Map<String, Object> context = makeContext();
        Pager pager = filterChain()
                .filter("year", year)
                .filter("month", month)
                .sort("publishDate", "desc")
                .pager(0, 5000);
        context.put("postsPager", pager);
        return TemplateRenderer.instance().renderTemplate(config.getListingTemplate(), context);

    }

    @GET
    @Path("/by-tag/:tag/")
    @Produces("text/html")
    public String listByTag(@PathParam("tag") String tag) throws Exception {
        Map<String, Object> context = makeContext();
        Pager pager = filterChain()
                .filter("tags", tag, "in")
                .sort("publishDate", "desc")
                .pager(0, 5000);
        context.put("postsPager", pager);
        return TemplateRenderer.instance().renderTemplate(config.getListingTemplate(), context);
    }
}
