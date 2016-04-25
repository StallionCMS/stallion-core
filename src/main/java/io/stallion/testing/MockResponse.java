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

package io.stallion.testing;

import io.stallion.requests.StResponse;
import io.stallion.utils.json.JSON;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

public class MockResponse<T> extends StResponse {

    private String contentType;
    private int status = 0;
    private int contentLength = 0;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private Map<String, String> headers;
    private ServletOutputStream outputStream;
    private List<Cookie> cookies = list();
    private boolean hasByteOutput = false;

    public MockResponse() {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        headers = new HashMap<String, String>();
        outputStream = new MockOutputStream();
    }

    public StResponse setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public StResponse setStatus(int sc) {
        this.status = sc;
        return this;
    }

    public int getStatus() {
        return this.status;
    }

    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    public String getContent() {
        String out = outputStream.toString();
        if (!empty(out)) {
            return out;
        }
        return stringWriter.toString();
    }

    public StResponse addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    public StResponse setDateHeader(String name, Long value) {
        addHeader(name, value.toString());
        return this;
    }

    public void setContentLength(int length) {
        this.contentLength = length;
    }

    public T asObject(Class<? extends T> cls) {
        try {
            return JSON.parse(this.getContent(), cls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map asMap() {
        return JSON.parseMap(getContent());
    }

    public String getHeader(String name) {
        return headers.getOrDefault(name, null);
    }

    public Cookie addCookie(Cookie cookie) {
        cookies.add(cookie);
        return cookie;
    }

    public Cookie getCookie(String name) {
        for (Cookie cookie: cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

}
