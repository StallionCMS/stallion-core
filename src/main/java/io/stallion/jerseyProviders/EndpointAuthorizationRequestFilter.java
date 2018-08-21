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

package io.stallion.jerseyProviders;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;

import io.stallion.Context;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.IRequest;
import io.stallion.requests.RequestWrapper;
import io.stallion.settings.Settings;
import io.stallion.users.Role;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.MethodList;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(FilterPriorities.ENDPOINT_AUTHORIZATION_FILTER)
public class EndpointAuthorizationRequestFilter  implements ContainerRequestFilter {

    @javax.ws.rs.core.Context
    private HttpServletRequest httpRequest;

    @javax.ws.rs.core.Context
    private HttpServletResponse httpResponse;



    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        IRequest request = new RequestWrapper(containerRequestContext);

        ExtendedUriInfo uriInfo = ((ExtendedUriInfo)containerRequestContext.getUriInfo());



        MinRole mr = uriInfo
                .getMatchedResourceMethod()
                .getInvocable()
                .getHandlingMethod()
                .getAnnotation(MinRole.class);

        if (mr == null) {
            mr = uriInfo
                    .getMatchedResourceMethod()
                    .getParent()
                    .getClass()
                    .getAnnotation(MinRole.class)
            ;

        }
        Role minRole = Settings.instance().getUsers().getDefaultEndpointRoleObj();
        if (mr != null) {
            minRole = mr.value();
        }
        if (Context.getUser().isInRole(minRole)) {
            return;
        }


        if ("text/html".equals(containerRequestContext.getProperty(ProducesDetectionRequestFilter.PRODUCES_PROPERTY_NAME))) {
            throw new RedirectionException(302, URI.create(Settings.instance().getUsers().getLoginPage() +
                    "?stReturnUrl=" + URLEncoder.encode(request.requestUrl(), "utf-8")));
        } else {
            throw new ClientErrorException("You are not authorized to access this resource.", 403);
        }
    }
}

