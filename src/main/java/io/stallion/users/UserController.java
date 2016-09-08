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

import io.stallion.Context;
import io.stallion.dataAccess.AuditTrailEnabled;
import io.stallion.dataAccess.DataAccessRegistration;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.DbPersister;
import io.stallion.dataAccess.file.JsonFilePersister;
import io.stallion.email.ContactableEmailer;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.StRequest;
import io.stallion.services.LocalMemoryCache;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.DateUtils;
import io.stallion.utils.Encrypter;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.http.Cookie;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

@AuditTrailEnabled
public class UserController<T extends IUser> extends StandardModelController<T> {
    private static final String PROBLEM_LOG_CACHE_BUCKET = "problemLog";
    private static final int PROBLEM_LOG_DURATION_SECONDS = 5 * 60;
    private static final int MAX_PROBLEMS = 7;

    public static String USER_COOKIE_NAME = "stUserSession";


    public static <Y  extends IUser> UserController<Y> instance() {
        return (UserController<Y>) DataAccessRegistry.instance().get("users");
    }

    public static void load() {
        DataAccessRegistration registration = new DataAccessRegistration()
                .setStashClass(UserMemoryStash.class)
                .setControllerClass(UserController.class)
                .setModelClass(User.class);
        if (DB.instance() == null) {

            registration
                    .setPersisterClass(JsonFilePersister.class)
                    .setPath("users")
                    .setNameSpace("")
                    .setWritable(true)
                    .setUseDataFolder(true)
                    .setShouldWatch(true);

            registration.hydratePaths(Settings.instance().getTargetFolder());
            File usersDir = new File(registration.getAbsolutePath());
            if (!usersDir.isDirectory()) {
                usersDir.mkdirs();
            }
        } else {
            registration
                    .setTableName("stallion_users")
                    .setBucket("users")
                    .setPersisterClass(DbPersister.class);
            if (!Settings.instance().getUsers().getSyncAllUsersToMemory()) {
                registration.setStashClass(UserPartialStash.class);
            }

        }
        Context.dal().register(registration);

    }


    public T forEmail(String email)  {
        // TODO make lookup by key work
        T user = filter("email", email).first();
        if (user == null) {
            return null;
        }
        if (!empty(user.getAliasForId())) {
            user = forId(user.getAliasForId());
        }
        return user;
    }

    public T forUsername(String username) {
        // TODO make lookup by key work
        T user = filter("username", username).first();
        if (user != null && !empty(user.getAliasForId())) {
            user = forId(user.getAliasForId());
        }
        return user;
    }

    public T createUser(T user) {
        save(user);
        return user;
    }

    /**
     * Hydrates the bycryptedPassword field, validiating the password for minimum length and matching confirmation.
     *
     * @param user
     * @param password
     * @param passwordConfirm
     */
    public void hydratePassword(T user, String password, String passwordConfirm) {

        if (empty(password) || password.length() < 6) {
            throw new ClientException("Password is empty or too short");
        }

        if (!password.equals(passwordConfirm)) {
            throw new ClientException("Confirmation password does not match!");
        }

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setBcryptedPassword(hashed);
    }


    @Override
    public void onPreCreatePrepare(T user) {
        user.setIsNewInsert(true);
        if (empty(user.getSecret())) {
            user.setSecret(RandomStringUtils.randomAlphanumeric(18));
        }
        if (empty(user.getEncryptionSecret())) {
            user.setEncryptionSecret(RandomStringUtils.randomAlphanumeric(36));
        }
        if (user.getCreatedAt() == null || user.getCreatedAt() == 0) {
            user.setCreatedAt(DateUtils.mils());
        }


    }

