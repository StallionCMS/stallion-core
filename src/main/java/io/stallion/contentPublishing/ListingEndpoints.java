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
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.DisplayableModelController;
import io.stallion.dataAccess.file.TextItem;
import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.dataAccess.filtering.Pager;
import io.stallion.requests.MetaInformation;
import io.stallion.settings.ContentFolder;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.map;


public class ListingEndpoints  {

    private static Map<String, ContentFolder> configMap;

    public static void register(ResourceConfig rc) {
        configMap = map();
        for(ContentFolder config: Settings.instance().getFolders()) {
            if (config.isListingEnabled()) {
                String rootUrl = config.getListingRootUrl();
                if (StringUtils.endsWith(rootUrl, "/")) {
                    rootUrl = rootUrl.substring(0, rootUrl.length()-1);
                }
                Resource.Builder resourceBuilder =
                        Resource.builder(ListingEndpoints.class, false)
                                .name(rootUrl)
                                .path(rootUrl);
                rc.registerResources(resourceBuilder.build());
                configMap.put(rootUrl, config);
            }
        }
    }

    @javax.ws.rs.core.Context
    private ContainerRequest request;

    /**
     * For rendering any individual content page that has a slug that matches the root URL of this listing.
     *
     * @param path
     * @return
     * @throws Exception
     */
    @GET
    @Path("{path:.*}")
    @Produces("text/html")
    public Response renderForSlugCatchAll(@PathParam("path") String path) throws Exception {
        return new ContentSlugCatchallResource().setRequest(request).renderPageForPath(path);
    }

    @GET
    @Produces("text/html")
    public String listHome() throws Exception {
        return listHome(0);
    }


    @GET
    @Path("/page/{page}/")
    @Produces("text/html")
    public String listHome(@PathParam("page") Integer page) throws Exception {
        Map<String, Object> context = makeContext();
        Pager pager = filterChain()
                .sort("publishDate", "desc")
                .pager(page, getConfig().getItemsPerPage());
        context.put("postsPager", pager);
        if (pager.getItems().size() == 0) {
            throw new NotFoundException("Results page not found.");
        }
        return TemplateRenderer.instance().renderTemplate(getConfig().getListingTemplate(), context);
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
        context.put("blogUrl", Context.getSettings().getSiteUrl() + getConfig().getFullPath());
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
    @Path("/archives/{year}/{month}/")
    public String listByDate(@PathParam("year") String year, @PathParam("month") String month) throws Exception {
        Map<String, Object> context = makeContext();
        Pager pager = filterChain()
                .filter("year", year)
                .filter("month", month)
                .sort("publishDate", "desc")
                .pager(0, 5000);
        context.put("postsPager", pager);
        return TemplateRenderer.instance().renderTemplate(getConfig().getListingTemplate(), context);

    }

    @GET
    @Path("/by-tag/{tag}/")
    @Produces("text/html")
    public String listByTag(@PathParam("tag") String tag) throws Exception {
        Map<String, Object> context = makeContext();
        Pager pager = filterChain()
                .filter("tags", tag, "in")
                .sort("publishDate", "desc")
                .pager(0, 5000);
        context.put("postsPager", pager);
        return TemplateRenderer.instance().renderTemplate(getConfig().getListingTemplate(), context);
    }

    private ContentFolder currentConfig = null;

    private ContentFolder getConfig() {
        if (currentConfig == null) {
            Resource parent = (((ContainerRequest) request).getUriInfo())
                    .getMatchedResourceMethod()
                    .getParent();
            if (parent.getParent() != null) {
                parent = parent.getParent();
            }
            String resourcePath = parent.getPath();

            currentConfig = configMap.get(resourcePath);
        }
        return currentConfig;
    }


    public Map<String, Object> makeContext() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("blogConfig", getConfig());
        context.put("postsFilter", filterChain());
        // TODO fix hydration of meta



        String blogRoot = GeneralUtils.slugify(getConfig().getListingRootUrl());
        if (empty(blogRoot) || blogRoot.equals("-")) {
            blogRoot = "root";
        } else if (blogRoot.startsWith("-")) {
            blogRoot = blogRoot.substring(1);
        }

        MetaInformation meta = new MetaInformation();
        meta.setBodyCssId("flatBlog-" + blogRoot);
        meta.getCssClasses().add("st-flatBlog-" + blogRoot);
        meta.setTitle(getConfig().getListingTitle());
        meta.setDescription(getConfig().getListingMetaDescription());
        context.put("meta", meta);

        return context;
    }

    private DisplayableModelController<TextItem> postsController() {
        return (DisplayableModelController<TextItem>)DataAccessRegistry.instance().get(getConfig().getPath());
    }

    private FilterChain<TextItem> filterChain() throws Exception {
        return postsController().filter("published", true);
    }

}
