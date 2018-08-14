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

package io.stallion.contentPublishing.forms;

import io.stallion.email.ContactableEmailer;
import io.stallion.users.User;


public class SimpleFormSubmissionEmailer extends ContactableEmailer {
    private SimpleFormSubmission submission;

    public SimpleFormSubmissionEmailer(User user, SimpleFormSubmission submission) {
        super(user);
        this.submission = submission;
        put("submission", submission);
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public String getTemplate() {
        return getClass().getResource("/templates/form-submission-notify.jinja").toString();
    }

    @Override
    public String getSubject() {
        return "New form submission: " + submission.getEmail() + " on page " + submission.getPageTitle();
    }

}


