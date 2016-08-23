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

package io.stallion.testing;

import io.stallion.Context;
import io.stallion.requests.RequestHandler;
import io.stallion.requests.StRequest;
import io.stallion.boot.AppContextLoader;
import io.stallion.restfulEndpoints.XSRFHooks;
import io.stallion.users.IUser;
import io.stallion.users.User;
import io.stallion.users.UserController;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.asArray;
import static io.stallion.utils.Literals.list;


public class TestClient {
    private List<Cookie> cookies = list(new Cookie(XSRFHooks.COOKIE_NAME, "someval"));
    private AppContextLoader app;
    public TestClient() {
        this.app = AppContextLoader.instance();
    }
    public TestClient(AppContextLoader app) {
        this.app = app;
    }

    public TestClient withUser(String email) {
        IUser user = UserController.instance().forEmail(email);
        String val = UserController.instance().userToCookieString(user, false);
        return withCookie(UserController.USER_COOKIE_NAME, val);
    }

    public TestClient withUser(Long userId) {
        IUser user = UserController.instance().forId(userId);
        String val = UserController.instance().userToCookieString(user, false);
        return withCookie(UserController.USER_COOKIE_NAME, val);
    }


    public TestClient withCookie(String name, String value) {
        this.cookies.add(new Cookie(name, value));
        return this;
    }

    public MockResponse get(String path) {
        MockRequest request = new MockRequest(path, "GET");
        request.addHeader(XSRFHooks.HEADER_NAME, "someval");
        request.setCookies(asArray(cookies, Cookie.class));
        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockResponse post(String path, Object dataObject) {
        MockRequest request = new MockRequest(path, "POST").setDataObject(dataObject);
        request.addHeader(XSRFHooks.HEADER_NAME, "someval");
        request.setCookies(asArray(cookies, Cookie.class));


        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockResponse put(String path, Object dataObject) {
        MockRequest request = new MockRequest(path, "PUT").setDataObject(dataObject);
        request.addHeader(XSRFHooks.HEADER_NAME, "someval");
        request.setCookies(asArray(cookies, Cookie.class));

        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockResponse post(String path, Map<String, Object> data) {
        MockRequest request = new MockRequest(path, "POST").setData(data);
        request.addHeader(XSRFHooks.HEADER_NAME, "someval");
        request.setCookies(asArray(cookies, Cookie.class));

        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockResponse put(String path, Map<String, Object> data) {
        MockRequest request = new MockRequest(path, "POST").setData(data);
        request.addHeader(XSRFHooks.HEADER_NAME, "someval");
        request.setCookies(asArray(cookies, Cookie.class));
        request.setCookies(new Cookie(XSRFHooks.COOKIE_NAME, "someval"));

        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockRequest build(String path, String method) {
        MockRequest request = new MockRequest(path, method);
        request.addHeader(XSRFHooks.HEADER_NAME, "someval");
        request.setCookies(asArray(cookies, Cookie.class));
        return request;
    }

    public MockResponse request(StRequest request) {
        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }
}
