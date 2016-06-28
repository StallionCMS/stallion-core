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

package io.stallion;

import io.stallion.boot.AppContextLoader;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.db.DB;
import io.stallion.plugins.PluginRegistry;
import io.stallion.requests.*;
import io.stallion.restfulEndpoints.RestEndpointBase;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.users.*;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.emptyInstance;

/**
 * A static helper class providing short-cut access to the most common services and data objects running
 * in the current application and request context.
 *
 * The static methods from this class are imported by default into all new source code files.
 *
 */
public class Context {
    private static final ThreadLocal<StRequest> _request = new ThreadLocal<StRequest>();
    private static final ThreadLocal<StResponse> _response = new ThreadLocal<StResponse>();


    public static AppContextLoader app() {
        return AppContextLoader.instance();
    }

    public static DB db() {
        return DB.instance();
    }
    public static DB getDb() {
        return DB.instance();
    }

    public static UserController getUserController() {
        return UserController.instance();
    }
    public static UserController userController() {
        return UserController.instance();
    }

    public static TemplateRenderer templateRenderer() {
        return TemplateRenderer.instance();
    }
    public static TemplateRenderer getTemplateRenderer() {
        return TemplateRenderer.instance();
    }

    public static PluginRegistry pluginRegistry() {
        return PluginRegistry.instance();
    }

    public static PluginRegistry getPluginRegistry() {
        return PluginRegistry.instance();
    }

    public static DataAccessRegistry dal() { return DataAccessRegistry.instance(); }
    public static DataAccessRegistry getDal() { return DataAccessRegistry.instance(); }

    public static DataAccessRegistry dataAccess() { return DataAccessRegistry.instance(); }
    public static DataAccessRegistry getDataAccess() { return DataAccessRegistry.instance(); }

    /**
     * Alias for getRequest();
     * @return
     */
    public static IRequest request() {
        if (_request.get() == null) {
            return new EmptyRequest();
        }
        return (StRequest)_request.get();
    }

    /**
     * The current request, as stashed on a thread local variable. Returns a new EmptyRequest() object
     * if called from outside a web request
     * @return
     */
    public static IRequest getRequest() {
        return request();
    }

    /**
     * Alias for getResponse();
     * @return
     */
    public static StResponse response() {
        if (_response.get() == null) {
            return new EmptyResponse();
        }
        return (StResponse)_response.get();

    }

    /**
     * The current response, as stashed on a thread local variable. Returns a new EmptyResponse() object
     * if called from outside a web request
     * @return
     */
    public static StResponse getResponse() {
        return response();
    }

    /**
     * Determines if the currently active user, based on the current active request, is allowed
     * to access the passed in endpoint.
     *
     * For a scoped, endpoint request, it will check the scope.
     *
     * For all other requests, it will check to see if the endpoint requires a mininum role, and
     * if the user has that role.
     *
     * @param endpoint
     * @return
     */
    public static boolean currentUserCanAccessEndpoint(RestEndpointBase endpoint) {
        // Scoped request
        if (request().isScoped()) {
            // Endpoint not scoped, deny to scoped requests
            if (!endpoint.isScoped()) {
                return false;
            }
            // Endpoint has scope that the current user does not, deny
            if (!"any".equals(endpoint.getScope()) && request().getScopes().contains(endpoint.getScope())) {
                return false;
            }
        }
        if (!emptyInstance(endpoint.getRole())) {
            // User does not have role, deny
            if (!getUser().isInRole(endpoint.getRole())) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public static AppContextLoader getApp() {
        return app();
    }

    public static void setRequest(StRequest request) {
        _request.set(request);
    }

    public static void setResponse(StResponse response) {
        _response.set(response);
    }

    public static Settings settings() {
        return getSettings();
    }

    public static Settings getSettings() {
        if (Settings.isNull()) {
            return new Settings();
        } else {
            return Settings.instance();
        }
    }



    public static void resetThreadContext() {
        setUser(new EmptyUser());
        setOrg(new EmptyOrg());
    }

    public static void setUser(IUser user) {
        _request.get().setUser(user);
    }

    public static void setOrg(IOrg org) {
        _request.get().setOrg(org);
    }

    public static IUser getUser() {
        return request().getUser();
    }

    public static IOrg getOrg() {
        return request().getOrg();
    }



}

