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

package io.stallion.users;

import com.fasterxml.jackson.annotation.JsonView;
import io.stallion.Context;
import io.stallion.assets.*;
import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.dataAccess.filtering.Pager;
import io.stallion.exceptions.*;
import io.stallion.requests.validators.SafeMerger;
import io.stallion.restfulEndpoints.*;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.utils.Sanitize;
import io.stallion.utils.json.RestrictedViews;

import javax.ws.rs.*;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

@Path("/st-users")
public class UsersApiResource implements EndpointResource {

    public static void register() {
        if (Settings.instance().getUsers().getEnableDefaultEndpoints()) {
            EndpointsRegistry.instance().addResource("", new UsersApiResource());

            /*
            DefinedBundle.register(new DefinedBundle(
                    "userAdminStylesheets", ".css",

                    new BundleFile().setPluginName("stallion").setLiveUrl("admin/admin.css"),
                    new BundleFile().setPluginName("stallion").setLiveUrl("admin/users-manage.css")
            ));
            DefinedBundle.register(new DefinedBundle(
                    "userAdminJavascripts", ".js",
                    new BundleFile().setPluginName("stallion").setLiveUrl("admin/users-manage.js"),
                    new BundleFile().setPluginName("stallion").setLiveUrl("admin/users-table-riot.tag").setProcessor("riot")
            ));


            BundleRegistry.instance().register(
                    new CompiledBundle("user-admin-vue",
                            new ResourceBundleFile("stallion", "admin/admin.css"),
                            new ResourceBundleFile("stallion", "admin/users-manage.css"),
                            new ResourceBundleFile("stallion", "vendor/vue.min.js", "vendor/vue.js"),
                            new ResourceBundleFile("stallion", "vendor/vue-router.min.js", "vendor/vue-router.js"),
                            new VueResourceBundleFile("stallion", "admin/*.vue"),
                            new ResourceBundleFile("stallion", "admin/users-manage-v2.js")
                    )
            );

*/
            /*
            DefinedBundle.register(
                    "user-admin-vue",
                    new BundleFile().setPluginName("stallion").setLiveUrl("admin/admin.css"),
                    new BundleFile().setPluginName("stallion").setLiveUrl("admin/users-manage.css"),
                    new BundleFile().setPluginName("stallion").setLiveUrl("vendor/vue.min.js").setDebugUrl("vendor/vue.js"),
                    new BundleFile().setPluginName("stallion").setLiveUrl("vendor/vue-router.min.js").setDebugUrl("vendor/vue-router.js"),
                    new VueBundleFile().setPluginName("stallion").setLiveUrl("admin/users-table.vue"),
                    new VueBundleFile().setPluginName("stallion").setLiveUrl("admin/users-edit.vue"),
                    new BundleFile().setPluginName("stallion").setLiveUrl("admin/users-manage-v2.js")
            );   */
        }
    }

    @GET
    @Path("/login")
    @Produces("text/html")
    public Object loginScreen(@QueryParam("email") String email) {
        URL url = getClass().getResource("/templates/public/login.jinja");
        email = or(email, "");
        Map ctx = null;
        try {
            ctx = map(
                    val("allowReset", settings().getUsers().getPasswordResetEnabled()),
                    val("allowRegister", settings().getUsers().getNewAccountsAllowCreation()),
                    val("returnUrl", URLEncoder.encode((or(request().getParameter("stReturnUrl"), "")).replace("\"", ""), "UTF-8")),
                    val("email", Sanitize.escapeHtmlAttribute(email)));
        } catch (UnsupportedEncodingException e) {
           throw new RuntimeException(e);
        }
        String html = TemplateRenderer.instance().renderTemplate(url.toString(), ctx);
        return html;
    }

    @GET
    @Path("/logoff")
    @Produces("text/html")
    public Object logoff() {
        UserController.instance().logoff();
        throw new RedirectException(Settings.instance().getUsers().getLoginPage(), 302);
    }

