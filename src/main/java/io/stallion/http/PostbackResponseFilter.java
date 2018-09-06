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

import io.stallion.requests.IRequest;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Request;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static io.stallion.utils.Literals.mils;

@Provider
@Priority(FilterPriorities.POSTBACK_RESPONSE_FILTER)
@PreMatching
public class PostbackResponseFilter implements ContainerResponseFilter {


    @javax.ws.rs.core.Context
    private Request request;



    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext responseContext) throws IOException {
        if (!"get".equals(request.getMethod().toLowerCase())) {
            NewCookie cookie = new NewCookie(
                    IRequest.RECENT_POSTBACK_COOKIE,
                    String.valueOf(mils()),
                    null,
                    null,
                    null,
                    15,
                    false
            );
            responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, cookie);
            //responseContext.getCookies().put(cookie.getName(), cookie);
        }

    }
}
