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

import io.stallion.exceptions.UsageException;
import io.stallion.monitoring.InternalEndpoints;
import io.stallion.sitemaps.SiteMapEndpoints;
import io.stallion.services.Log;

import java.util.ArrayList;
import java.util.List;


public class EndpointsRegistry {
    private ArrayList<RestEndpointBase> endpoints = new ArrayList<>();

    private static EndpointsRegistry _instance;

    public static EndpointsRegistry instance() {
        if (_instance == null) {
            throw new UsageException("You must call endpoint load before instance() is called.");
        }
        return _instance;
    }

    public static EndpointsRegistry load() {
        _instance = new EndpointsRegistry();
        registerDefaultEndpoints();
        return _instance;
    }

    public static void registerDefaultEndpoints() {
        _instance.addResource("", new InternalEndpoints());
        //_instance.addResource("", new SiteMapEndpoints());
        SiteMapEndpoints.registerEndpoints();
    }

    public static void shutdown() {
        _instance = null;
    }



    public EndpointsRegistry addResource(String basePath, Object ...resources) {
        ResourceToEndpoints converter = new ResourceToEndpoints(basePath);
        for (Object resource: resources) {
            Log.finer("Register resource {0}", resource.getClass().getName());
            List<? extends RestEndpointBase> endPoints = converter.convert(resource);
            addEndpoints(endPoints.toArray(new RestEndpointBase[endPoints.size()]));
        }
        return this;
    }

    public EndpointsRegistry addEndpoints(RestEndpointBase...newEndpoints) {
        for(RestEndpointBase endpoint: newEndpoints) {
            if (endpoints.contains(endpoint)) {
                endpoints.remove(endpoint);
            }
            Log.fine("Adding endpoint {0} {1}", endpoint.getRoute(), endpoint.getClass());
            endpoints.add(endpoint);
        }
        return this;
    }


    public List<RestEndpointBase> getEndpoints() {
        return endpoints;
    }
}
