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

import javax.persistence.Column;
import java.util.Map;
import java.util.Set;

import static io.stallion.utils.Literals.map;
import static io.stallion.utils.Literals.or;
import static io.stallion.utils.Literals.set;

/**
 * A dummy request object that is put in the context for the duration of executing
 * an Async task, to prevent getting null reference errors.
 *
 */
public class TaskRequest implements IRequest {
    private IOrg org;
    private IUser user;
    private Map<String, Object> items = map();
    private String name = "";
    private Long taskId = 0L;
    private String customKey = "";

    @Override
    public String requestUrl() {
        return "";
    }

    @Override
    public String getScheme() {
        return "stallion-async-task://";
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
        return "/" + getName() + "?taskId=" + taskId + "&customKey=" + customKey;
    }



    @Override
    public String getHost() {
        return or(System.getenv("STALLION_HOST"), "");
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
        return "ASYNC_TASK";
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

    public String getName() {
        return name;
    }

    public TaskRequest setName(String name) {
        this.name = name;
        return this;
    }

    public Long getTaskId() {
        return taskId;
    }

    public TaskRequest setTaskId(Long taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getCustomKey() {
        return customKey;
    }

    public TaskRequest setCustomKey(String customKey) {
        this.customKey = customKey;
        return this;
    }
}
