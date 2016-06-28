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

package io.stallion.forms;

import io.stallion.asyncTasks.AsyncCoordinator;
import io.stallion.asyncTasks.AsyncTaskHandlerBase;
import io.stallion.dataAccess.Model;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.User;
import io.stallion.users.UserController;

import static io.stallion.utils.Literals.*;


public class SimpleFormSubmissionEmailTask extends AsyncTaskHandlerBase {
    private Long submissionId;

    public static void enqueue(SimpleFormSubmission submission) {
        SimpleFormSubmissionEmailTask handler = new SimpleFormSubmissionEmailTask();
        handler.setSubmissionId(submission.getId());
        AsyncCoordinator.instance().enqueue(handler, "new-simple-submission-email-" + submission.getId(), 0);
    }


    public void process() {
        SimpleFormSubmission submission = SimpleFormSubmissionController.instance().forId(submissionId);
        if (empty(submission.getEmail())) {
            return;
        }
        Log.info("Mail submission to to moderators commentId={0} moderators={1}", submission.getId(), Settings.instance().getEmail().getAdminEmails());
        for(String email: Settings.instance().getEmail().getAdminEmails()) {
            Model m = null;
            if (UserController.instance()!= null) {
                m = UserController.instance().forUniqueKey("email", email);
            }
            User user;
            if (m == null) {
                user = new User().setEmail(email);
            } else {
                user = (User)m;
            }
            SimpleFormSubmissionEmailer emailer = new SimpleFormSubmissionEmailer(user, submission);
            Log.info("Send moderation email. submission={0} moderator={0}", submission.getId(), user.getEmail());
            emailer.sendEmail();
        }
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }
}


