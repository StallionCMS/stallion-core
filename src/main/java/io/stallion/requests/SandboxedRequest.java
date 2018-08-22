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


import io.stallion.users.IOrg;
import io.stallion.users.IUser;

import javax.ws.rs.core.Cookie;
import java.util.Set;


public class SandboxedRequest implements IRequest {
    private IRequest request;
    private Sandbox sandbox;

    public SandboxedRequest(Sandbox sandbox, IRequest request) {
        this.sandbox = sandbox;
        this.request = request;
    }

    @Override
    public String requestUrl() {
        return request.requestUrl();
    }



    @Override
    public String getScheme() {
        return request.getScheme();
    }


    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public String getQueryParam(String name) {
        return request.getQueryParam(name);
    }

    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    @Override
    public String getActualIp() {
        return request.getActualIp();
    }

    @Override
    public Object getBodyObject(Class clazz) {
        return request.getBodyObject(clazz);
    }




    @Override
    public Cookie getCookie(String cookieName) {
        if (sandbox.getWhitelist().getCookies().contains(cookieName)) {
            return request.getCookie(cookieName);
        }
        return null;
    }

    @Override
    public String getPath() {
        return request.getPath();
    }


    @Override
    public String getHeader(String name) {
        if (sandbox.getWhitelist().getHeaders().contains(name)) {
            return request.getHeader(name);
        }
        return "";
    }

    @Override
    public String getHost() {
        return request.getHost();
    }


    @Override
    public Iterable<String> getHeaderNames() {
        return request.getHeaderNames();
    }

    @Override
    public IUser getUser() {
        return null;
    }

    @Override
    public void setUser(IUser user) {

    }

    @Override
    public IOrg getOrg() {
        return null;
    }

    @Override
    public void setOrg(IOrg org) {

    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }



    @Override
    public Boolean getIsJsonRequest() {
        return request.getIsJsonRequest();
    }

    @Override
    public void setIsJsonRequest(Boolean isJsonRequest) {

    }

    public SandboxedRequest getSandboxedRequest(Sandbox box) {
        return this;
    }

    @Override
    public Set<String> getScopes() {
        return request.getScopes();
    }

    @Override
    public SandboxedRequest setScopes(Set<String> scopes) {
        return null;
    }

    @Override
    public boolean isScoped() {
        return request.isScoped();
    }

    @Override
    public SandboxedRequest setScoped(boolean scoped) {
        return null;
    }


    @Override
    public Long getValetUserId() {
        return null;
    }

    @Override
    public String getValetEmail() {
        return null;
    }

    @Override
    public String getBodyString() {
        return null;
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public void setProperty(String name, Object obj) {

    }

    @Override
    public String getQueryParam(String name, String defaultValue) {
        return request.getQueryParam(name);
    }
}
