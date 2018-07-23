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

import io.stallion.Context;
import io.stallion.exceptions.UsageException;
import io.stallion.restfulEndpoints.RestEndpointBase;
import io.stallion.services.Log;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry of all routes defined in the stallion.toml settings file.
 *
 */
public class RoutesRegistry {
    private List<RouteDefinition> routes = new ArrayList<>();

    private static RoutesRegistry _instance;

    public static RoutesRegistry instance() {
        if (_instance == null) {
            throw new UsageException("You must call RoutesRegistry.load() before accessing the instance()");
        }
        return _instance;
    }

    public static RoutesRegistry load() {
        _instance = new RoutesRegistry();
        for(RouteDefinition route: Context.settings().getRoutes()) {
            _instance.getRoutes().add(route);
        }
        return _instance;
    }


    public RouteResult routeForEndpoints(StRequest request, List<RestEndpointBase> endpoints) {

        for (RestEndpointBase endpoint: endpoints) {
            Log.finest("EndpointMethod: {0} RequestMethod: {1}", endpoint.getMethod(), request.getMethod());
            Log.finest("EndpointRoute: {0} RequestPath: {1}", endpoint.getRoute(), request.getPath());

            if (!endpoint.getMethod().equals(request.getMethod())) {
                Log.finest("method does not match");
                continue;
            }


            // Catch-all
            if (endpoint.getRoute().equals("*")) {
                RouteResult result = new RouteResult()
                        .setEndpoint(endpoint);
                return result;
            }


            Log.finest("Mapping pathParams");
            String requestPath = request.getPath();
            String[] requestParts = request.getPath().split("/");
            String endpointPath = endpoint.getRoute();
            String[] endpointParts = endpointPath.split("/");

            // If the last part endpoint is a wildcard/match-all, then we truncate the request parts to
            // all the path sections before the wildcard, and ignore all parts after the wildcard.
            if (endpointParts.length > 0) {
                if (endpointParts[endpointParts.length - 1].equals("*") && requestParts.length >= endpointParts.length) {
                    requestParts = ArrayUtils.subarray(requestParts, 0, endpointParts.length - 1);
                }
            }


            HashMap<String, String> pathParams = urlToParamsMap(requestParts, endpointParts);
            if (pathParams != null) {
                RouteResult result = new RouteResult()
                        .setParams(pathParams)
                        .setEndpoint(endpoint);
                return result;
            } else {
                Log.finest("pathParams do not match");
            }
        }
        return null;
    }

    public RouteResult route(String path) {
        if ("".equals(path)) {
            path = "/";
        }
        for(RouteDefinition definition: routes) {
            HashMap<String, String> pathParams = urlToParamsMap(path.split("/"), definition.getRoute().split("/"));
            if (pathParams != null) {
                //val pathParamsNotNull:Map<String, String> = pathParams;

                for(Map.Entry<String, String> entry: definition.getParams().entrySet()) {
                    pathParams.put(entry.getKey(), entry.getValue());
                }
                RouteResult result = new RouteResult()
                        .setTemplate(definition.getTemplate())
                        .setGroup(definition.getGroup())
                        .setParams(pathParams)
                        .setPageTitle(definition.getPageTitle())
                        .setMetaDescription(definition.getMetaDescription())
                        .setRedirectUrl(definition.getDestination())
                        .setPreempt(definition.getPreempt())
                        .setName(definition.getName())
                        ;
                return result;
            }
        }
        return null;
    }

    /**
     *
     * @param givenUrlSegments An array representing the URL path attempting to be opened (i.e. ["users", "42"])
     * @param routerUrlSegments An array representing a possible URL match for the router (i.e. ["users", ":id"])
     * @return A map of URL parameters if it's a match (i.e. {"id" =&gt; "42"}) or null if there is no match
     */
    public HashMap<String, String> urlToParamsMap(String[] givenUrlSegments,String[] routerUrlSegments) {
        HashMap<String, String> formatParams = new HashMap<String, String>();
        if (routerUrlSegments.length != givenUrlSegments.length) {
            return null;
        }
        for (int index=0; index<routerUrlSegments.length; index++) {
            String routerPart = routerUrlSegments[index];
            if (index >= givenUrlSegments.length) {
                return null;
            }
            String givenPart = givenUrlSegments[index];
            if (routerPart.length() == 0) {
                continue;
            }
            if (routerPart.contains("{") && routerPart.contains("}")) {
                int s = routerPart.indexOf("{");
                int e = routerPart.indexOf("}");
                String key = routerPart.substring(s + 1, e);
                formatParams.put(key, givenPart);
                continue;
            } else {
                if (routerPart.charAt(0) == ':') {
                    String key = routerPart.substring(1, routerPart.length());
                    formatParams.put(key, givenPart);
                    continue;
                }
            }

            if (!routerPart.equals(givenPart)) {
                return null;
            }
        }
        return formatParams;
    }


    public static void shutdown() {
        _instance = null;
    }


    public List<RouteDefinition> getRoutes() {
        return routes;
    }
}
