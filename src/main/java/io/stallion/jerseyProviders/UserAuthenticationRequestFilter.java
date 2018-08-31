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

import io.stallion.Context;
import io.stallion.exceptions.UsageException;
import io.stallion.requests.RequestWrapper;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.OAuthApprovalController;
import io.stallion.users.User;
import io.stallion.users.UserController;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static io.stallion.utils.Literals.empty;

@Provider
@Priority(FilterPriorities.USER_AUTHENTICATION_FILTER)
@PreMatching
public class UserAuthenticationRequestFilter implements ContainerRequestFilter {


    @javax.ws.rs.core.Context
    private ClientRequestContext clientRequestContext;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        if (!"prod".equals(Settings.instance().getEnv())) {
            String username = (String)containerRequestContext.getProperty("testUsername");
            if (!empty(username)) {
                IUser user = UserController.instance().forUsername(username);
                if (user == null) {
                    throw new UsageException("Could not find test user " + username);
                }
                Context.setUser(user);
            } else {
                Long userId = (Long) containerRequestContext.getProperty("testUserId");
                if (!empty(userId)) {
                    IUser user = UserController.instance().forId(userId);
                    if (user == null) {
                        throw new UsageException("Could not find test user for id " + userId);
                    }
                    Context.setUser(user);
                }
            }
        }

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
