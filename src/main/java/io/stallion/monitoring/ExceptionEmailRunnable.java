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

package io.stallion.monitoring;

import io.stallion.email.ContactableEmailer;
import io.stallion.users.User;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;

import static io.stallion.Context.getSettings;
import static io.stallion.Context.settings;
import static io.stallion.utils.Literals.utcNow;


public class ExceptionEmailRunnable implements Runnable {
    private ExceptionInfo exceptionInfo;

    public ExceptionEmailRunnable(ExceptionInfo info) {
        this.exceptionInfo = info;
    }

    @Override
    public void run() {
        for (String email: settings().getEmail().getAdminEmails()) {
            User user = new User().setEmail(email);
            ExceptionEmailer emailer = new ExceptionEmailer(user, exceptionInfo);
            emailer.sendEmail();
        }
    }

    public static class ExceptionEmailer extends ContactableEmailer<User> {

        public ExceptionEmailer(User user, ExceptionInfo ex) {
            super(user);
            put("siteUrl", getSettings().getSiteUrl());
            put("siteName", getSettings().getSiteName());
            put("exceptionInfo", ex);
        }

        @Override
        public String getUniqueKey() {
            ExceptionInfo ex = (ExceptionInfo)getContext().get("exceptionInfo");
            String hash = GeneralUtils.md5Hash(StringUtils.join(ex.getStackTraces(), "\n"));
            String minute = utcNow().format(DateUtils.MINUTE_FORMAT);
            return "exception-" + hash + "-" + GeneralUtils.slugify(minute);
        }

        @Override
        public boolean shouldLog() {
            return false;
        }

        @Override
        public boolean isTransactional() {
            return true;
        }

        @Override
        public String getTemplate() {
            return getClass().getResource("/templates/exception-email.jinja").toString();
        }

        @Override
        public String getSubject() {
            return "[ERROR] {exceptionInfo.className} on {exceptionInfo.requestUrl}";
        }
    }
}
