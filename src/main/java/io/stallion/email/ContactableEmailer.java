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
import io.stallion.exceptions.UsageException;
import io.stallion.plugins.javascript.Sandbox;
import io.stallion.services.LocalMemoryCache;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.utils.DateUtils;
import io.stallion.utils.SimpleTemplate;
import io.stallion.utils.GeneralUtils;

import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * This class a helper class for defining a way to send emails to a
 * Contactable (usually a User or a Contact). You create a subclass,
 * override getTemplate() and getSubject() to define the template and
 * subject to use, override any additional handlers, and then you can send
 * an email via:
 *
 * new MyEmailer(user, myContextVars).sendEmail()
 *
 * ContactableEmailer does a lot of extra work for you, such as adding
 * in extra variables, excluding optedout users, preventing duplicates,
 * making it easy to use templated subject lines, etc, etc.
 *
 * @param <T>
 */
public abstract class ContactableEmailer<T extends Contactable> {

    protected T user;
    protected String template;
    protected URL templateUrl;
    protected String weekStamp;
    protected String hourStamp;
    protected String dayStamp;
    protected Sandbox sandbox;
    protected String minuteStamp;



    private Map<String, Object> context = map();

    public ContactableEmailer(T user) {
        this.user = user;
    }

    public ContactableEmailer(T user, Map<String, Object> context) {
        this.context.putAll(context);
        this.user = user;
    }

