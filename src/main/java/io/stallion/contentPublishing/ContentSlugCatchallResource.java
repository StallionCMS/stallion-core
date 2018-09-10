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
import io.stallion.dataAccess.Displayable;
import io.stallion.dataAccess.DisplayableModelController;
import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.ModelBase;
import io.stallion.requests.IRequest;
import io.stallion.requests.MetaInformation;
import io.stallion.requests.RequestWrapper;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.Role;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Map;

import static io.stallion.utils.Literals.*;

@Path("{path:.*}")
public class ContentSlugCatchallResource {

    @javax.ws.rs.core.Context
    private ContainerRequest request;

    public ContentSlugCatchallResource setRequest(ContainerRequest request) {
        this.request = request;
        return this;
    }

    @GET
    public Response renderPageForPath(@PathParam("path") String path) {
        Response response = tryRenderForSlug(path);
        if (response != null) {
            return response;
        }
        throw new NotFoundException("Page not found.");
    }


    /**
     * Tries to route the request using a slug registered with the SlugRegistry
     *
     * Short circuits if the request is served, otherwise does nothings and returns
     *
     * @throws Exception
     */
    public Response tryRenderForSlug(String path)  {

        RequestWrapper stRequest = new RequestWrapper(request);
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        Log.finer("Did not match any resource endpoints, calling tryRenderforSlug: {0}", path);
        Displayable item = null;
        if (SlugRegistry.instance().hasUrl(path)) {
            item = SlugRegistry.instance().lookup(path);
        }

        if (item == null) {
            return null;
        }


        Model baseItem = (Model)item;

        // Item has an override domain, but we are accessing from a different domain
        if (!empty(item.getOverrideDomain())) {
            if (!item.getOverrideDomain().equals(stRequest.getHost())) {
                return null;
            }
        }

        // If the item is unpublished, return, unless we are a logged in staff user viewing the article preview

        if (!item.getPublished()) {
            String previewKey = stRequest.getQueryParam("stPreview", null);
            // No preview key in the query string, abort rendering
            if (previewKey == null) {
                return null;
            }
            if (!Context.getUser().isInRole(Role.STAFF_LIMITED)) {
                // Non-staff user with invalid preview key, abort rendering
                if (empty(item.getPreviewKey()) || !previewKey.equals(item.getPreviewKey())) {
                    return null;
                }
            }
        }

        // In local mode, check for newer version from the file system so we do not load stale items
        if (Settings.instance().getLocalMode()) {
            if (!baseItem.getController().getPersister().isDbBacked()) {
                baseItem = baseItem.getController().getStash().reloadIfNewer(baseItem);
                item = (Displayable)baseItem;
            }
        }


        MetaInformation meta = new MetaInformation();
        meta.setDescription(item.getMetaDescription());
        if (!empty(item.getTitleTag())) {
            meta.setTitle(item.getTitleTag());
        } else {
            meta.setTitle(item.getTitle());
        }
        meta.setBodyCssId(item.getSlugForCssId());
        meta.getCssClasses().add("st-" + ((ModelBase) item).getController().getBucket());
        meta.setOgType(item.getOgType());
        if (!empty(item.getRelCanonical())) {
            meta.setCanonicalUrl(item.getRelCanonical());
        }
        String contentType = "text/html";
        if (!empty(item.getContentType())) {
            contentType = item.getContentType();
        }


        Map ctx = map(val("page", item), val("post", item), val("item", item));
        String output = ((DisplayableModelController)baseItem.getController()).render(item, ctx, meta);
        return Response.ok(output, contentType).build();
    }

}
