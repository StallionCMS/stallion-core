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

import io.stallion.plugins.javascript.Sandbox;
import io.stallion.users.IOrg;
import io.stallion.users.IUser;

import javax.servlet.http.Cookie;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import static io.stallion.utils.Literals.*;


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
    public Map<String, Object> getBodyMap() {
        return request.getBodyMap();
    }

    @Override
    public Object getBodyParam(String name) {
        return request.getBodyParam(name);
    }

    @Override
    public Map<String, String> getQueryParams() {
        return request.getQueryParams();
    }

    @Override
    public Cookie[] getCookies() {
        List<Cookie> cookies = Arrays.asList(request.getCookies());
        cookies = filter(cookies, cookie -> {
            if (sandbox.getWhitelist().getCookies().contains(cookie.getName())) {
                return true;
            }
            return false;
        });
        Cookie[] cookieArray = cookies.toArray(new Cookie[cookies.size()]);
        return cookieArray;
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
    public void setPath(String path) {

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
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getContent() {
        return request.getContent();
    }

    @Override
    public Enumeration<String> getHeaderNames() {
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
    public String getParameter(String paramName) {
        return request.getParameter(paramName);
    }

    @Override
    public Boolean getIsJsonRequest() {
        return request.getIsJsonRequest();
    }

    @Override
    public void setIsJsonRequest(Boolean isJsonRequest) {

    }

    @Override
    public void setQuery(String query) {

    }

    @Override
    public Map<String, Object> getItems() {
        return request.getItems();
    }

    @Override
    public void setItems(Map<String, Object> items) {

    }

    public SandboxedRequest getSandboxedRequest(Sandbox box) {
        return this;
    }

    @Override
    public Set<String> getScopes() {
        return request.getScopes();
    }

    @Override
    public StRequest setScopes(Set<String> scopes) {
        return null;
    }

    @Override
    public boolean isScoped() {
        return request.isScoped();
    }

    @Override
    public StRequest setScoped(boolean scoped) {
        return null;
    }
}