    {
        ZonedDateTime now = DateUtils.utcNow();
        weekStamp = now.format(DateTimeFormatter.ofPattern("YYYY-w"));
        dayStamp = now.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));
        minuteStamp = now.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HHmm"));
        hourStamp = now.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH"));
        context.put("weekStamp", weekStamp);
        context.put("dayStamp", dayStamp);
        context.put("minuteStamp", minuteStamp);
        context.put("hourStamp", hourStamp);
        context.put("qaPrefix", "");
        context.put("envPrefix", "");
        context.put("env", settings().getEnv());
        if (!"prod".equals(settings().getEnv())) {
            context.put("qaPrefix", "qa");
            context.put("envPrefix", settings().getEnv());
        }
    }



    public boolean sendEmail() {
        if (user == null) {
            throw new UsageException("Tried to send email, but user is null!");
        }

        if (empty(user.getEmail())) {
            throw new UsageException("Tried to send email, but email address was empty! id=" + user.getId());
        }
        prepareContext();
        onPrepareContext();

        if (checkDefaultOptOut()) {
            Log.info("User {0} id:{1} has opted out of emails via checkDefaultOptOut()", user.getEmail(), user.getId());
            return false;
        }
        if (checkOptOut()) {
            Log.info("User {0} id:{1} has opted out of emails via checkOptOut().", user.getEmail(), user.getId());
            return false;
        }
        if (hasSeenKey()) {
            Log.warn("You already have sent an email recently with the unique key {0}", transformMaybe(getUniqueKey()));
            return false;
        }
        markSeenKey();

        String templatePath = null;

        templatePath = getTemplate();


        String html;
        if (getSandbox() != null) {
            html = TemplateRenderer.instance().renderSandboxedTemplate(getSandbox(), templatePath, context);
        } else {
            html = TemplateRenderer.instance().renderTemplate(templatePath, context);
        }
        EmailSender emailer = EmailSender.newSender();
        emailer
                .setFrom(transformMaybe(getFromAddress()))
                .setHtml(html)
                .setReplyTo(transformMaybe(getReplyTo()))
                .setSubject(transformMaybe(getSubject()))
                .setShouldLog(shouldLog())
                .setCustomKey(transformMaybe(getUniqueKey()))
                .setTo(user.getEmail());
        onPreSend();
        return emailer.send();
    }

    public boolean shouldLog() {
        return true;
    }

    /**
     * Apply default heuristics for opting out the user from email.
     *
     * @return
     */
    public boolean checkDefaultOptOut() {
        if (user.isOptedOut() && !isTransactional()) {
            return true;
        }
        if (user.isTotallyOptedOut()) {
            return true;
        }
        if (user.isDisabled()) {
            return true;
        }
        if (user.getDeleted()) {
            return true;
        }
        return false;
    }

    /**
     * Return true if this is an email that opt-out should not apply to.
     * For example, return true for: password reset emails, security alerts, transaction reciepts.
     *
     * return false for all other emails that need an unsubscribe link, per CAN-SPAM laws.
     *
     * @return
     */
    public abstract boolean isTransactional();

    /**
     * Override this to check for additional conditions where the user might be
     * opted out.
     *
     * @return
     */
    public boolean checkOptOut() {
        return false;
    }

    /**
     * If not null, will render the email template with the sandbox, thus limiting
     * access in the template context to site-wide data.
     * @return
     */
    public Sandbox getSandbox() {
        return sandbox;
    }

    public <E extends ContactableEmailer> E setSandbox(Sandbox box) {
        this.sandbox = box;
        return (E)this;
    }

    /**
     * Override this to do any additional actions just before the email is finally sent out.
     * This may be useful for logging the message to a database.
     */
    public void onPreSend() {

    }

    private String transformMaybe(String s) {
        if (s.contains("{") && s.contains("}")) {
            return new SimpleTemplate(s, context).render();
        } else {
            return s;
        }
    }

    protected void prepareContext() {
        context.put("user", user);
        context.put("contact", user);
        context.put("subjectSlug", GeneralUtils.slugify(getSubject()));
        context.put("baseUrl", Context.getSettings().getSiteUrl());
        context.put("emailType", getEmailType());
        context.put("emailer", this);
    }

    /**
     * Override this to prepare additional context variables.
     */
    protected void onPrepareContext() {

    }

    protected void updateContext(Map<String, ? extends Object> context) {
        this.context.putAll(context);
    }

    /**
     * Add additional data to the template context
     * @param key
     * @param val
     */
    public <Y extends ContactableEmailer> Y put(String key, Object val) {
        this.context.put(key, val);
        return (Y)this;
    }
    /**
     * The type of the email, used for logging, defaults to using the
     * Java class name, can be overrident
     * @return
     */
    public String getEmailType() {
        return this.getClass().getCanonicalName();
    }

    /**
     * Checks to see if the unique key for this email has been seen.
     *
     * @return
     */
    protected boolean hasSeenKey() {
        if (!empty(getUniqueKey())) {
            Object seen = LocalMemoryCache.get("contactEmailerKeys", transformMaybe(getUniqueKey()));
            if (seen instanceof Boolean && true == (boolean)seen) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mark the unique key as being seen.
     */
    protected void markSeenKey() {
        if (!empty(getUniqueKey())) {
            LocalMemoryCache.set("contactEmailerKeys", transformMaybe(getUniqueKey()), true, 100000);
        }
    }

    /**
     * The path to the jinja template. If the template is in a resource file,
     * should be "pluginName:/my-template.jinja" where my-template.jinja is in the
     * templates folder in your resources directory in your java project. If it is a
     * built-in stallion template, use "stallion:/my-template.jinja"
     *
     * @return
     */
    public abstract String getTemplate();

    /**
     * Your email subject. This is interpreted as a SimpleTemplate -- so if you do
     * "Hello, {{ contact.firstName }}" or "Hello, {{ myVar }}" then it will look-up
     * the variable from the context and interpolate it.
     *
     * @return
     */
    public abstract String getSubject();

    /**
     * Who is sending the email. Defaults to email.defaultFromAddress in Stallion settings,
     * or the first admin email if that does not exist, or the email user and host, if that
     * doesn't exist.
     *
     * @return
     */
    public String getFromAddress() {
        if (emptyInstance(Context.getSettings().getEmail())) {
            throw new ConfigException("Email settings are null, and no override for the from email address was set.");
        }
        if (!emptyInstance(Context.getSettings().getEmail().getDefaultFromAddress())) {
            return Context.getSettings().getEmail().getDefaultFromAddress();
        }
        if (Settings.instance().getEmail().getAdminEmails().size() > 0) {
            return Settings.instance().getEmail().getAdminEmails().get(0);
        }

        String host = Context.getSettings().getEmail().getHost();
        String email = Context.getSettings().getEmail().getUsername();
        if (!email.contains("@")) {
            return email + "@" + host;
        } else {
            return email;
        }
    }

    /**
     * Override this to set a custom-reply to address.
     * @return
     */
    public String getReplyTo() {
        return "";
    }

    /**
     * Override this to set a custom list of CC addresses
     * @return
     */
    public String getCc() {
        return "";
    }

    /**
     * This is used to prevent accidentally sending duplicate emails, by default the
     * uniqueKey is built from the subject, the contact id, the current week, and the email type.
     *
     * @return
     */
    public String getUniqueKey() {
        return truncate(GeneralUtils.slugify(getSubject()), 150) + "-" + user.getEmail() + "-" + weekStamp + getEmailType();
    }


    protected Map<String, Object> getContext() {
        return context;
    }
}
