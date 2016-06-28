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

import io.stallion.services.Log;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class ExceptionInfo {
    private String className;
    private String message;
    private ZonedDateTime thrownAt;
    private String stackTrace;
    private String requestUrl;
    private String requestMethod;
    private Map<String, String> requestHeaders;
    private String requestBody = "";
    private String remoteAddr;
    private String actualIp;

    public static ExceptionInfo newForException(Throwable e) {
        ExceptionInfo info = new ExceptionInfo();
        info.thrownAt = utcNow();
        info.className = e.getClass().getSimpleName();
        info.message = e.getMessage();
        info.stackTrace = ExceptionUtils.getStackTrace(e);
        info.requestUrl = request().requestUrl();
        info.requestMethod = request().getMethod();
        info.remoteAddr = request().getRemoteAddr();
        info.actualIp = request().getActualIp();
        info.requestHeaders = map();
        for(String name: Collections.list(request().getHeaderNames())) {
            info.requestHeaders.put(name, request().getHeader(name));
        }
        try {
            info.setRequestBody(request().getContent());
        } catch (RuntimeException e2) {
            Log.info("Error logging the exception - could not get the request body: {0}", e2);
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

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
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
}
