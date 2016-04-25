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

import io.stallion.Context;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.ValidationException;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.EmailSettings;
import io.stallion.services.Log;
import io.stallion.testing.Stubbing;
import io.stallion.utils.GeneralUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.empty;

/**
 * Helper class for sending an email. Can be used as follows:
 *
 * EmailSender
 *     .newSender()
 *     .setTo("recipient@domain.com")
 *     .setFrom("sender@domain.com")
 *     .setSubject("President's Day Sale!")
 *     .setHtml("...email body email...")
 *     .send();
 *
 * You can create subclasses to implement senders for particular services, such as
 * Amazon Simple Email Service, Sendgrid, etc.
 *
 * The default subclass is SmtpEmailSender() which is used by default throughout Stallion.
 *
 */
public abstract class EmailSender {
    private List<String> tos;
    private String from;
    private String subject;
    private String html;
    private String text;
    private String replyTo;
    private EmailSettings emailSettings;

    /**
     * Create a new instance of the default implementation for this application.
     * TODO: allow the default implementation class to be configured via settings
     *
     * @return
     */
    public static EmailSender newSender() {
        return new SmtpEmailSender();
    }

    public EmailSender() {
        if (Context.settings().getEmail() == null) {
            throw new ConfigException("Email settings section of conf/stallion.toml is empty. You cannot send email.");
        }
        init(Context.settings().getEmail());
    }

    public EmailSender(EmailSettings emailSettings) {
        init(emailSettings);
    }

    protected void init(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
    }

    /**
     * Send the email.
     *
     * @return
     */
    public boolean send() {
        String tosString = String.join(",", tos);
        try {
            doSend();
        } catch (EmailSendException ex) {
            Log.exception(ex, "Error sending email to: " + tosString);
            return false;
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        } catch (ValidationException invalid) {
            throw new ConfigException(invalid);
        }
        return true;
    }

    /**
     * Validate the email settings to make sure that a user, host, password, etc, have been set correctly.
     *
     * @throws ValidationException
     */
    public void validate() throws ValidationException {
        if (Context.settings().getEmail() == null) {
            throw new ValidationException("Email settings section of conf/stallion.toml is empty. You cannot send email.");
        }
        if (empty(emailSettings.getUsername())) {
            throw new ValidationException("No email user name in conf/stallion.toml email settings");
        }
        if (empty(emailSettings.getHost())) {
            throw new ValidationException("No email host in conf/stallion.toml email settings");
        }
        if (empty(emailSettings.getPassword())) {
            throw new ValidationException("No email password in conf/stallion.toml email settings");
        }
    }


    protected void doSend() throws MessagingException, EmailSendException, ValidationException {
        validate();


        Properties props = System.getProperties();
        EmailSettings settings = Context.settings().getEmail();

        String host = settings.getHost();
        props.put("mail.smtp.starttls.enable", settings.getTls().toString().toLowerCase());
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", settings.getUsername());
        props.put("mail.smtp.password", settings.getPassword());
        props.put("mail.smtp.port", settings.getPort().toString());
        props.put("mail.smtp.auth", "true");

        Session session = javax.mail.Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(getFrom()));

        // Format the to addresses and convert into an array of InternetAddress[]
        List<String> tosList = new ArrayList<>();
        for( int i = 0; i < tos.size(); i++ ) {
            tosList.add(restrictToAddress(tos.get(i)));
        }
        InternetAddress[] toAddress = new InternetAddress[tos.size()];
        for( int i = 0; i < tosList.size(); i++ ) {
            toAddress[i] = new InternetAddress(tosList.get(0));
            i++;
        }

        String tosString = String.join(",", tosList);

        for( int i = 0; i < toAddress.length; i++) {
            message.addRecipient(Message.RecipientType.TO, toAddress[i]);
        }

