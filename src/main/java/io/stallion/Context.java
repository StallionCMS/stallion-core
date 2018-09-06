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

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.db.DB;
import io.stallion.plugins.PluginRegistry;
import io.stallion.requests.EmptyRequest;
import io.stallion.requests.IRequest;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.users.*;

/**
 * A static helper class providing short-cut access to the most common services and data objects running
 * in the current application and request context.
 *
 * The static methods from this class are imported by default into all new source code files.
 *
 */
public class Context {
    private static final ThreadLocal<IRequest> _request = new ThreadLocal<IRequest>();



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
        return (IRequest) _request.get();
    }

    /**
     * The current request, as stashed on a thread local variable. Returns a new EmptyRequest() object
     * if called from outside a web request
     * @return
     */
    public static IRequest getRequest() {
        return request();
    }




    public static void setRequest(IRequest request) {
        _request.set(request);
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

    public static String getValetEmail() {
        return request().getValetEmail();
    }

    public static Long getValetUserId() {
        return request().getValetUserId();
    }

    public static void setValet(Long valetUserId, String valetEmail) {
        if (_request.get() != null) {
            _request.get().setValetUserId(valetUserId);
            _request.get().setValetEmail(valetEmail);
        }

    }

    public static void setUser(IUser user) {
        if (_request.get() != null) {
            _request.get().setUser(user);
        }
    }

    public static void setOrg(IOrg org) {
        if (_request.get() != null) {
            _request.get().setOrg(org);
        }
    }

    public static IUser getUser() {
        IUser user = request().getUser();
        if (user == null) {
            return new EmptyUser();
        }
        return user;
    }

    public static IOrg getOrg() {
        IOrg org = request().getOrg();
        if (org == null) {
            return new EmptyOrg();
        }
        return org;
    }



}