    /**
     * Checks the standard Stallion auth cookie, loads and validates the user,
     * and hydrates the current request Context user, and returns true. Returns
     * false if there is not cookie, or it did not represent a valid user.
     *
     * @param request
     * @return
     */
    public boolean checkCookieAndAuthorizeForRequest(StRequest request) {
        Cookie userCookie = request.getCookie(UserController.USER_COOKIE_NAME);
        if (userCookie == null) {
            return false;
        }
        try {
            UserValetResult result = UserController.instance().cookieStringToUser(userCookie.getValue());
            if (result != null && result.getUser() != null) {
                Context.setUser(result.getUser());
                if (result.getValet() != null) {
                    Context.setValet(result.getValet().getId(), result.getValet().getEmail());
                }
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            Log.exception(e, "Error loading user from cookie");
            return false;
        }
    }

    /**
     * Void out the authentication cookie
     */
    public void logoff() {
        Context.getResponse().addCookie(UserController.USER_COOKIE_NAME, "", 1);
    }

    /**
     * Checks the user information represents a valid login, adds the user to the context,
     * and adds a cookie to the current request response.
     *
     * @param username
     * @param password
     * @param rememberMe
     * @return
     */
    public T loginUser(String username, String password, Boolean rememberMe) {
        T user = checkUserLoginValid(username, password);
        return addSessionCookieForUser(user, rememberMe);
    }



    public T valetLoginIfAllowed(String email) {
        T user = forEmail(email);
        if (user == null) {
            user = forUsername(email);
        }
        if (user == null) {
            throw new ClientException("Could not find user matching email " + email);
        }
        return valetLoginIfAllowed(user);
    }

    public T valetLoginIfAllowed(Long userId) {
        T user = forIdOrNotFound(userId);
        return valetLoginIfAllowed(user);
    }

    public T valetLoginIfAllowed(T user) {
        if (!Settings.instance().getUsers().getAllowValetMode()) {
            throw new ClientException("Valet mode not enabled for this site.");
        }
        T valet = (T)Context.getUser();
        if (valet == null) {
            throw new ClientException("You are not logged in, cannot use valet mode");
        }

        Long valetId = null;
        if (!empty(Context.getValetUserId())) {
            valet = forId(Context.getValetUserId());
        } else if (!valet.isInRole(Role.ADMIN)) {
            throw new ClientException("You must be an admin to use valet mode");
        }
        if (valet.getId().equals(user.getId())) {
            // Valet is switching out of valet mode, back to being their normal user
            return addSessionCookieForUser(user, true);
        }
        if (valet.getRole().getValue() <= user.getRole().getValue()) {
            throw new ClientException("You cannot valet to a user who has the same role as you, only into a user of lesser role.");
        }
        return addSessionCookieForUser(user, false, valet);
    }


    public void logoutCurrentUser() {

    }

    /**
     * Returns a user if the login information is valid, throws a ClientException exception otherwise.
     *
     * @param username
     * @param password
     * @return
     */
    public T checkUserLoginValid(String username, String password) throws ClientException {
        Integer failures = or((Integer)LocalMemoryCache.get(PROBLEM_LOG_CACHE_BUCKET, request().getActualIp()), 0);
        if (failures > MAX_PROBLEMS) {
            throw new ClientException("You have too many login failures in the last 10 minutes. Please wait before trying again.", 429);
        }


        T user = forUsername(username);
        String err = "User not found or password invalid";
        if (user == null) {
            markFailed(username);
            throw new ClientException(err, 403);
        }

        failures = or((Integer)LocalMemoryCache.get(PROBLEM_LOG_CACHE_BUCKET, username), 0);
        if (failures > (MAX_PROBLEMS + 5)) { // We are more tolerant of user name failures, else easy to lock someone else out of account
            throw new ClientException("You have too many login failures in the last 10 minutes. Please wait before trying again.", 429);
        }

        if (empty(user.getBcryptedPassword())) {
            throw new ClientException("Password never confiruged for this user. Did you originally login with Google or Facebook? Otherwise, click on the password reset link to choose a new password.");
        }

        boolean valid = BCrypt.checkpw(password, user.getBcryptedPassword());



        if (!valid) {
            markFailed(username);
            throw new ClientException(err, 403);
        }
        return user;
    }

    /**
     * Mark a login failure in the local cache, too many failures and the user or IP address will be locked out.
     *
     * @param username
     */
    public void markFailed(String username) {
        Integer failures = or((Integer)LocalMemoryCache.get(PROBLEM_LOG_CACHE_BUCKET, request().getActualIp()), 0);
        Log.fine("Mark login failed {0} {1} failCount={2}", username, request().getActualIp(), failures + 1);
        LocalMemoryCache.set(PROBLEM_LOG_CACHE_BUCKET, request().getActualIp(), failures + 1, PROBLEM_LOG_DURATION_SECONDS);

        if (!empty(username)) {
            failures = or((Integer) LocalMemoryCache.get(PROBLEM_LOG_CACHE_BUCKET, username), 0);
            LocalMemoryCache.set(PROBLEM_LOG_CACHE_BUCKET, username, failures + 1, PROBLEM_LOG_DURATION_SECONDS);
        }

    }

    /**
     * Add a session cookie to the current request response for this user.
     *
     * @param user
     * @param rememberMe
     * @return
     */
    public T addSessionCookieForUser(T user, Boolean rememberMe) {
        return addSessionCookieForUser(user, rememberMe, null);
    }
    /**
     * Add a session cookie to the current request response for this user.
     *
     * @param user
     * @param rememberMe
     * @return
     */
    public T addSessionCookieForUser(T user, Boolean rememberMe, T valetUser) {
        Long valetUserId = null;
        if (valetUser != null) {
            valetUserId = valetUser.getId();
        }
        String cookie = userToCookieString(user, rememberMe, valetUserId);
        int expires = (int)((mils() + (86400*30*1000))/1000);
        if (rememberMe) {
            Context.getResponse().addCookie(UserController.USER_COOKIE_NAME, cookie, expires);
        } else {
            Context.getResponse().addCookie(UserController.USER_COOKIE_NAME, cookie);
        }
        return user;
    }

    /**
     * Change the primary email of this user to an already validated and verified
     * that is alaread an alias for the user id. Returns false if invalid for any reseason.
     *
     * @param user
     * @param newPrimaryEmail
     * @return
     */
    public boolean changePrimaryEmail(T user, String newPrimaryEmail) {
        T aliasUser = forUniqueKey("email", newPrimaryEmail);
        if (aliasUser == null) {
            return false;
        }
        if (!aliasUser.getEmailVerified()) {
            return false;
        }
        if (!aliasUser.getAliasForId().equals(user.getId())) {
            return false;
        }
        boolean orgWasVerified = user.getEmailVerified();
        String orgEmail = user.getEmail();
        if (user.getUsername().equals(orgEmail)) {
            user.setUsername(newPrimaryEmail);
        }
        user.setEmail(newPrimaryEmail);
        user.setEmailVerified(true);

        // Need to set a placeholder to avoid unique key errors
        String placeholder = UUID.randomUUID().toString() + "@" + UUID.randomUUID().toString() + ".com";
        aliasUser.setEmail(placeholder);
        aliasUser.setUsername(placeholder);
        save(aliasUser);

        // Now save the user
        save(user);

        // Now put the old primary
        aliasUser.setUsername(orgEmail);
        aliasUser.setEmail(orgEmail);
        aliasUser.setEmailVerified(orgWasVerified);
        save(aliasUser);

        return true;
    }

    public boolean sendEmailVerifyEmail(String email) {
        return sendEmailVerifyEmail(email, "");
    }

    public boolean sendEmailVerifyEmail(String email, String returnUrl) {
        T user = forUniqueKey("email", email);
        return sendEmailVerifyEmail(user, returnUrl);
    }

    public boolean sendEmailVerifyEmail(T user) {
        return sendEmailVerifyEmail(user, "");
    }
    public boolean sendEmailVerifyEmail(T user, String returnUrl) {

        if (user == null) {
            return false;
        }
        if (user.isPredefined()) {
            throw new ClientException("You cannot verify email for a builtin user. You must edit this user in your configuration files.");
        }
        // send email
        String token = makeVerifyEmailToken(user);
        new VerifyEmailEmailer(user, token, returnUrl).sendEmail();
        return true;
    }

    public String makeVerifyEmailToken(T user) {
        if (empty(user.getResetToken())) {
            user.setResetToken(GeneralUtils.randomToken(14));
            save(user);
        }
        return makeEncryptedToken(user, "verifyEmail", user.getResetToken());
    }

    public String makeEncryptedToken(T user, String type, String value) {
        String fullToken = user.getId() + "|" + type + "|" + DateUtils.mils() + "|" + value;
        String encryptedToken = Encrypter.encryptString(user.getEncryptionSecret(), fullToken);
        return encryptedToken;
    }

    public String readEncryptedToken(T user, String expectedType, String encrypted, int expiresMinutes) {
        String full = Encrypter.decryptString(user.getEncryptionSecret(), encrypted);
        String[] parts = full.split("\\|", 4);
        Log.info("decrypted token {0}", full);
        long actualId = Long.parseLong(parts[0]);
        long userId = user.getId();
        String actualType = parts[1];
        long createdAt = Long.parseLong(parts[2]);
        String value = parts[3];
        if (actualId != userId) {
            Log.finer("Token has userId: {0} but passed in user had id: {1}", actualId, userId);
            return null;
        }
        if (!actualType.equals(expectedType)) {
            Log.finer("Incorrect token type expected:{0} got: {1}", expectedType, actualType);
            return null;
        }
        if ((createdAt + (expiresMinutes * 60 * 1000)) < DateUtils.mils()) {
            Log.finer("Token has expired");
            return null;
        }
        return value;
    }

    public boolean verifyEmailVerifyToken(String email, String encryptedToken) {
        T user = forUniqueKey("email", email);
        return verifyEmailVerifyToken(user, encryptedToken);
    }

    public boolean verifyEmailVerifyToken(T user, String encryptedToken) {
        if (user == null) {
            return false;
        }
        String resetToken = readEncryptedToken(user, "verifyEmail", encryptedToken, 10*60*24);
        if (empty(resetToken)) {
            return false;
        }
        if (resetToken.equals(user.getResetToken())) {
            return true;
        }

        return false;
    }

    public void markEmailVerified(String email, String token) {
        markEmailVerified(forUniqueKeyOrNotFound("email", email), token);
    }

    public void markEmailVerified(T user, String token) {
        if (!verifyEmailVerifyToken(user, token)) {
            throw new ClientException("Invalid verification token");
        }
        user.setEmailVerified(true);
        user.setResetToken("");
        // set reset token to a new value

        if (Settings.instance().getUsers().getNewAccountsAutoApprove()) {
            user.setApproved(true);
        }
        save(user);
    }


    public boolean sendPasswordResetEmail(String email) {
        return sendPasswordResetEmail(forEmail(email), "");
    }

    public boolean sendPasswordResetEmail(String email, String returnUrl) {
        return sendPasswordResetEmail(forEmail(email), returnUrl);
    }

    public boolean sendPasswordResetEmail(T user, String returnUrl) {
        if (user == null) {
            return false;
        }
        if (user.isPredefined()) {
            throw new ClientException("You cannot reset the password for a builtin user. You must edit this user in your configuration files.");
        }
        if (empty(user.getResetToken())) {
            user.setResetToken(GeneralUtils.randomToken(14));
            save(user);
        }



        String encryptedToken = makeEncryptedToken(user, "reset", user.getResetToken());

        ResetEmailEmailer emailer = new ResetEmailEmailer(user, encryptedToken, returnUrl);
        emailer.sendEmail();
        return true;
    }

    public boolean verifyPasswordResetToken(String email, String encryptedToken) {
        return verifyPasswordResetToken(forEmail(email), encryptedToken);
    }

    public boolean verifyPasswordResetToken(T user, String encryptedToken) {
        if (user == null) {
            return false;
        }
        String token = readEncryptedToken(user, "reset", encryptedToken, 10*60*24);
        if (empty(token)) {
            return false;
        }
        if (!token.equals(user.getResetToken())) {
            return true;
        }
        return true;
    }

    public T changePassword(String email, String token, String newPassword, String confirmNewPassword) {
        return changePassword(forEmail(email), token, newPassword, confirmNewPassword);
    }

    public T changePassword(T user, String token, String newPassword, String confirmNewPassword) {
        if (!verifyPasswordResetToken(user, token)) {
            throw new ClientException("Invalid reset token");
        }
        hydratePassword(user, newPassword, confirmNewPassword);
        user.setResetToken("");
        this.save(user);
        return user;
    }


    public String userToCookieString(T user, Boolean rememberMe) {
        return userToCookieString(user, rememberMe, null);
    }

    public String userToCookieString(T user, Boolean rememberMe, Long valetId) {
        Long now = mils();
        Long expires = now + (86400L * 1000L);
        if (rememberMe) {
            expires = now + (90L * 86400L * 1000L);
        }
        SessionInfo session = new SessionInfo()
                .setSec(user.getSecret())
                .setCrt(mils())
                .setExp(expires);
        if (!empty(valetId)) {
            session.setVid(valetId);
        }

        String encryptedPart = Encrypter.encryptString(user.getEncryptionSecret(), JSON.stringify(session));

        String cookie = user.getId().toString() + "&" + encryptedPart;
        return cookie;
    }

    public UserValetResult cookieStringToUser(String cookie) {
        String[] parts = cookie.split("&", 2);
        if (parts.length < 2) {
            return null;
        }
        if (!StringUtils.isNumeric(parts[0])) {
            Log.warn("Invalid user id found in cookie: {0}", parts[0]);
            return null;
        }
        Long id = Long.parseLong(parts[0]);
        String encryptedJson = parts[1];


        T user = forId(id);
        if (user == null) {
            return null;
        }
        String json = Encrypter.decryptString(user.getEncryptionSecret(), encryptedJson);
        SessionInfo info = JSON.parse(json, SessionInfo.class);

        if (info.getExp() < mils()) {
            return null;
        }
        if (!info.getSec().equals(user.getSecret())) {
            return null;
        }
        UserValetResult result = new UserValetResult()
                .setUser(user);
        if (!empty(info.getVid())) {
            T valet = forId(info.getVid());
            if (valet == null) {
                return null;
            }
            result.setValet(valet);
        }
        return result;

    }

    public static class UserValetResult {
        public IUser user;
        public IUser valet;

        public IUser getUser() {
            return user;
        }

        public UserValetResult setUser(IUser user) {
            this.user = user;
            return this;
        }

        public IUser getValet() {
            return valet;
        }

        public UserValetResult setValet(IUser valet) {
            this.valet = valet;
            return this;
        }
    }

    public static class SessionInfo {
        private String sec = "";
        private Long exp = 0L;
        private Long crt = 0L;
        private Long vid = 0L;

        public String getSec() {
            return sec;
        }

        public SessionInfo setSec(String sec) {
            this.sec = sec;
            return this;
        }


        public Long getExp() {
            return exp;
        }

        public SessionInfo setExp(Long exc) {
            this.exp = exc;
            return this;
        }

        public Long getCrt() {
            return crt;
        }

        public SessionInfo setCrt(Long crt) {
            this.crt = crt;
            return this;
        }

        public Long getVid() {
            return vid;
        }

        public SessionInfo setVid(Long vid) {
            this.vid = vid;
            return this;
        }
    }

    public class VerifyEmailEmailer extends ContactableEmailer {

        public VerifyEmailEmailer(T user, Map<String, Object> context) {
            super(user, context);
        }

        public VerifyEmailEmailer(T user, String token, String returnUrl) {
            super(user);
            put("verifyToken", token);
            String url = Settings.instance().getUsers().getVerifyEmailPage();
            if (!url.contains("//")) {
                url = Settings.instance().getSiteUrl() + url;
            }

            try {
                url = url + "?verifyToken=" + URLEncoder.encode(token, "UTF-8") +
                        "&email=" + URLEncoder.encode(user.getEmail(), "UTF-8");
                if (!empty(returnUrl)) {
                    url += "&returnUrl=" + URLEncoder.encode(returnUrl, "UTF-8");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            url = url + "#verify-email";
            put("verifyUrl", url);
        }

        @Override
        public boolean isTransactional() {
            return true;
        }

        @Override
        public String getTemplate()  {
            return "stallion:email/verify-email-address.jinja";
        }

        @Override
        public String getSubject() {
            return "Verify your email address.";
        }

        public String getUniqueKey() {
            return truncate(GeneralUtils.slugify(getSubject()), 150) + "-" + user.getEmail() + "-" + minuteStamp + getEmailType();
        }
    }

    public class ResetEmailEmailer extends ContactableEmailer {

        public ResetEmailEmailer(T user, String resetToken, String returnUrl) {
            super(user);
            put("resetToken", resetToken);
            String url = Settings.instance().getUsers().getPasswordResetPage();
            if (!url.contains("//")) {
                url = Settings.instance().getSiteUrl() + url;
            }
            try {
                url = url + "?resetToken=" + URLEncoder.encode(resetToken, "UTF-8") +
                        "&email=" + URLEncoder.encode(user.getEmail(), "UTF-8")  +
                        "&returnUrl=" + URLEncoder.encode(or(returnUrl, ""), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            url = url + "#confirm-reset-token";
            put("resetUrl", url);
        }

        @Override
        public boolean isTransactional() {
            return true;
        }

        @Override
        public String getTemplate() {
            return "stallion:email/reset-password.jinja";
        }

        @Override
        public String getSubject() {
            return "Reset your password for " + Settings.instance().getSiteName();
        }


        public String getUniqueKey() {
            return truncate(GeneralUtils.slugify(getSubject()), 150) + "-" + user.getEmail() + "-" + minuteStamp + getEmailType();
        }

    }
}
