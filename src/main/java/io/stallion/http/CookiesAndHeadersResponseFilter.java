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

import io.stallion.requests.RequestWrapper;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Map;

import static io.stallion.utils.Literals.list;

@Provider
@Priority(FilterPriorities.COOKIES_AND_HEADERS_RESPONSE_FILTER)
public class CookiesAndHeadersResponseFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        RequestWrapper req = new RequestWrapper(requestContext);
        if (req.getResponseCookies() != null) {
            for (Map.Entry<String, NewCookie> entry : req.getResponseCookies().entrySet()) {
                //responseContext.getCookies().put(entry.getKey(), entry.getValue());
                responseContext.getHeaders().add("Set-Cookie", entry.getValue());
            }
        }
        if (req.getResponseHeaders() != null) {
            for (Map.Entry<String, String> entry : req.getResponseHeaders().entrySet()) {
                requestContext.getHeaders().put(entry.getKey(), list(entry.getValue()));
            }
        }

    }
}
