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

package io.stallion.http;

import io.stallion.Context;
import io.stallion.services.Log;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Priority(FilterPriorities.TEARDOWN_CONTEXT_RESPONSE_FILTER)
@Provider
public class TearDownContextResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        if (containerRequestContext.getUriInfo().getPath().startsWith("/st-assets")
                || containerRequestContext.getUriInfo().getPath().startsWith("/st-resource")) {
            Log.finest("Response: {0} {1} for request {2}", containerResponseContext.getLength(), containerResponseContext.getStatus(), containerRequestContext.getUriInfo().getPath());
        } else {
            Log.fine("Response: {0} {1} for request {2}", containerResponseContext.getLength(), containerResponseContext.getStatus(), containerRequestContext.getUriInfo().getPath());
        }
        Context.setValet(null, null);
        Context.setUser(null);
        Context.setOrg(null);
        Context.setRequest(null);

    }
}
