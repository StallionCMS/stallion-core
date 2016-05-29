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

import io.stallion.dataAccess.ModelBase;

import javax.persistence.Column;
import java.time.ZonedDateTime;


public class EmailLog extends ModelBase {
    private String toEmails;
    private String fromEmail;
    private String from;
    private String to;
    private String subject;
    private String bodySummary;
    private String replyTo;
    private String type;
    private String uniqueKey;
    private ZonedDateTime createdAt;

    @Column
    public String getToEmails() {
        return toEmails;
    }

    public EmailLog setToEmails(String toEmails) {
        this.toEmails = toEmails;
        return this;
    }

    @Column
    public String getFromEmail() {
        return fromEmail;
    }

    public EmailLog setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
        return this;
    }

    @Column
    public String getFrom() {
        return from;
    }

    public EmailLog setFrom(String from) {
        this.from = from;
        return this;
    }

    @Column
    public String getTo() {
        return to;
    }

    public EmailLog setTo(String to) {
        this.to = to;
        return this;
    }

    @Column
    public String getSubject() {
        return subject;
    }

    public EmailLog setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    @Column
    public String getBodySummary() {
        return bodySummary;
    }

    public EmailLog setBodySummary(String bodySummary) {
        this.bodySummary = bodySummary;
        return this;
    }

    @Column
    public String getReplyTo() {
        return replyTo;
    }

    public EmailLog setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    @Column
    public String getType() {
        return type;
    }

    public EmailLog setType(String type) {
        this.type = type;
        return this;
    }

    @Column
    public String getUniqueKey() {
        return uniqueKey;
    }

    public EmailLog setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
        return this;
    }

    @Column
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public EmailLog setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
