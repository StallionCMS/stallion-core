/*
 * Stallion: A Modern Content Management System
 *
 * Copyright (C) 2015 - 2016 Patrick Fitzsimmons.
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

package io.stallion.monitoring;

import io.stallion.exceptions.ClientException;
import io.stallion.settings.Settings;
import io.stallion.users.IUser;
import io.stallion.users.UserController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class SeleniumEndpoints {

    @Path("/_stx/selenium/get-reset-token")
    @GET
    @Produces("application/json")
    public Map getResetToken(@QueryParam("email") String email, @QueryParam("secret") String secret) {
        if (!Settings.instance().getHealthCheckSecret().equals(secret)) {
            throw new ClientException("Invalid or missing ?secret= query param. Secret must equal the healthCheckSecret in settings.");
        }
        if (!email.startsWith("selenium+resettest+") || !email.endsWith("@stallion.io")) {
            throw new ClientException("Invalid email address. Must be a stallion selenium email.");
        }
        IUser user = UserController.instance().forEmail(email);
        String token = UserController.instance().makeEncryptedToken(user, "reset", user.getResetToken());
        return map(val("resetToken", token));
    }
}
