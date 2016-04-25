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

package io.stallion.plugins.javascript;

import io.stallion.restfulEndpoints.RestEndpointBase;
import io.stallion.restfulEndpoints.JavascriptRequestHandler;
import io.stallion.restfulEndpoints.RequestArg;

/**
 * Created by pfitzsimmons on 7/26/14.
 *
 EndPoint('route/:post_id', 'POST')
 .bodyArg('state').queryArg('option')
 .requireRole('public')
 .requireXSRFToken(True)
 .handler(function(post_id, state, option) {

 });
 */
public class JsEndpoint extends RestEndpointBase {
    private JavascriptRequestHandler _handler;

    public JsEndpoint() {

    }

    public JsEndpoint(String route) {
        this(route, "GET");
    }

    public JsEndpoint(String route, String method) {
        setRoute(route);
        setMethod(method);
    }

    public JsEndpoint bodyParam(String name) {
        this.getArgs().add(new RequestArg().setName(name).setType("BodyParam"));
        return this;
    }

    public JsEndpoint queryParam(String name) {
        this.getArgs().add(new RequestArg().setName(name).setType("QueryParam"));
        return this;
    }

    public JsEndpoint pathParam(String name) {
        this.getArgs().add(new RequestArg().setName(name).setType("PathParam"));
        return this;
    }

    public JsEndpoint mapParam() {
        this.getArgs().add(new RequestArg().setType("MapParam"));
        return this;
    }

    public JavascriptRequestHandler getHandler() {
        return _handler;
    }

    public JsEndpoint setHandler(JavascriptRequestHandler handler) {
        this._handler = handler;
        return this;
    }

    public JsEndpoint handler(JavascriptRequestHandler handler) {
        setHandler(handler);
        return this;
    }
}
