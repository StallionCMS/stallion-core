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

import javax.servlet.http.Cookie;


public class SandboxedResponse {
    private StResponse response;

    public SandboxedResponse(StResponse response) {
        this.response = response;
    }

    public SandboxedResponse setContentType(String contentType) {
        response.setContentType(contentType);
        return this;
    }

    public int getStatus() {
        return response.getStatus();
    }

    public SandboxedResponse setStatus(int sc) {
        response.setStatus(sc);
        return this;
    }


    public SandboxedResponse addHeader(String name, String value) {
        response.addHeader(name, value);
        return this;
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

}
