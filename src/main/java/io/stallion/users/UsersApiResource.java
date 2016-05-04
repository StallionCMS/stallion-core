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

package io.stallion.users;

import com.fasterxml.jackson.annotation.JsonView;
import io.stallion.Context;
import io.stallion.assets.AssetsController;
import io.stallion.assets.BundleFile;
import io.stallion.assets.BundleHandler;
import io.stallion.assets.DefinedBundle;
import io.stallion.dal.filtering.FilterChain;
import io.stallion.dal.filtering.Pager;
import io.stallion.exceptions.*;
import io.stallion.restfulEndpoints.*;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.utils.Sanitize;
import io.stallion.utils.json.RestrictedViews;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.*;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class UsersApiResource implements EndpointResource {

    @GET
    @Path("/login")
    @Produces("text/html")
    public Object loginScreen() {
        URL url = getClass().getResource("/templates/semipublic/login.jinja");
        String html = TemplateRenderer.instance().renderTemplate(url.toString());
        return html;
    }

    @POST
    @JsonView(RestrictedViews.Owner.class)
    @Path("/submit-login")
    public Object login(@BodyParam("username") String username, @BodyParam("password") String password, @BodyParam("rememberMe") Boolean rememberMe) {
        IUser user = UserController.instance().forUniqueKey("username", username);
        String err = "User not found or password invalid";
        if (user == null) {
            throw new ClientException(err, 403);
        }

        boolean valid = BCrypt.checkpw(password, user.getBcryptedPassword());
        if (!valid) {
            throw new ClientException(err, 403);
        }
        String cookie = UserController.instance().userToCookieString(user, rememberMe);
        int expires = (int)((mils() + (86400*30*1000))/1000);
        if (rememberMe) {
            Context.getResponse().addCookie(UserController.USER_COOKIE_NAME, cookie, expires);
        } else {
            Context.getResponse().addCookie(UserController.USER_COOKIE_NAME, cookie);
        }
        return user;
    }



    @POST
    @Path("/send-verify-email")
    @Produces("text/html")
    public Object sendVerifyEmail(@BodyParam("email") String email, @BodyParam("returnUrl") String returnUrl) {
        UserController.instance().sendEmailVerifyEmail(email, returnUrl);
        return true;
    }


    @GET
    @Path("/verify-email-address")
    @Produces("text/html")
    public Object verifyEmailAddress(@QueryParam("verifyToken") String verifyToken, @QueryParam("email") String email, @QueryParam("returnUrl") String returnUrl) {

        // For security, only verify an email address if we are logged in as that user
        boolean requiresLogin = false;

        if (!getUser().isAuthorized()) {
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
        }

        returnUrl = or(returnUrl, Settings.instance().getSiteUrl());
        ctx.put("returnUrl", Sanitize.stripAll(returnUrl));
        ctx.put("email", Sanitize.stripAll(email));
        URL url = getClass().getResource("/templates/semipublic/verify-email-address.jinja");
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
        URL url = getClass().getResource("/templates/semipublic/reset-password.jinja");
        String html = TemplateRenderer.instance().renderTemplate(url.toString(), ctx);
        return html;
    }


    @POST
    @Path("/send-reset-email")
    @Produces("text/html")
    public Object sendResetEmail(@BodyParam("email") String email, @BodyParam("returnUrl") String returnUrl) {
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
    public Object sendResetEmail(
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



    @GET
    @Path("/new")
    @Produces("text/html")
    public Object newAccount() {
        URL url = getClass().getResource("/templates/semipublic/login.jinja");
        String html = TemplateRenderer.instance().renderTemplate(url.toString());
        return html;
    }

    @POST
    @Path("/create-new-account")
    @Produces("application/json")
    public Object createNewAccount(@BodyParam("displayName") String displayName, @BodyParam("email") String email, @BodyParam("password") String password, @BodyParam("passwordConfirm") String passwordConfirm) {
        if (!settings().getUsers().getNewAccountsAllowCreation()) {
            throw new ClientException("User creation is disabled for this application.");
        }

        if (empty(email) || !email.contains("@")) {
            throw new ClientException("Email is missing or not valid");
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
        if (!settings().getUsers().getNewAccountsAutoApprove() && !settings().getUsers().getNewAccountsAutoApproveIfEmailValid()) {
            ctx.put("pendingAdminApproval", true);
        }

        if (settings().getUsers().getNewAccountsRequireValidEmail()) {
            UserController.instance().sendEmailVerifyEmail(user, "");
            ctx.put("verifyEmailSent", true);
        }


        UserController.instance().addSessionCookieForUser(user, true);

        return map(val("verifyEmailSent", true), val("pendingAdminApproval", false));


    }



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
    public String manageUsers() {


        Map<String, Object> ctx = map();
        return TemplateRenderer.instance().renderTemplate("stallion:admin/admin-users.jinja", ctx);
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


    public static void register() {
        if (Settings.instance().getUsers().getEnableDefaultEndpoints()) {
            EndpointsRegistry.instance().addResource("/st-admin/users", new UsersApiResource());
        }
        DefinedBundle.register(new DefinedBundle(
                "userAdminStylesheets", ".css",
                new BundleFile().setPluginName("stallion").setLiveUrl("admin/users-manage.css")
        ));
        DefinedBundle.register(new DefinedBundle(
                "userAdminJavascripts", ".js",
                new BundleFile().setPluginName("stallion").setLiveUrl("admin/users-manage.js")
        ));
    }
}
