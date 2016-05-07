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

import io.stallion.exceptions.ClientException;
import io.stallion.exceptions.RedirectException;
import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.MinRole;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.requests.validators.ParamExtractor;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class OAuthEndpoints {

    @GET
    @Path("/auth")
    @Produces("text/html")
    @MinRole(Role.CONTACT)
    public Object authScreen(@QueryParam("client_id") String clientFullId, @QueryParam("scopes") String scopesString) {
        String[] scopes = StringUtils.split(scopesString, ",");
        String description = "";
        Map<String, String> descriptionMap = Settings.instance().getoAuth().getScopeDescriptions();
        for (int x = 0; x<scopes.length; x++) {
            String scope = scopes[x];
            String s = or(descriptionMap.getOrDefault(scope, scope), scope);
            if (scopes.length == 1) {
                 description = s;
            } else if (scopes.length == 2) {
                if (x == 0) {
                    description += s;
                } else {
                    description += " and " + s;
                }
            } else if (x + 1 == scopes.length) {
                description += " and " + s;
            } else {
                description += "," + s;
            }
        }
        Map<String, Object> ctx = map(val("clientId", clientFullId));
        ctx.put("client", OAuthClientController.instance().clientForFullId(clientFullId));
        ctx.put("scopesDescription", description);
        ctx.put("scopes", set(scopes));
        String html = TemplateRenderer.instance().renderTemplate("stallion:public/oauth.jinja");
        return html;
    }

    @POST
    @Path("/authorize-and-redirect")
    @MinRole(Role.CONTACT)
    @Consumes("application/x-www-form-urlencoded")
    public void authorizeToRedirect(@BodyParam("clientId") String fullClientId, @BodyParam("redirectUri") String redirectUri, @BodyParam("scopes") String scopesString, @BodyParam("state") String state) {
        state = or(state, "");
        boolean scoped = true;
        Set<String> scopes = null;
        if (scopesString == null) {
            scoped = false;
            scopes = set();
        } else {
            scopes = set(or(scopesString, "").split(","));
        }
        String providedCode = request().getBodyMap().getOrDefault("providedCode", "").toString();
        OAuthApproval approval = OAuthApprovalController.instance().checkGrantApprovalForUser(GrantType.CODE, getUser(), fullClientId, scopes, scoped, redirectUri, providedCode);
        if (!redirectUri.contains("?")) {
            redirectUri += "?";
        } else if (!redirectUri.endsWith("&")) {
            redirectUri += "&";
        }
        try {
            redirectUri += "code=" + approval.getCode() + "&state=" + URLEncoder.encode(state, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        ;
        throw new RedirectException(redirectUri, 302);
    }

    @POST
    @Path("/authorize-and-redirect-to-hash")
    @MinRole(Role.CONTACT)
    @Consumes("application/x-www-form-urlencoded")
    public void authorizeToRedirectHash(@BodyParam("clientId") String fullClientId, @BodyParam("redirectUri") String redirectUri, @BodyParam("scopes") String scopesString, @BodyParam("state") String state) {
        state = or(state, "");
        boolean scoped = true;
        Set<String> scopes = null;
        if (scopesString == null) {
            scoped = false;
            scopes = set();
        } else {
            scopes = set(or(scopesString, "").split(","));
        }
        OAuthApproval approval = OAuthApprovalController.instance().checkGrantApprovalForUser(GrantType.TOKEN, getUser(), fullClientId, scopes, scoped, redirectUri);
        if (!redirectUri.contains("#")) {
            redirectUri += "#";
        } else if (!redirectUri.endsWith("&")) {
            redirectUri += "&";
        }
        try {
            redirectUri += "token=" + approval.getAccessToken() + "&state=" + URLEncoder.encode(state, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        throw new RedirectException(redirectUri, 302);
    }

    @POST
    @Path("/authorize-to-json")
    @MinRole(Role.CONTACT)
    public Map<String, Object> authorize(@BodyParam("grantType") String grantTypeString, @BodyParam("clientId") String fullClientId, @BodyParam("redirectUri") String redirectUri, @BodyParam("scopes") String scopesString, @BodyParam("state") String state) {
        GrantType grantType = Enum.valueOf(GrantType.class, grantTypeString.toUpperCase());
        state = or(state, "");
        boolean scoped = true;
        Set<String> scopes = null;
        if (scopesString == null) {
            scoped = false;
            scopes = set();
        } else {
            scopes = set(or(scopesString, "").split(","));
        }
        OAuthApproval approval = OAuthApprovalController.instance().checkGrantApprovalForUser(grantType, getUser(), fullClientId, scopes, scoped, redirectUri);
        Map<String, Object> data = map(val("state", state), val("redirect_uri", redirectUri));
        if (grantType.equals(GrantType.CODE)) {
            data.put("code", approval.getCode());
        } else if (grantType.equals(GrantType.TOKEN)) {
            data.put("access_token", approval.getAccessToken());
        }
        return data;
    }

    @POST
    @Path("/refresh")
    public Object refresh(
            @BodyParam("access_token") String accessToken,
            @BodyParam("refresh_token") String refreshToken,
            @BodyParam("client_id") String fullClientId,
            @BodyParam("client_secret") String clientSecret ) {

        OAuthApproval approval = OAuthApprovalController.instance().newAccessTokenForRefreshToken(refreshToken, accessToken, fullClientId, clientSecret);
        return map(val("access_token", approval.getAccessToken()), val("refresh_token", approval.getRefreshToken()));
    }


    @POST
    @Path("/token")
    public Object grantToken(@BodyParam("grant_type") String grantType) {
        switch(grantType) {
            case "authorization_code":
                return authorizationCodeGrantToken();
            case "password":
                return passwordGrantToken();
            default:
                throw new ClientException("Could not understand grant_type: " + grantType);
        }
    }

    public Object authorizationCodeGrantToken() {
        ParamExtractor<String> params =
                new ParamExtractor(request().getBodyMap(),
                "Required post body parameter {0} was not found.");
        String code = params.get("code");
        String redirectUri = params.get("redirect_uri");
        String fullClientId = params.get("client_id");
        String clientSecret = params.get("client_secret");
        OAuthClient client = OAuthClientController.instance().clientForFullId(fullClientId);
        if (empty(client)) {
            throw new ClientException("Client not found with id :'" + fullClientId + "'");
        }
        if (client.hasGrantType(GrantType.CODE)) {
            throw new ClientException("Client cannot use password login.");
        }
        if (client.isRequiresSecret() && !clientSecret.equals(client.getClientSecret())) {
            throw new ClientException("The client secret was not valid");
        }
        if (!client.getAllowedRedirectUris().contains(redirectUri)) {
            throw new ClientException("The URI '" + redirectUri + "' was not on the allowed list.");
        }
        OAuthApproval token = OAuthApprovalController.instance().forUniqueKey("code", code);
        if (empty(token)) {
            throw new ClientException("No valid token found for code: '" + code + "'");
        }
        if (token.isVerified()) {
            throw new ClientException("Code has already been used: '" + code + "'");
        }
        // Tokens expire in 15 minutes if they are not verified
        if ((token.getCreatedAt() + (15 * 60 * 1000)) < mils() ) {
            throw new ClientException("Code was not verified within fifteen minutes: '" + code + "'");
        }
        token.setVerified(true);
        token.setCode(UUID.randomUUID().toString());
        OAuthApprovalController.instance().save(token);

        return map(
                val("access_token", token.getAccessToken()),
                val("refresh_token", token.getRefreshToken())
        );


    }

    public Object passwordGrantToken() {
        ParamExtractor<String> params =
                new ParamExtractor(request().getBodyMap(),
                        "Required post body paramater {0} was not found.");
        String password = params.get("password");
        String username = params.get("username");
        String clientId = params.get("clientId");
        OAuthClient client = OAuthClientController.instance().forUniqueKey("clientKey", clientId);
        if (empty(client)) {
            throw new ClientException("Client not found with id :'" + clientId + "'");
        }
        if (client.hasGrantType(GrantType.PASSWORD)) {
            throw new ClientException("Client cannot use password login.");
        }
        Set<String> scopes;
        boolean scoped = true;
        if (client.isScoped()) {
            scopes = new HashSet<>(client.getScopes());
            scoped = true;
        } else {
            scopes = set();
            scoped = false;
        }
        IUser user = UserController.instance().checkUserLoginValid(username, password);
        OAuthApproval token = OAuthApprovalController.instance().generateNewApprovalForUser(user, client, scopes, scoped, "");
        return map(val("access_token", token.getAccessToken()));
    }


}
