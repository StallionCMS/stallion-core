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

import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import static io.stallion.utils.Literals.*;

/**
 * A dummy request object that is put in the context for the duration of executing
 * an Async task, to prevent getting null reference errors.
 *
 */
public class TaskRequest implements IRequest {
    private IOrg org;
    private IUser user;
    private Map<String, Object> items = map();

    @Override
    public String requestUrl() {
        return "";
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
    public String getQueryString() {
        return "";
    }


    @Override
    public String getRemoteAddr() {
        return "";
    }

    @Override
    public String getActualIp() { return ""; }

    @Override
    public Object getBodyObject(Class clazz) {
        return null;
    }



    @Override
    public String getPath() {
        return "";
    }



    @Override
    public String getHost() {
        return "";
    }

    @Override
    public String getHeader(String name) {
        return "";
    }


    @Override
    public Iterable<String> getHeaderNames() {
        return null;
    }

    @Override
    public IUser getUser() {
        return this.user;
    }

    @Override
    public void setUser(IUser user) {
        this.user = user;
    }

    @Override
    public IOrg getOrg() {
        return this.org;
    }

    @Override
    public void setOrg(IOrg org) {
        this.org = org;
    }

    @Override
    public String getMethod() {
        return "";
    }


    public String getParameter(String paramName) {
        return "";
    }

    @Override
    public Boolean getIsJsonRequest() {
        return false;
    }

    @Override
    public void setIsJsonRequest(Boolean isJsonRequest) {

    }


    @Override
    public String getQueryParam(String name) {
        return null;
    }

    @Override
    public javax.ws.rs.core.Cookie getCookie(String cookieName) {
        return null;
    }

    public SandboxedRequest getSandboxedRequest(Sandbox box) {
        return new SandboxedRequest(box, this);
    }

    @Override
    public Set<String> getScopes() {
        return set();
    }

    @Override
    public TaskRequest setScopes(Set<String> scopes) {
        return this;
    }

    @Override
    public boolean isScoped() {
        return false;
    }

    @Override
    public TaskRequest setScoped(boolean scoped) {
        return this;
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
        return items.get(name);
    }

    @Override
    public void setProperty(String name, Object obj) {
        items.put(name, obj);
    }

    @Override
    public String getQueryParam(String name, String defaultValue) {
        return null;
    }
}
