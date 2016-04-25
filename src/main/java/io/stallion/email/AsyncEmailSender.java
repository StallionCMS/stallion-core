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

import io.stallion.asyncTasks.AsyncCoordinator;
import io.stallion.asyncTasks.AsyncTaskHandlerBase;

/**
 * Basic task handler for sending an email asynchronously, given an email address,
 * template, and subject.
 */
public class AsyncEmailSender extends AsyncTaskHandlerBase {
    private String emailAddress = "";
    private String template = "";
    private String subject = "";

    public AsyncEmailSender() {

    }

    public AsyncEmailSender(String email, String template, String subject) {
        this.emailAddress = email;
        this.template = template;
        this.subject = subject;
    }

    public void process() {
        AddressEmailer emailer = new AddressEmailer(emailAddress, template, subject);
        emailer.sendEmail();
    }

    public void submit() {
        AsyncCoordinator.instance().enqueue(this);
    }


    public String getEmailAddress() {
        return emailAddress;
    }

    public AsyncEmailSender setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public String getTemplatePath() {
        return template;
    }

    public AsyncEmailSender setTemplatePath(String template) {
        this.template = template;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public AsyncEmailSender setSubject(String subject) {
        this.subject = subject;
        return this;
    }
}
