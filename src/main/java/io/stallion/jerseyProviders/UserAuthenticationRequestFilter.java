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

import io.stallion.Context;
import io.stallion.requests.RequestWrapper;
import io.stallion.settings.Settings;
import io.stallion.users.OAuthApprovalController;
import io.stallion.users.UserController;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(1000)
@PreMatching
public class UserAuthenticationRequestFilter implements ContainerRequestFilter {




    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        //containerRequestContext.getRequest();
        // Authorize via cookie?
        if (UserController.instance() != null) {
            UserController.instance().checkCookieAndAuthorizeForRequest(new RequestWrapper(containerRequestContext));
        }
        // Authorize via an OAuth bearer token?
        if (!Context.getUser().isAuthorized() && Settings.instance().getoAuth().getEnabled()) {
            OAuthApprovalController.instance().checkHeaderAndAuthorizeUserForRequest(new RequestWrapper(containerRequestContext));
        }
    }
}
