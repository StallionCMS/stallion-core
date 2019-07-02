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

package io.stallion.requests;

import io.stallion.Context;
import io.stallion.settings.Settings;
import io.stallion.users.EmptyOrg;
import io.stallion.users.EmptyUser;
import io.stallion.users.IOrg;
import io.stallion.users.IUser;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.or;


public class RequestWrapper implements IRequest {
    ContainerRequest request;
    private String scheme;

    public RequestWrapper(ContainerRequestContext requestContext) {
        this.request = (ContainerRequest)requestContext.getRequest();
    }

    public RequestWrapper(ContainerRequest request) {
        this.request = request;
    }

    @Override
    public String requestUrl() {
        String host = getHost();
        String proto = getScheme();
        String baseUrl = proto + "://" + host;
        return baseUrl + this.getPath();
    }

    @Override
    public String getScheme() {
        if (empty(scheme) && this.request != null) {
            if (!empty(getHeader("x-upstream-forwarded-proto"))) {
                scheme = getHeader("x-upstream-forwarded-proto");
            }
            if (empty(scheme) && !empty(getHeader("x-forwarded-proto"))) {
                scheme = getHeader("x-forwarded-proto");
            }
        }
        if (empty(scheme)) {
            String baseUrl = "";
            if (Context.getSettings() != null ) {
                baseUrl = Context.getSettings().getSiteUrl();
            }
            if (baseUrl != null) {
                int i = baseUrl.indexOf("://");
                if (i > -1) {
                    scheme = baseUrl.substring(0, i);
                }
            }
        }
        if (empty(scheme) && this.request != null) {
            scheme = this.request.getUriInfo().getRequestUri().getScheme();
        }
        if (empty(scheme)){
            scheme = "http";
        }
        return scheme;
    }

    @Override
    public int getActualPort() {
        String portHeader = getHeader("X - Forwarded - Port");
        if (!empty(portHeader)) {
            return Integer.parseInt(portHeader);
        } else if (this.request != null && this.request.getUriInfo() != null) {
            return this.request.getUriInfo().getRequestUri().getPort();
        }
        return 80;
    }


    @Override
    public String getQueryString() {
        return request.getRequestUri().getQuery();
    }

    @Override
    public String getRemoteAddr() {
        if (request != null) {
            return or((String)request.getProperty("remoteAddr"), "0.0.0.0");
        } else {
            return "0.0.0.0";
        }
    }

    public RequestWrapper setRemoteAddr(String remoteAddr) {
        request.setProperty("remoteAddr", remoteAddr);
        return this;
    }

    @Override
    public String getHost() {
        if (this.request == null) {
            return "http://localhost" + this.getPath();
        }
        String host = this.request.getUriInfo().getRequestUri().getHost();
        if (!empty(getHeader("x-forwarded-host"))) {
            host = getHeader("x-forwarded-host");
        }
        return host;
    }

    @Override
    public String getActualIp() {
        if (!empty(Settings.instance().getIpHeaderName())) {
            return or(request.getHeaderString(Settings.instance().getIpHeaderName()), getRemoteAddr());
        }
        return getRemoteAddr();
    }

    @Override
    public Object getBodyObject(Class clazz) {
        return request.readEntity(clazz);
    }

    @Override
    public String getBodyString() {
        return request.readEntity(String.class);
    }

    @Override
    public Object getProperty(String name) {
        return request.getProperty(name);
    }

    @Override
    public void setProperty(String name, Object obj) {
        request.setProperty(name, obj);
    }


    @Override
    public Cookie getCookie(String cookieName) {
        if (request.getCookies() != null) {
            return request.getCookies().get(cookieName);
        }
        return null;
    }

    @Override
    public String getPath() {
        return request.getRequestUri().getPath();
    }



    @Override
    public String getHeader(String name) {
        return request.getHeaderString(name);
    }


    @Override
    public Iterable<String> getHeaderNames() {
        return request.getHeaders().keySet();
    }

    @Override
    public String getQueryParam(String name) {
        List<String> val = request.getUriInfo().getQueryParameters().getOrDefault(name, null);
        if (val != null && val.size() > 0) {
            return val.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getQueryParamAsList(String name) {
        return request.getUriInfo().getQueryParameters().getOrDefault(name, Collections.EMPTY_LIST);
    }

    @Override
    public String getQueryParam(String name, String defaultValue) {
        List<String> val = request.getUriInfo().getQueryParameters().getOrDefault(name, null);
        if (val != null && val.size() > 0) {
            return val.get(0);
        } else {
            return defaultValue;
        }
    }

    @Override
    public IUser getUser() {
        IUser user = (IUser)request.getProperty("stallionUser");
        if (user == null) {
            request.setProperty("stallionUser", new EmptyUser());
            user = (IUser)request.getProperty("stallionUser");
        }
        return user;
    }

    @Override
    public void setUser(IUser user) {
        request.setProperty("stallionUser", user);
    }

    @Override
    public IOrg getOrg() {
        IOrg org = (IOrg)request.getProperty("stallionOrg");
        if (org == null) {
            request.setProperty("stallionOrg", new EmptyOrg());
            org = (IOrg)request.getProperty("stallionOrg");
        }
        return org;
    }

    @Override
    public void setOrg(IOrg org) {
        request.setProperty("stallionOrg", org);
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }


    @Override
    public Boolean getIsJsonRequest() {
        return (Boolean)request.getProperty("isJsonRequest");
    }

    @Override
    public void setIsJsonRequest(Boolean isJsonRequest) {
        request.setProperty("isJsonRequest", isJsonRequest);
    }


    @Override
    public SandboxedRequest getSandboxedRequest(Sandbox box) {
        return new SandboxedRequest(box, this);
    }

    @Override
    public Set<String> getScopes() {
        return (Set<String>)request.getProperty("stallionOAuthScopes");
    }

    @Override
    public IRequest setScopes(Set<String> scopes) {
        request.setProperty("stallionOAuthScopes", scopes);
        return this;
    }

    @Override
    public boolean isScoped() {
        Object prop = request.getProperty("isScoped");
        if (prop != null) {
            return (boolean)prop;
        } else {
            return false;
        }
    }

    @Override
    public IRequest setScoped(boolean scoped) {
        request.setProperty("isScoped", scoped);
        return this;
    }

    @Override
    public Long getValetUserId() {
        return (Long)request.getProperty("valetUserId");
    }

    @Override
    public String getValetEmail() {
        return (String) request.getProperty("valetUserEmail");
    }

    @Override
    public void setValetUserId(Long valetUserId) {
        request.setProperty("valetUserId", valetUserId);
    }

    @Override
    public void setValetEmail(String email) {
        request.setProperty("valetUserEmail", email);
    }

}
