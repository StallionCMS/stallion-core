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
import io.stallion.requests.RequestWrapper;
import org.glassfish.jersey.server.ExtendedUriInfo;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.set;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class XsrfRequestFilter implements ContainerRequestFilter {
    public static final String HEADER_NAME = "X-Requested-By";


    @javax.ws.rs.core.Context
    private HttpServletResponse httpResponse;

    private static final Set<String> SKIP_METHODS = Collections.unmodifiableSet(set("GET", "OPTIONS", "HEAD"));

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        if (SKIP_METHODS.contains(containerRequestContext.getMethod())) {
            return;
        }
        if (containerRequestContext.getHeaders().containsKey(HEADER_NAME)) {
            return;
        }
        if (!requiresValidXsrfTokens(containerRequestContext)) {
            return;
        }
        else throw new BadRequestException("Cross-site forgery protection check failed. You must included the 'X-Requested-By' header.");

    }


    private boolean requiresValidXsrfTokens(ContainerRequestContext containerRequestContext) {

        IRequest request = new RequestWrapper(containerRequestContext);
        XSRF anno = ((ExtendedUriInfo)containerRequestContext.getUriInfo())
                .getMatchedResourceMethod()
                .getInvocable()
                .getHandlingMethod()
                .getAnnotation(XSRF.class);
        if (anno == null) {
            anno = ((ExtendedUriInfo)containerRequestContext.getUriInfo())
                    .getMatchedResourceMethod()
                    .getParent()
                    .getClass()
                    .getAnnotation(XSRF.class);
        }
        if (anno != null && anno.value() == false) {
            return false;
        }
        return true;

    }

}

