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

package io.stallion.forms;

import io.stallion.exceptions.ClientException;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.ObjectParam;
import io.stallion.services.LocalMemoryCache;
import io.stallion.utils.Encrypter;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class SimpleFormEndpoints {

    @POST
    @Path("/contacts/submit-form")
    public Boolean submitForm(@ObjectParam(targetClass = SimpleFormSubmission.class) SimpleFormSubmission rawSubmission) {
        SimpleFormSubmission submission = SafeMerger
                .with()
                .nonEmpty("antiSpamToken", "pageUrl", "data")
                .optionalEmail("email")
                .optional("pageTitle", "formId")
                .merge(rawSubmission);

        /* The Anti-spam token is an encrypted token with a milliseconds timestamp and a randomly generated key
           This prevents a spammer from simply hitting this endpoint over and over again with a script. A given
           random key can only be used once within an hour, and all tokens expire after an hour, so all together
           it is not possible to submit more than once.

           This will not stop a spammer who is actually requesting a new copy of the page and who is
           parsing out the spam token single every time. We would have to implement IP address throttling or
           captchas to fix that.
         */

        String token = Encrypter.decryptString(settings().getAntiSpamSecret(), submission.getAntiSpamToken());
        if (empty(token) || !token.contains("|")) {
            throw new ClientException("Anti-spam token is not in the correct format");
        }
        String[] parts = StringUtils.split(token, "|", 2);
        Long time = Long.parseLong(parts[0]);
        String randomKey = parts[1];

        if (time == null || ((time + 60 * 60 * 1000) < mils())) {
            throw new ClientException("Anti-spam token has expired. Please reload the page and submit again.");
        }

        Integer submissionCount = or((Integer)LocalMemoryCache.get("form_submissions", randomKey), 0);
        if (submissionCount > 0) {
                throw new ClientException("You have already submitted this form once.");
        }


        submission
                .setSubmittedAt(mils());
        SimpleFormSubmissionController.instance().save(submission);
        if (!empty(submission.getEmail())) {
            SimpleFormSubmissionEmailTask.enqueue(submission);
        }

        // Store a record of this token, so it cannot be reused
        LocalMemoryCache.set("form_submissions", randomKey, submissionCount + 1, 90 * 60 * 1000);

        return true;
    }
}
