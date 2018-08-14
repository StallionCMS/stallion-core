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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.Displayable;
import io.stallion.dataAccess.DisplayableModelController;
import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.ModelBase;
import io.stallion.hooks.HookRegistry;
import io.stallion.requests.DisplayableBySlugHook;
import io.stallion.restfulEndpoints.SlugRegistry;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.Role;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;


public class ContentPublishingResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        if (containerResponseContext.getStatus() == 404) {

            tryRenderForSlug(containerRequestContext, containerResponseContext);
        }
    }

    /**
     * Tries to route the request using a slug registered with the SlugRegistry
     *
     * Short circuits if the request is served, otherwise does nothings and returns
     *
     * @throws Exception
     */
    public void tryRenderForSlug(ContainerRequestContext request, ContainerResponseContext response)  {
        //String path = request.getUriInfo().getPath();
        String path = ((ContainerRequest) request).getRequestUri().getPath();
        if (path.equals("")) {
            path = "/";
        }
        Log.finer("Did not match any resource endpoints, calling tryRenderforSlug: {0}", path);
        Displayable item = null;
        if (SlugRegistry.instance().hasUrl(path)) {
            item = SlugRegistry.instance().lookup(path);
        }

        if (item == null) {
            return;
        }


        Model baseItem = (Model)item;

        // Item has an override domain, but we are accessing from a different domain
        if (!empty(item.getOverrideDomain())) {
            if (!item.getOverrideDomain().equals(request.getUriInfo().getRequestUri().getHost())) {
                return;
            }
        }

        // If the item is unpublished, return, unless we are a logged in staff user viewing the article preview
        /*
        if (!item.getPublished()) {
            String previewKey = request.getQueryParams().getOrDefault("stPreview", null);
            // No preview key in the query string, abort rendering
            if (previewKey == null) {
                return;
            }
            if (!request.getUser().isInRole(Role.STAFF_LIMITED)) {
                // Non-staff user with invalid preview key, abort rendering
                if (empty(item.getPreviewKey()) || !previewKey.equals(item.getPreviewKey())) {
                    return;
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
*/
        /*
        Map ctx = map(val("page", item), val("post", item), val("item", item));
        response.getMeta().setDescription(item.getMetaDescription());
        if (!empty(item.getTitleTag())) {
            response.getMeta().setTitle(item.getTitleTag());
        } else {
            response.getMeta().setTitle(item.getTitle());
        }
        response.getMeta().setBodyCssId(item.getSlugForCssId());
        response.getMeta().getCssClasses().add("st-" + ((ModelBase) item).getController().getBucket());
        response.getMeta().setOgType(item.getOgType());
        if (!empty(item.getRelCanonical())) {
            response.getMeta().setCanonicalUrl(item.getRelCanonical());
        }
        if (!empty(item.getContentType())) {
            response.setContentType(item.getContentType());
        }
        markHandled(200, MessageFormat.format("slug->displayableItemController for id={0} slug={1} controller={2}", baseItem.getId(), item.getSlug(), baseItem.getController().getClass().getName()));
        */
        Map ctx = map(val("page", item), val("post", item), val("item", item));
        String output = ((DisplayableModelController)baseItem.getController()).render(item, ctx);
        response.setEntity(output);
        response.setStatus(200);
    }


}
