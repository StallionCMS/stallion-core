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

import io.stallion.Context;
import io.stallion.dal.DalRegistry;
import io.stallion.dal.base.DalRegistration;
import io.stallion.dal.base.LocalMemoryStash;
import io.stallion.dal.base.NoStash;
import io.stallion.dal.base.StandardModelController;
import io.stallion.dal.db.DB;
import io.stallion.dal.db.DbPersister;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.StRequest;
import io.stallion.settings.Settings;
import io.stallion.utils.Encrypter;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static io.stallion.utils.Literals.*;


public class OAuthApprovalController extends StandardModelController<OAuthApproval> {

    public static void register() {
        DalRegistration registration = new DalRegistration()
                .setBucket("oauth_approvals")
                .setControllerClass(OAuthApprovalController.class)
                .setModelClass(OAuthApproval.class)
                ;
        if (DB.available()) {
            registration
                    .setStashClass(NoStash.class)
                    .setTableName("oauth_approvals")
                    .setPersisterClass(DbPersister.class);
        } else {
            registration
                    .setStashClass(LocalMemoryStash.class)
                    .setPath("oauth_approvals")
                    .setShouldWatch(false);

        }

    }

    public static OAuthApprovalController instance() {
        return (OAuthApprovalController)DalRegistry.instance().get("oauth_approvals");
    }

    public boolean checkHeaderAndAuthorizeUserForRequest(StRequest request) {
        String header = request.getHeader("Authorization");
        if (empty(header)) {
            return false;
        }
        if (!header.startsWith("Bearer ")) {
            return false;
        }
        String token = header.substring(7);
        OAuthUserLogin login = tokenToUser(token);
        Context.getRequest().setScoped(login.isScoped());
        Context.getRequest().setScopes(login.getScopes());
        Context.setUser(login.getUser());
        return true;
    }

    public OAuthUserLogin tokenToUser(String accessToken) {
        return tokenToUser(accessToken, false);
    }
    public OAuthUserLogin tokenToUser(String accessToken, boolean ignoreExpires) {
        if (!accessToken.contains("-")) {
            throw new ClientException("Invalid access token format");
        }
        String[] parts = StringUtils.split(accessToken, "-", 2);
        if (!StringUtils.isNumeric(parts[0])) {
            throw new ClientException("Invalid access token format");
        }
        long userId = Long.parseLong(parts[0]);
        IUser user = UserController.instance().forId(userId);
        if (user == null) {
            throw new ClientException("User not found for access token");
        }
        String decrypted = Encrypter.decryptString(user.getEncryptionSecret(), parts[1]);
        OAuthAccesTokenData data = JSON.parse(decrypted, OAuthAccesTokenData.class);
        if (data.getUserId() != userId) {
            throw new ClientException("Invalid access token");
        }
        if (!ignoreExpires && data.getExpires() < mils()) {
            throw new ClientException("Access token has expired");
        }
        if (Settings.instance().getoAuth().getAlwaysCheckAccessTokenValid()) {
            OAuthApproval approval = OAuthApprovalController.instance().forId(data.getApprovalId());
            if (approval == null || approval.isRevoked() || (!ignoreExpires && approval.getAccessTokenExpiresAt() < mils())) {
                throw new ClientException("Invalid approval");
            }
        }
        OAuthUserLogin login = new OAuthUserLogin()
                .setScoped(data.isScoped())
                .setUser(user)
                .setApprovalId(data.getApprovalId())
                .setScopes(data.getScopes());
        return login;
    }

    public OAuthApproval checkGrantApprovalForUser(GrantType grantType, IUser user, String fullClientId, Set<String> scopes, boolean isScoped, String redirectUri) {
        return checkGrantApprovalForUser(grantType, user, fullClientId, scopes, isScoped, "");
    }

