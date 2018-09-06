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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

import static io.stallion.utils.Literals.*;

import io.stallion.exceptions.UsageException;
import io.stallion.requests.IRequest;
import io.stallion.requests.RequestWrapper;
import io.stallion.services.Log;
import io.stallion.settings.SecondaryDomain;
import io.stallion.settings.Settings;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(FilterPriorities.REDIRECT_REQUEST_FILTER)
public class RedirectResponseFilter implements ContainerRequestFilter {

    @javax.ws.rs.core.Context
    private HttpServletRequest httpRequest;

    @javax.ws.rs.core.Context
    private HttpServletResponse httpResponse;



    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        IRequest request = new RequestWrapper(containerRequestContext);
        String path = containerRequestContext.getUriInfo().getRequestUri().getPath();
        for(Map.Entry<String, String> redirect: Settings.instance().getRedirects().entrySet()) {
            if (redirect.getKey().equals(path)) {
                if (path.equals(redirect.getValue())){
                    throw new UsageException("Configured redirect creates an infinite loop:" + path);
                }
                throw new RedirectionException(301, URI.create(redirect.getValue()));
            }
        }

    }


}

