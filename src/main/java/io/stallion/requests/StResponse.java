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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static io.stallion.utils.Literals.empty;

public class StResponse {
    private HttpServletResponse response;
    private MetaInformation meta = new MetaInformation();
    private PerPageLiterals pageFooterLiterals = new PerPageLiterals();
    private PerPageLiterals pageHeadLiterals = new PerPageLiterals();
    private SandboxedResponse sandboxedResponse;
    private String contentType = "";


    public StResponse() {

    }

    public StResponse(HttpServletResponse response) {
        this.response = response;
    }


    public HttpServletResponse getHttpServletResponse() {
        return this.response;
    }


    public StResponse setDefaultContentType(String contentType) {
        response.setContentType(contentType);
        return this;
    }

    public boolean isContentTypeSet() {
        return !empty(contentType);
    }

    public StResponse setContentType(String contentType) {
        this.contentType = contentType;
        response.setContentType(contentType);
        return this;
    }

    public StResponse setStatus(int sc) {
        response.setStatus(sc);
        return this;
    }

    public int getStatus() {
        return response.getStatus();
    }

    public PrintWriter getWriter() throws IOException {
        return response.getWriter();
    }

    public StResponse addHeader(String name, String value) {
        response.addHeader(name, value);
        return this;
    }

    public ServletOutputStream getOutputStream() throws IOException{
        return response.getOutputStream();
    }

    public StResponse setDateHeader(String name, Long value) {
        response.setDateHeader(name, value);
        return this;
    }

    public void setContentLength(int length) {
        response.setContentLength(length);
    }

    public String getHeader(String name) {
        return response.getHeader(name);
    }

    public String getContentType() {
        return getHeader("Content-type");
    }

    public Cookie addCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        addCookie(cookie);
        return cookie;
    }

    public Cookie addCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        addCookie(cookie);
        return cookie;
    }

    public Cookie addCookie(Cookie cookie) {
        response.addCookie(cookie);
        return cookie;
    }

    public MetaInformation getMeta() {
        return meta;
    }

    public void setMeta(MetaInformation meta) {
        this.meta = meta;
    }


    public PerPageLiterals getPageFooterLiterals() {
        return pageFooterLiterals;
    }

    public PerPageLiterals getPageHeadLiterals() {
        return pageHeadLiterals;
    }


    public SandboxedResponse getSandboxedResponse() {
        if (sandboxedResponse == null) {
            sandboxedResponse = new SandboxedResponse(this);
        }
        return sandboxedResponse;
    }
}
