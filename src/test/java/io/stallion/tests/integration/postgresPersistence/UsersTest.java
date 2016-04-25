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

package io.stallion.tests.integration.postgresPersistence;

import io.stallion.services.Log;
import io.stallion.testing.TestClient;
import io.stallion.utils.Encrypter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.crypto.keygen.KeyGenerators;


public class UsersTest {
    private static TestClient client;


    @BeforeClass
    public static void setUpClass() throws Exception {

    }


    @Test
    public void testEncryption() throws Exception {
        String key = "qkJ92QHx5xLfqtnN";
        String org = "This is an original string with a snowman: ☃";
        String encrypted = Encrypter.encryptString(key, org);
        Assert.assertNotEquals(org, encrypted);
        String back = Encrypter.decryptString(key, encrypted);
        Assert.assertEquals(org, back);
    }

    @Test
    public void testUsers() throws Exception {
        /*
        String name = "frank" + new DateTime().getMillis();
        String email = "frank" + new DateTime().getMillis() + "@some.stallion.io";
        User user = (User) new User()
                .setGivenName(name)
                .setEmail(email)
                .setUsername(email);
        UserController.instance().createUser(user);
        Assert.assertTrue(((Long) user.getId()) > 1000);
        User userRetrieved = (User) UserController.instance().getUserById(user.getId());
        Assert.assertEquals(name, userRetrieved.getGivenName());
        Assert.assertEquals(user.getId(), userRetrieved.getId());

        String cookie = DbSession.userToCookie(user, null);
        IUser fromCookie = DbSession.cookieToUser(cookie);
        Assert.assertEquals(email, fromCookie.getEmail());
        Assert.assertEquals(user.getId(), fromCookie.getId());
        */
    }



}