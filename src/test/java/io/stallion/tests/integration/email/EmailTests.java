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

package io.stallion.tests.integration.email;


import io.stallion.email.EmailSender;
import io.stallion.email.ContactableEmailer;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.StubHandler;
import io.stallion.testing.Stubbing;
import io.stallion.users.User;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.mail.internet.MimeMessage;

public class EmailTests extends AppIntegrationCaseBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/a_minimal_site");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        cleanUpClass();
    }

    @Test
    public void testUserEmail() {
        User user = new User()
                .setEmail("testing1@stallion.io")
                .setDisplayName("Stallion Person")
                ;

        Stubbing.stub(EmailSender.class, "executeSend", new StubHandler() {
            @Override
            public Object execute(Object... params) throws Exception {
                MimeMessage message = (MimeMessage)params[1];
                assertEquals(user.getEmail(), message.getAllRecipients()[0].toString());
                assertTrue(message.getContent().toString().contains("displayName=" + user.getDisplayName()));
                assertEquals("Hello, " + user.getDisplayName(), message.getSubject());
                return null;
            }
        });
        MyUserEmailer emailer = new MyUserEmailer(user);

        boolean sent = emailer.sendEmail();
        assertEquals(true, sent);
        Stubbing.verifyAndReset();
    }


    public static class MyUserEmailer extends ContactableEmailer {

        public MyUserEmailer(User user) {
            super(user);
        }

        @Override
        public boolean isTransactional() {
            return true;
        }

        @Override
        public String getTemplate() {
            return "my-email.jinja";
        }

        @Override
        public String getSubject() {
            return "Hello, {user.displayName}";
        }

        @Override
        public String getFromAddress() {
            return "no-reply@mysite.com";
        }
    }



}
