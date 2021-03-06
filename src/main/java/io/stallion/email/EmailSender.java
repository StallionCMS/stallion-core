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

package io.stallion.email;

import io.stallion.Context;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.ValidationException;
import io.stallion.services.Log;
import io.stallion.services.TransactionLog;
import io.stallion.services.TransactionLogController;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.EmailSettings;
import io.stallion.testing.SelfMocking;
import io.stallion.testing.StubHandler;
import io.stallion.testing.Stubbing;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;

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
public abstract class EmailSender implements Runnable, SelfMocking {
    private List<String> tos;
    private String from;
    private String subject;
    private String html;
    private String text;
    private String replyTo;
    private EmailSettings emailSettings;
    private String customKey = "";
    private boolean shouldLog = true;
    private String type = "";

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

    @Override
    public void run() {
        send();
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
            if (shouldLog) {
                logEmail();
            }
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

    protected void logEmail() {
        Map<String, Object> extra = map();
        TransactionLog log = new TransactionLog()
                .setBody(or(getHtml(), getText()))
                .setSubject(getSubject())
                .setCustomKey(customKey)
                .setUserId(Context.getUser().getId())
                .setOrgId(Context.getUser().getOrgId())
                .setToAddress(String.join(",", tos))
                .setCustomKey(customKey)
                .setType(type)
                .setCreatedAt(DateUtils.utcNow())
                ;
        extra.put("fromAddress", getFrom());
        extra.put("replyTo", getReplyTo());
        TransactionLogController.instance().save(log);




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
        // .test domains are never real, live domains, so we never send a real email to them
        if (emailAddress.endsWith(".test")) {
            return emailAddress;
        }
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
            Stubbing.checkExecuteStub(this, this, message, session, settings);
        } catch (Stubbing.StubbedOut stubbedOut) {
            return;
        }
        if (empty(settings.getHost())) {
            throw new ConfigException("No SMTP host configured for sending outbound emails");
        }

        boolean hasNonTest = false;

        try {
            for (Address a:message.getAllRecipients()) {
                if (!a.toString().endsWith(".test")) {
                    hasNonTest = true;
                }
            }
            if (!hasNonTest) {
                Log.warn("All email addresses in this message are going to invalid test domains, so skipping actual send: {0}", message.getAllRecipients());
                return;
            }

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


    public String getCustomKey() {
        return customKey;
    }

    public EmailSender setCustomKey(String customKey) {
        this.customKey = customKey;
        return this;
    }

    public boolean isShouldLog() {
        return shouldLog;
    }

    public EmailSender setShouldLog(boolean shouldLog) {
        this.shouldLog = shouldLog;
        return this;
    }

    public String getType() {
        return type;
    }

    public EmailSender setType(String type) {
        this.type = type;
        return this;
    }



    /* SelfMocking support. This allows us to call this.mockClass(EmailSender.class) from tests and automatically
     * stub out the the final email sending step to make the tests go extra fast.
     * */

    private EmailStubHandler emailStubHandler;

    @Override
    public void onSelfMockingBeforeClass() {
        emailStubHandler = new EmailStubHandler();
        Stubbing.stub(EmailSender.class, "executeSend", emailStubHandler);
    }


    @Override
    public List<MimeMessage> onSelfMockingGetResults() {
        return emailStubHandler.messages;
    }

    public static class EmailStubHandler implements StubHandler {
        private List<MimeMessage> messages = list();

        @Override
        public Object execute(Object... params) throws Exception {
            MimeMessage message = (MimeMessage)params[1];
            messages.add(message);
            return true;
        }
    }

    /* end self mocking support */
}
