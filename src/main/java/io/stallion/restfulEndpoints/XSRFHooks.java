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

package io.stallion.restfulEndpoints;

import io.stallion.hooks.HookRegistry;
import io.stallion.requests.PostRequestHookHandler;
import io.stallion.requests.StRequest;
import io.stallion.requests.StResponse;

import javax.servlet.http.Cookie;

import java.util.UUID;

import static io.stallion.utils.Literals.*;


public class XSRFHooks extends PostRequestHookHandler {
    private static final String COOKIE_NAME = "XSRF-TOKEN";
    private static final String HEADER_NAME = "X-XSRF-TOKEN";

    public static boolean checkXsrfAllowed(StRequest request, RestEndpointBase endpoint) {
        if (!endpoint.shouldCheckXSRF()) {
            return true;
        }
        Cookie cookie = request.getCookie(COOKIE_NAME);
        if (cookie == null || empty(cookie.getValue())) {
            return false;
        }
        String header = request.getHeader(HEADER_NAME);
        if (empty(header)) {
            return false;
        }
        return header.equals(cookie.getValue());
    }

    public static void register() {
        HookRegistry.instance().register(new XSRFHooks());
    }

    @Override
    public void handleRequest(StRequest request, StResponse response) {
        Cookie cookie = request.getCookie(COOKIE_NAME);
        if (cookie != null && !empty(cookie.getValue())) {
            return;
        }
        response.addCookie(COOKIE_NAME, UUID.randomUUID().toString(), 20 * 365 * 86400);
    }
}
