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

package io.stallion.tests.integration.filePersistenceSite;

import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.users.IUser;
import io.stallion.users.User;
import io.stallion.users.UserController;
import org.junit.Assert;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.nio.file.Paths;


public class UsersTests extends AppIntegrationCaseBase {


    @BeforeClass
    public static void setUpClass() throws Exception {
        cleanup();
        startApp("/file_persistence_site");
    }

    @After
    public void tearDown() throws Exception {
        cleanup();
    }

    public static void cleanup() throws Exception {
        URL resourceUrl = AppIntegrationCaseBase.class.
                getResource("/file_persistence_site/dossiers/jesse-james.json");
        if (resourceUrl != null) {
            Paths.get(resourceUrl.toURI()).toFile().delete();
        }
        resourceUrl = AppIntegrationCaseBase.class.
                getResource("/file_persistence_site/crimes/carmen-sandiego.json");
        if (resourceUrl != null) {
            Paths.get(resourceUrl.toURI()).toFile().delete();
        }
        resourceUrl = AppIntegrationCaseBase.class.
                getResource("/file_persistence_site/bios/lincoln.txt");
        if (resourceUrl != null) {
            Paths.get(resourceUrl.toURI()).toFile().delete();
        }
        resourceUrl = AppIntegrationCaseBase.class.
                getResource("/file_persistence_site/app-data/users");
        if (resourceUrl != null) {
            Paths.get(resourceUrl.toURI()).toFile().delete();
        }

    }

    @Test
    public void testUsers() throws Exception {
        //FileUserController.load();
        IUser user = new User();
        user.setEmail("person@somewhere.com");
        user.setBcryptedPassword(BCrypt.hashpw("foxywords", BCrypt.gensalt()));
        user.setUsername(user.getEmail());
        user.setFamilyName("Von Colt");
        user.setGivenName("Wolfgang");
        UserController.instance().save(user);

        User existing = (User) UserController.instance().forId(user.getId());
        Assert.assertEquals(existing.getEmail(), user.getEmail());

        existing = (User) UserController.instance().forUniqueKey("email", user.getEmail());
        Assert.assertEquals(user.getGivenName(), existing.getGivenName());


    }
}