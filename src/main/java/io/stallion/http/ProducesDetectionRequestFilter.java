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
import org.apache.commons.lang3.ArrayUtils;
import org.glassfish.jersey.server.ExtendedUriInfo;

import javax.annotation.Priority;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

@Provider
@Priority(FilterPriorities.PRODUCES_DETECTION_FILTER)
public class ProducesDetectionRequestFilter  implements ContainerRequestFilter {

    public static final String PRODUCES_PROPERTY_NAME = "_ENDPOINT_PRODUCES_GUESS";
    public static final String GUESS_IS_JSON_PROPERTY_NAME = "_GUESS_IS_JSON";




    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        RequestWrapper request = new RequestWrapper(containerRequestContext);
        ExtendedUriInfo uriInfo = ((ExtendedUriInfo)containerRequestContext.getUriInfo());
        Method method = uriInfo
                .getMatchedResourceMethod()
                .getInvocable()
                .getHandlingMethod();
        Produces produces = method.getAnnotation(Produces.class);
        if (produces == null) {
            produces = uriInfo
                    .getMatchedResourceMethod()
                    .getParent()
                    .getClass()
                    .getAnnotation(Produces.class);
        }
        String producesContentType = "text/html";
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            producesContentType = "application/json";
        }

        if (produces != null) {
            if (ArrayUtils.contains(produces.value(), ("application/json"))) {
                producesContentType = "application/json";
            } else if (ArrayUtils.contains(produces.value(), "text/html")) {
                producesContentType = "text/html";
            } else if (produces.value().length > 0) {
                producesContentType = produces.value()[0];
            }
        }
        containerRequestContext.setProperty(PRODUCES_PROPERTY_NAME, producesContentType);
        containerRequestContext.setProperty(GUESS_IS_JSON_PROPERTY_NAME, "application/json".equals(producesContentType));

    }
}