    @POST
    @JsonView(RestrictedViews.Owner.class)
    @Produces("application/json")
    @Path("/submit-login")
    public Object login(@BodyParam("username") String username, @BodyParam("password") String password, @BodyParam(value = "rememberMe", allowEmpty = true) Boolean rememberMe) {
        return UserController.instance().loginUser(username, password, rememberMe);
    }

    @GET
    @Produces("text/html")
    @Path("/register")
    @MinRole(Role.ANON)
    public String registerPage() {
        if (!settings().getUsers().getNewAccountsAllowCreation()) {
            throw new ClientException("The default new account creation endpoint is not enabled for this site.");
        }
        Map ctx = map();
        return TemplateRenderer.instance().renderTemplate("stallion:/public/register.jinja", ctx);
    }

    @POST
    @Produces("application/json")
    @JsonView(RestrictedViews.Member.class)
    @Path("/do-register")
    public Object doRegister(@BodyParam("displayName") String displayName, @BodyParam("username") String email, @BodyParam("password") String password, @BodyParam("passwordConfirm") String passwordConfirm, @BodyParam(value = "returnUrl", allowEmpty = true) String returnUrl) {
        if (!settings().getUsers().getNewAccountsAllowCreation()) {
            throw new ClientException("The default new account creation endpoint is not enabled for this site.");
        }
        IUser existing = UserController.instance().forEmail(email);
        if (existing != null) {
            throw new ClientException("A user with that email address already exists.");
        }
        User user = new User()
                .setDisplayName(displayName)
                .setUsername(email)
                .setRole(Role.valueOf(settings().getUsers().getNewAccountsRole().toUpperCase()))
                .setEmail(email);

        if (!settings().getUsers().getNewAccountsRequireValidEmail() && settings().getUsers().getNewAccountsAutoApprove() == true) {
            user.setApproved(true);
        }
        Boolean requireValidEmail = false;
        if (settings().getUsers().getNewAccountsRequireValidEmail()) {
            requireValidEmail = true;
        }
        UserController.instance().hydratePassword(user, password, passwordConfirm);
        IUser u = UserController.instance().createUser(user);
        UserController.instance().addSessionCookieForUser(user, true);
        if (requireValidEmail) {
            UserController.instance().sendEmailVerifyEmail(user, or(returnUrl, ""));
        }
        return map(val("user", u), val("requireValidEmail", requireValidEmail));
    }

    @POST
    @Produces("application/json")
    @JsonView(RestrictedViews.Member.class)
    @MinRole(Role.ADMIN)
    @Path("/admin-create-user")
    public Object adminCreateUser(@ObjectParam User newUser) {
        if (empty(newUser.getEmail())) {
            newUser.setEmail(newUser.getUsername());
        } else if (empty(newUser.getUsername())) {
            newUser.setUsername(newUser.getEmail());
        }
        IUser user = SafeMerger.with()
                .nonEmpty("email", "username", "displayName", "role")
                .optional("familyName", "givenName")
                .merge(newUser);
        IUser existing = UserController.instance().forEmail(user.getEmail());
        if (existing != null) {
            throw new ClientException("A user with that email address already exists.");
        }

        user.setApproved(true);
        UserController.instance().createUser(user);
        return user;
    }



    @POST
    @Path("/send-verify-email")
    @Produces("text/html")
    public Object sendVerifyEmail(@BodyParam("email") String email, @BodyParam(value = "returnUrl", allowEmpty = true) String returnUrl) {
        UserController.instance().sendEmailVerifyEmail(email, returnUrl);
        return true;
    }

    @GET
    @Path("/verify-email")
    @Produces("text/html")
    public Object verifyEmailAddress( @QueryParam("email") String email, @QueryParam("returnUrl") String returnUrl, @QueryParam("alreadySent") Boolean alreadySent) {

        email = or(email, Context.getUser().getEmail());
        alreadySent = or(alreadySent, false);
        Map ctx = map(val("email", Sanitize.stripAll(email)), val("alreadySent", alreadySent));
        String html = TemplateRenderer.instance().renderTemplate("stallion:/public/verify-email-address.jinja", ctx);
        return html;
    }

