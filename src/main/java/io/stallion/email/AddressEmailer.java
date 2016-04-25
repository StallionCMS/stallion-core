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

package io.stallion.email;

import io.stallion.users.User;

import java.net.MalformedURLException;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * Simple implementation of ContactableEmailer that can send an email
 * jus using an email address, template, and email subject.
 */
public class AddressEmailer extends ContactableEmailer {
    private String template = "";
    private String subject = "";

    public AddressEmailer(String emailAddress, String template, String subject) {
        this(emailAddress, template, subject, map());

    }

    /**
     *
     * @param emailAddress
     * @param template - path to a jinja template
     * @param subject
     * @param context - gets passed to the template context
     */
    public AddressEmailer(String emailAddress, String template, String subject, Map<String, Object> context) {
        super(new User()
                .setDisplayName(emailAddress)
                .setEmail(emailAddress)
                .setUsername(emailAddress), context);
        this.template = template;
        this.subject = subject;
    }


    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public String getSubject() {
        return subject;
    }
}
