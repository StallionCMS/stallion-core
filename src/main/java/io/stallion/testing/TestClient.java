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

import java.util.Map;


public class TestClient {
    private AppContextLoader app;
    public TestClient() {
        this.app = AppContextLoader.instance();
    }
    public TestClient(AppContextLoader app) {
        this.app = app;
    }

    public MockResponse get(String path) {
        MockRequest request = new MockRequest(path, "GET");
        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockResponse post(String path, Object dataObject) {
        MockRequest request = new MockRequest(path, "POST").setDataObject(dataObject);
        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockResponse put(String path, Object dataObject) {
        MockRequest request = new MockRequest(path, "PUT").setDataObject(dataObject);
        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockResponse post(String path, Map<String, Object> data) {
        MockRequest request = new MockRequest(path, "POST").setData(data);
        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockResponse put(String path, Map<String, Object> data) {
        MockRequest request = new MockRequest(path, "POST").setData(data);
        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }

    public MockRequest build(String path, String method) {
        MockRequest request = new MockRequest(path, method);
        return request;
    }

    public MockResponse request(StRequest request) {
        MockResponse response = new MockResponse();
        RequestHandler.instance().handleStallionRequest(request, response);
        return response;
    }
}