    public OAuthApproval checkGrantApprovalForUser(GrantType grantType, IUser user, String fullClientId, Set<String> scopes, boolean isScoped, String redirectUri, String providedCode) {
        if (empty(user) || !user.isAuthorized() || empty(user.getId())) {
            throw new ClientException("You are not logged in with a valid user.");
        }

        OAuthClient client = OAuthClientController.instance().clientForFullId(fullClientId);

        if (empty(client) || client.getFullClientId().equals(fullClientId) || client.isDisabled() || client.getDeleted()) {
            throw new ClientException("Invalid client id");
        }
        if (!client.hasGrantType(grantType)) {
            throw new ClientException("Cannot use this grant type with this client");
        }
        if (!client.getAllowedRedirectUris().contains(redirectUri)) {
            throw new ClientException("Unauthorized redirect_uri");
        }
        if (!isScoped) {
            if (client.isScoped()) {
                throw new ClientException("This client requires specific scopes.");
            }
        } else if (client.isScoped() && !client.getScopes().containsAll(scopes)) {
            throw new ClientException("Invalid set of scopes for this client application.");
        }
        return generateNewApprovalForUser(user, client, scopes, isScoped, providedCode);
    }

    public OAuthApproval generateNewApprovalForUser(IUser user, OAuthClient client, Set<String> scopes, boolean isScoped) {
        return generateNewApprovalForUser(user, client, scopes, isScoped, "");
    }

    public OAuthApproval generateNewApprovalForUser(IUser user, OAuthClient client, Set<String> scopes, boolean isScoped, String providedCode) {
        OAuthApproval approval = new OAuthApproval();
        approval.setId(Context.dal().getTickets().nextId());
        if (empty(providedCode) || !client.isAllowProvidedCode()) {
            approval.setCode(approval.getId() + "-" + GeneralUtils.secureRandomToken(30));
        } else {
            approval.setCode(providedCode);
        }
        approval.setCreatedAt(mils());
        approval.setVerified(false);
        approval.setInternalSecret(GeneralUtils.secureRandomToken(30));
        approval.setScopes(or(scopes, set()));
        approval.setScoped(isScoped);
        return createOrUpdateApproval(approval, user, client);
    }

    protected OAuthApproval createOrUpdateApproval(OAuthApproval approval, IUser user, OAuthClient client) {

        approval.setRefreshToken(GeneralUtils.secureRandomToken(30));




        if (!approval.isScoped() && client.isScoped()) {
            throw new ClientException("You must set specific scopes for this approval");
        } else if (approval.isScoped()) {
            if (!client.getScopes().containsAll(approval.getScopes())) {
                throw new ClientException("This client cannot grant the given scopes");
            }
        }

        approval.setAccessTokenExpiresAt(
                mils() + (or(client.getAccessTokenValiditySeconds(), Settings.instance().getoAuth().getAccessTokenValidSeconds()) * 1000)
        );
        approval.setRefreshTokenExpiresAt(
                mils() + (or(client.getRefreshTokenValiditySeconds(), Settings.instance().getoAuth().getRefreshTokenValidSeconds()) * 1000)
        );

        OAuthAccesTokenData tokenData = new OAuthAccesTokenData()
                .setExpires(approval.getAccessTokenExpiresAt())
                .setUserId(user.getId())
                .setApprovalId(approval.getId())
                .setClientId(client.getId())
                .setScoped(approval.isScoped())
                .setScopes(approval.getScopes())
                ;

        String token = user.getId() + "-" + Encrypter.encryptString(user.getEncryptionSecret(), JSON.stringify(tokenData));
        approval.setAccessToken(token);
        approval.setUserId(user.getId());
        approval.setClientId(client.getFullClientId());
        save(approval);
        return approval;
    }

    public OAuthApproval newAccessTokenForRefreshToken(String refreshToken, String accessToken, String fullClientId, String fullClientSecret) {
        OAuthClient client = OAuthClientController.instance().clientForFullId(fullClientId);
        if (client.getClientSecret().equals(fullClientSecret)) {
            throw new ClientException("Invalid client secret");
        }
        OAuthUserLogin login = tokenToUser(accessToken, true);
        OAuthApproval approval = forId(login.getApprovalId());
        if (approval.isRevoked()) {
            throw new ClientException("Approval has been revoked. You will need to re-authorize");
        }
        if (!refreshToken.equals(approval.getRefreshToken())) {
            throw new ClientException("Invalid refresh token");
        }
        return createOrUpdateApproval(approval, login.getUser(), client);
    }

}
