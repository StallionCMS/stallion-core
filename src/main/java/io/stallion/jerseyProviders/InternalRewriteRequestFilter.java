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

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;


@Priority(700)
@PreMatching
@Provider
public class InternalRewriteRequestFilter  implements ContainerRequestFilter {

    @javax.ws.rs.core.Context
    private HttpServletRequest httpRequest;

    @javax.ws.rs.core.Context
    private HttpServletResponse httpResponse;



    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        // redirect conditionally
        //if (shouldRedirect(reqContext)) {
        //    reqContext.setRequestUri(URI.create("/temp"));
        //}
    }
}
