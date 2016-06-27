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

package io.stallion.tests.integration.users;

import io.stallion.Context;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.MockRequest;
import io.stallion.testing.MockResponse;
import io.stallion.users.UserController;
import io.stallion.users.User;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.Cookie;

import static org.junit.Assert.*;

import static io.stallion.utils.Literals.*;


public class UsersTests extends AppIntegrationCaseBase {


    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/a_minimal_site");

    }


    @Test
    public void testEndpoints() {
        MockResponse response;

        //String hashed = BCrypt.hashpw("a", BCrypt.gensalt());
        //Log.info("Hashed: \"{0}\"", hashed);
        //assertTrue(BCrypt.checkpw("a", hashed));
        Context.getSettings().setDevMode(true);

        response = client.get("/st-users/login");
        Log.finer("login html: {0}", response.getContent());
        assertEquals(200, response.getStatus());

        response = client.post("/st-users/submit-login",
                map(
                        val("username", "unittests@stallion.io"),
                        val("rememberMe", false),
                        val("password", "invalid")));
        assertEquals(403, response.getStatus());

        response = client.post("/st-users/submit-login",
                map(
                        val("username", "unittests@stallion.io"),
                        val("rememberMe", true),
                        val("password", "unitstallioncaseword")));
        assertEquals(200, response.getStatus());

        MockRequest req = new MockRequest("/st-users/current-user-info", "GET");
        String userCookie = response.getCookie(UserController.USER_COOKIE_NAME).getValue();
        Log.info("User cookie {0}", userCookie);
        req.setCookies(new Cookie(UserController.USER_COOKIE_NAME, userCookie));
        response = client.request(req);
        assertEquals(200, response.getStatus());
        Log.finer("Response: {0}", response.getContent());
        User user = (User)response.asObject(User.class);
        assertEquals("unittests@stallion.io", user.getEmail());
        assertEquals("", user.getSecret());



    }

}