        message.setSubject(getSubject());
        //message.setText(text);
        message.setContent(html, "text/html");
        Log.info("Sending email to {0} with subject ''{1}'' from {2}", tosString, getSubject(), message.getFrom()[0].toString());
        executeSend(message, session, settings);


    }

    /**
     * We want to avoid mistakenly emailing real people when running debug mode.
     * So instead we reroute all emails to an admin address, with a "+" sign for the wildcard.
     * @param emailAddress
     * @return
     */
    private String restrictToAddress(String emailAddress) {
        // Running in prod, not in debug mode
        if (Settings.instance().getDevMode() != true && "prod".equals(Settings.instance().getEnv())) {
            return emailAddress;
        }
        if (Settings.instance().getEmail().getRestrictOutboundEmails() != true) {
            return emailAddress;
        }
        // Empty host, cannot send an email anyways
        if (empty(Settings.instance().getEmail().getHost())) {
            return emailAddress;
        }
        if (Settings.instance().getEmail().getAllowedOutboundEmails().contains(emailAddress))  {
            return emailAddress;
        }
        if (empty(Settings.instance().getEmail().getAllowedTestingOutboundEmailCompiledPatterns()) &&
                (empty(Settings.instance().getEmail().getOutboundEmailTestAddress()) || !Settings.instance().getEmail().getOutboundEmailTestAddress().contains("@"))) {
            throw new ConfigException("You tried to send an email in debug mode with restrictOutboundEmails set to true, but did not define an admin email address nor a outboundEmailTestAddress in your stallion.toml.");
        }
        if (!empty(Settings.instance().getEmail().getAllowedTestingOutboundEmailCompiledPatterns())) {
            for(Pattern pattern: Settings.instance().getEmail().getAllowedTestingOutboundEmailCompiledPatterns()) {
                if (pattern.matcher(emailAddress).matches()) {
                    return emailAddress;
                }
            }
        }
        String[] parts = Settings.instance().getEmail().getOutboundEmailTestAddress().split("@");
        emailAddress = GeneralUtils.slugify(emailAddress).replace("-", ".");
        emailAddress = parts[0] + "+" + emailAddress + "@" + parts[1];
        return emailAddress;
    }


    private void executeSend(MimeMessage message, Session session, EmailSettings settings) throws EmailSendException {
        try {
            Stubbing.checkExecuteStub(this, message, session, settings);
        } catch (Stubbing.StubbedOut stubbedOut) {
            return;
        }
        if (empty(settings.getHost())) {
            throw new ConfigException("No SMTP host configured for sending outbound emails");
        }
        try {
            Transport transport = session.getTransport("smtp");
            transport.connect(settings.getHost(), settings.getUsername(), settings.getPassword());
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException ex) {
            throw new EmailSendException("Error sending email " + message.toString(), ex);
        }
    }


    /**
     * A list of valid email addresses that we are sending to
     *
     * @return
     */
    public List<String> getTos() {
        return tos;
    }

    public EmailSender setTos(List<String> tos) {
        this.tos = tos;
        return this;
    }

    public EmailSender setTo(String ...tos) {
        this.tos = new ArrayList<>();
        for (String to: tos) {
            this.tos.add(to);
        }
        return this;
    }

    /**
     * The email from address
     * @return
     */
    public String getFrom() {
        if (empty(from)) {
            return Settings.instance().getEmail().getAdminEmails().get(0);
        }
        return from;
    }

    public EmailSender setFrom(String from) {
        this.from = from;
        return this;
    }

    /**
     * The HTML for the email body
     * @return
     */
    public String getHtml() {
        return html;
    }

    public EmailSender setHtml(String html) {
        this.html = html;
        return this;
    }

    /**
     * Plain text version of the email body
     *
     * @return
     */
    public String getText() {
        return text;
    }

    public EmailSender setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * The reply to address (optional)
     * @return
     */
    public String getReplyTo() {
        return replyTo;
    }

    public EmailSender setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    /**
     * The email subject
     * @return
     */
    public String getSubject() {
        return subject;
    }

    public EmailSender setSubject(String subject) {
        this.subject = subject;
        return this;
    }




}
