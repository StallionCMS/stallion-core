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

package io.stallion.monitoring;

import io.stallion.Context;
import io.stallion.requests.TaskRequest;
import io.stallion.services.Log;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.persistence.Column;
import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.Map;

import static io.stallion.Context.request;
import static io.stallion.utils.Literals.map;
import static io.stallion.utils.Literals.utcNow;


public class ExceptionInfo {
    private String className;
    private String message;
    private ZonedDateTime thrownAt;
    private String[] stackTraces;
    private String requestUrl;
    private String requestUrlWithQuery;
    private String requestMethod;
    private Map<String, String> requestHeaders;
    private String requestBody = "";
    private String remoteAddr;
    private String actualIp;
    private String outerMessage = "";
    private String outerClassName = "";
    private String username;
    private String email;
    private String taskDataJson = "";
    private Long userId;
    private Long valetId;

    public static ExceptionInfo newForException(Throwable e) {
        ExceptionInfo info = new ExceptionInfo();
        info.thrownAt = utcNow();
        info.stackTraces = ExceptionUtils.getRootCauseStackTrace(e);
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException)e).getTargetException();
        }
        info.className = e.getClass().getSimpleName();
        info.message = e.getMessage();
        info.requestUrl = request().getRequestUrl();
        info.requestUrlWithQuery = request().getRequestUrlWithQuery();
        info.requestMethod = request().getMethod();
        info.remoteAddr = request().getRemoteAddr();
        info.actualIp = request().getActualIp();
        info.requestHeaders = map();
        for(String name: request().getHeaderNames()) {
            info.requestHeaders.put(name, request().getHeader(name));
        }
        if (request() instanceof TaskRequest) {
            info.taskDataJson = ((TaskRequest) request()).getDataJson();
        }
        /*
        TODO: problem with this is that once we read the body, nothing else can read the body
              maybe only call this if we know we are past the lifecycle where we need to read from
              the entity? if the request is dread?
        try {
            info.setRequestBody(request().getBodyString());
        } catch (RuntimeException e2) {
            Log.info("Error logging the exception - could not get the request body: {0}", e2);
        }
        */
        if (!Context.getUser().isAnon()) {
            info.setEmail(Context.getUser().getEmail());
            info.setUsername(Context.getUser().getUsername());
            info.setUserId(Context.getUser().getId());
            info.setValetId(Context.getValetUserId());
        }
        return info;
    }

    public String getRequestHeadersString() {
        return JSON.stringify(requestHeaders);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ZonedDateTime getThrownAt() {
        return thrownAt;
    }

    public void setThrownAt(ZonedDateTime thrownAt) {
        this.thrownAt = thrownAt;
    }

    public String[] getStackTraces() {
        return stackTraces;
    }

    public ExceptionInfo setStackTraces(String[] stackTraces) {
        this.stackTraces = stackTraces;
        return this;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestUrlWithQuery() {
        return requestUrlWithQuery;
    }

    public ExceptionInfo setRequestUrlWithQuery(String requestUrlWithQuery) {
        this.requestUrlWithQuery = requestUrlWithQuery;
        return this;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public ExceptionInfo setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
        return this;
    }

    public String getActualIp() {
        return actualIp;
    }

    public ExceptionInfo setActualIp(String actualIp) {
        this.actualIp = actualIp;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ExceptionInfo setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ExceptionInfo setEmail(String email) {
        this.email = email;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public ExceptionInfo setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getValetId() {
        return valetId;
    }

    public ExceptionInfo setValetId(Long valetId) {
        this.valetId = valetId;
        return this;
    }


    public String getTaskDataJson() {
        return taskDataJson;
    }

    public ExceptionInfo setTaskDataJson(String taskDataJson) {
        this.taskDataJson = taskDataJson;
        return this;
    }
}