    @GET
    @Path("/verify-email-address")
    @Produces("text/html")
    public Object verifyEmailAddress(@QueryParam("verifyToken") String verifyToken, @QueryParam("email") String email, @QueryParam("returnUrl") String returnUrl) {

        // For security, only verify an email address if we are logged in as that user
        boolean requiresLogin = false;

        if (empty(getUser().getId())) {
            requiresLogin = true;
        } else {
            IUser associatedUser = UserController.instance().forEmail(email);
            if (associatedUser == null || !associatedUser.getId().equals(getUser().getId())) {
                requiresLogin = true;
            }
        }
        if (requiresLogin) {
            try {
                String loginUrl = settings().getUsers().getFullLoginUrl() + "?email=" + email +
                        "&returnUrl=" + URLEncoder.encode(request().requestUrl(), "UTF-8");
                throw new RedirectException(loginUrl, 302);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }


        Map<String, Object> ctx = map(val("verified", false), val("verificationFailed", false));
        if (!empty(verifyToken) && !empty(email)) {
            boolean verified = UserController.instance().verifyEmailVerifyToken(email, verifyToken);
            if (verified) {
                UserController.instance().markEmailVerified(email, verifyToken);
                ctx.put("verified", true);
            } else {
                ctx.put("verificationFailed", true);
            }
        }
        if (empty(email) && !empty(Context.getUser().getEmail())) {
            email = Context.getUser().getEmail();
        }
        IUser user = UserController.instance().forUniqueKey("email", email);
        if (user.getEmailVerified()) {
            ctx.put("verified", true);
            if (!user.getApproved()) {
                ctx.put("requiresApproval", true);
            }

        }

        returnUrl = or(returnUrl, Settings.instance().getSiteUrl());
        ctx.put("returnUrl", Sanitize.stripAll(returnUrl));
        ctx.put("email", Sanitize.stripAll(email));
        URL url = getClass().getResource("/templates/public/verify-email-address.jinja");
        String html = TemplateRenderer.instance().renderTemplate(url.toString(), ctx);
        return html;
    }



    @GET
    @Path("/reset-password")
    @Produces("text/html")
    public Object resetPassword(@QueryParam("resetToken") String resetToken, @QueryParam("email") String email, @QueryParam("returnUrl") String returnUrl) {
        Map<String, Object> ctx = map(val("email", email), val("tokenVerified", false));
        if (!empty(resetToken) && !empty(email)) {
            boolean verified = UserController.instance().verifyPasswordResetToken(email, resetToken);
            if (verified) {
                ctx.put("tokenVerified", verified);
            }
        }
        URL url = getClass().getResource("/templates/public/reset-password.jinja");
        String html = TemplateRenderer.instance().renderTemplate(url.toString(), ctx);
        return html;
    }


    @POST
    @Path("/send-reset-email")
    @Produces("application/json")
    public Object sendResetEmail(@BodyParam("email") String email, @BodyParam(value = "returnUrl", allowEmpty = true) String returnUrl) {
        if (!settings().getUsers().getPasswordResetEnabled()) {
            throw new ClientException("Password reset has been disabled. Please contact an administrator to reset your password.");
        }
        IUser user = UserController.instance().forEmail(email);
        if (user == null) {
            user = UserController.instance().forUsername(email);
        }
        UserController.instance().sendPasswordResetEmail(user, returnUrl);
        return true;
    }


    @POST
    @Path("/do-password-reset")
    @Produces("application/json")
    public Object doPasswordReset(
            @BodyParam("resetToken") String resetToken,
            @BodyParam("email") String email,
            @BodyParam("password") String password,
            @BodyParam("passwordConfirm") String passwordConfirm
    ) {

        IUser user = UserController.instance().changePassword(email, resetToken, password, passwordConfirm);
        if (user == null) {
            return false;
        }
        UserController.instance().addSessionCookieForUser(user, true);
        return true;
    }

    /*

    @POST
    @Path("/create-new-account")
    @Produces("application/json")
    public Object createNewAccount(@BodyParam("displayName") String displayName, @BodyParam(value = "email", isEmail = true) String email, @BodyParam(value = "password", minLength = 6) String password, @BodyParam(value = "passwordConfirm", minLength = 6) String passwordConfirm) {
        if (!settings().getUsers().getNewAccountsAllowCreation()) {
            throw new ClientException("User creation is disabled for this application.");
        }

        String domain = StringUtils.split(email, "@", 2)[1];
        if (!empty(settings().getUsers().getNewAccountsDomainRestricted())) {
            if (!settings().getUsers().getNewAccountsDomainRestricted().equals(domain)) {
                throw new ClientException("You can only register email accounts from domain " + domain);
            }
        }

        IUser user = UserController.instance().forEmail(email);
        if (user == null) {
            throw new ClientException("User with that email address already exists.");
        }
        user = new User();
        user
                .setDisplayName(displayName)
                .setEmail(email);


        if (!empty(settings().getUsers().getNewAccountsRole())) {
            user.setRole(Role.valueOf(settings().getUsers().getNewAccountsRole()));
        }

        if (settings().getUsers().getNewAccountsAutoApprove()) {
            user.setApproved(true);
        }

        UserController.instance().hydratePassword(user, password, passwordConfirm);
        UserController.instance().createUser(user);

        Map<String, Object> ctx = map(val("verifyEmailSent", false), val("pendingAdminApproval", false));
        if (!settings().getUsers().getNewAccountsAutoApprove()) {
            ctx.put("pendingAdminApproval", true);
        }

        if (settings().getUsers().getNewAccountsRequireValidEmail()) {
            UserController.instance().sendEmailVerifyEmail(user, "");
            ctx.put("verifyEmailSent", true);
        }


        UserController.instance().addSessionCookieForUser(user, true);

        return map(val("verifyEmailSent", true), val("pendingAdminApproval", false));


    }

         */

    @GET
    @Path("/current-user-info")
    @JsonView(RestrictedViews.Owner.class)
    public Object currentUserInfo() {
        if (Context.getUser() == null || empty(Context.getUser().getId())) {
            throw new ClientException("You are not logged in", 401);
        }
        return Context.getUser();
    }


    /******
     *
     * ADMIN Endpoints
     *
     */



    @GET
    @Path("/manage")
    @MinRole(Role.ADMIN)
    @Produces("text/html")
    public String manageUsers2() {

        response().getMeta().setTitle("Manage Users");
        Map<String, Object> ctx = map();
        return TemplateRenderer.instance().renderTemplate("stallion:admin/admin-users2.jinja", ctx);
    }


    @GET
    @Path("/users-screen")
    @MinRole(Role.ADMIN)
    @Produces("application/json")
    public Map manageUsersScreen() {
        Map<String, Object> ctx = map();
        return ctx;
    }

    @GET
    @Path("/users-table")
    @MinRole(Role.ADMIN)
    @Produces("application/json")
    @JsonView(RestrictedViews.Owner.class)
    public Pager usersTable(@QueryParam("page") Integer page, @QueryParam("withDeleted") Boolean withDeleted) {
        Map<String, Object> ctx = map();
        if (page == null || page < 1) {
            page = 1;
        }
        withDeleted = or(withDeleted, false);
        FilterChain chain = UserController.instance().filterChain();
        if (withDeleted) {
            chain = chain.includeDeleted();
        }
        Pager pager = chain.sort("email", "ASC").pager(page, 100);
        return pager;
    }


    @GET
    @Path("/view-user/:userId")
    @MinRole(Role.ADMIN)
    @Produces("application/json")
    @JsonView(RestrictedViews.Owner.class)
    public IUser viewUser(@PathParam("userId") Long userId) {
        IUser user = UserController.instance().forIdWithDeleted(userId);
        if (user == null) {
            throw new io.stallion.exceptions.NotFoundException("User not found");
        }
        return user;
    }

    @POST
    @Path("/update-user/:userId")
    @MinRole(Role.ADMIN)
    @Produces("application/json")
    @JsonView(RestrictedViews.Owner.class)
    public IUser updateUser(@PathParam("userId") Long userId, @ObjectParam(targetClass=User.class) User updatedUser) {
        IUser user = UserController.instance().forIdWithDeleted(userId);
        if (user == null) {
            throw new io.stallion.exceptions.NotFoundException("User not found");
        }
        if (!updatedUser.getEmail().equals(user.getEmail())) {
            IUser existing = UserController.instance().forEmail(updatedUser.getEmail());
            if (existing != null) {
                throw new ClientException("User with email " + updatedUser.getEmail() + " already exists");
            }
            user.setEmailVerified(false);
        }
        if (!updatedUser.getUsername().equals(user.getUsername())) {
            IUser existing = UserController.instance().forUsername(updatedUser.getUsername());
            if (existing != null) {
                throw new ClientException("User with username " + updatedUser.getUsername() + " already exists");
            }
        }
        user
                .setDisplayName(updatedUser.getDisplayName())
                .setUsername(updatedUser.getUsername())
                .setEmail(updatedUser.getEmail())
                .setRole(updatedUser.getRole())
                .setFamilyName(updatedUser.getFamilyName())
                .setGivenName(updatedUser.getGivenName());

        UserController.instance().save(user);


        return user;
    }

    @POST
    @Path("/toggle-user-disabled/:userId")
    @MinRole(Role.ADMIN)
    @Produces("application/json")
    @JsonView(RestrictedViews.Owner.class)
    public Object toggleDisableUser(@PathParam("userId") Long userId, @BodyParam("disabled") boolean disabled) {
        IUser user = UserController.instance().forIdWithDeleted(userId);
        if (user == null) {
            throw new io.stallion.exceptions.NotFoundException("User not found");
        }
        user.setDisabled(disabled);
        UserController.instance().save(user);
        return true;
    }

    @POST
    @Path("/toggle-user-approved/:userId")
    @MinRole(Role.ADMIN)
    @Produces("application/json")
    @JsonView(RestrictedViews.Owner.class)
    public Object toggleUserApproved(@PathParam("userId") Long userId, @BodyParam("approved") boolean approved) {
        IUser user = UserController.instance().forIdWithDeleted(userId);
        if (user == null) {
            throw new io.stallion.exceptions.NotFoundException("User not found");
        }
        user.setApproved(approved);
        UserController.instance().save(user);
        return true;
    }

    @POST
    @Path("/toggle-user-deleted/:userId")
    @MinRole(Role.ADMIN)
    @Produces("application/json")
    @JsonView(RestrictedViews.Owner.class)
    public Object toggleUserDeleted(@PathParam("userId") Long userId, @BodyParam("deleted") boolean deleted) {
        IUser user = UserController.instance().forIdWithDeleted(userId);
        if (user == null) {
            throw new io.stallion.exceptions.NotFoundException("User not found");
        }
        if (deleted == true) {
            UserController.instance().softDelete(user);
        } else {
            user.setDeleted(deleted);
            UserController.instance().save(user);
        }

        return true;
    }

    @POST
    @Path("/force-password-reset/:userId")
    @MinRole(Role.ADMIN)
    @Produces("application/json")
    @JsonView(RestrictedViews.Owner.class)
    public Object triggerPasswordReset(@PathParam("userId") Long userId) {
        IUser user = UserController.instance().forIdWithDeleted(userId);
        if (user == null) {
            throw new io.stallion.exceptions.NotFoundException("User not found");
        }
        user.setBcryptedPassword("");
        UserController.instance().save(user);
        UserController.instance().sendPasswordResetEmail(user, "");
        return true;
    }

    @Path("/selenium/get-reset-token")
    @GET
    @Produces("application/json")
    public Map getResetToken(@QueryParam("email") String email, @QueryParam("secret") String secret) {
        if (!Settings.instance().getHealthCheckSecret().equals(secret)) {
            throw new ClientException("Invalid or missing ?secret= query param. Secret must equal the healthCheckSecret in settings.");
        }
        if (!email.startsWith("selenium+resettest+") || !email.endsWith("@stallion.io")) {
            throw new ClientException("Invalid email address. Must be a stallion selenium email.");
        }
        IUser user = UserController.instance().forEmail(email);
        String token = UserController.instance().makeEncryptedToken(user, "reset", user.getResetToken());
        return map(val("resetToken", token));
    }

}
