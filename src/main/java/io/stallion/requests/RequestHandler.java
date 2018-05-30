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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.stallion.Context;
import io.stallion.settings.SecondaryDomain;
import io.stallion.settings.Settings;
import io.stallion.services.Log;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;


import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;

/**
 * The implementation of the Jetty request handler to handle all HTTP
 * requests for Stallion.
 */
public class RequestHandler extends AbstractHandler {


    private static RequestHandler _instance;

    public static RequestHandler instance() {
        if (_instance == null) {
            _instance = new RequestHandler();
        }
        return _instance;
    }


    public void handle(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        if (path.startsWith("/st-wsroot")) {
            return;
        }
        StRequest stRequest = new StRequest(path, baseRequest, request);
        StResponse stResponse = new StResponse(response);
        handleStallionRequest(stRequest, stResponse);
        baseRequest.setHandled(true);
        if (response.getStatus() >= 400) {
            Log.info("ErrorCode={0} url={1}", response.getStatus(), stRequest.requestUrl());
        }
    }

    public void handleStallionRequest(StRequest stRequest, StResponse stResponse) {
        Context.setRequest(stRequest);
        Context.setResponse(stResponse);
        rewriteRequest(stRequest);
        RequestProcessor requestProcessor = new RequestProcessor(stRequest, stResponse);
        requestProcessor.process();
    }

    public void rewriteRequest(StRequest request) {
        String path = request.getPath();

        // Check for secondary domain mappings
        if (!path.startsWith("/st-")) { // Don't re-map internal endpoints and asset endpoints.
            if (Settings.instance().getSecondaryDomains().size() > 0) {
                for (SecondaryDomain domain : Settings.instance().getSecondaryDomains()) {
                    if (request.getHost().equals(domain.getDomain()) && domain.isStripRootFromPageSlug()) {
                        path = domain.getRewriteRoot() + path;
                        Log.fine("SecondaryDomain rewrite {0} to {1}", request.getPath(), path);
                        request.setPath(path);
                        break;
                    }
                }
            }
        }

        if (Settings.instance().getRewrites().containsKey(path)) {
            path = Settings.instance().getRewrites().get(path);
            request.setPath(path);
            Log.fine("Non regex rewrite {0} to {1}", path, request.getPath());
            return;
        }

        String fullPath = "";
        String query = null;
        if (!empty(request.getQueryString())) {
            fullPath = path + "?" + request.getQueryString();
        } else {
            fullPath = path;
        }

        if (Settings.instance().getRewriteCompiledPatterns().size() > 0) {
            for(Map.Entry<Pattern, String> entry: Settings.instance().getRewriteCompiledPatterns()) {
                Matcher matcher = null;
                boolean matchedQuery = false;
                if (entry.getKey().toString().contains("\\?")) {
                    matcher = entry.getKey().matcher(fullPath);
                    matchedQuery = true;
                } else {
                    matcher = entry.getKey().matcher(path);
                }
                if (matcher.matches()) {
                    Log.fine("Regex matched: {0}", entry.getKey());
                    String newPath = matcher.replaceAll(entry.getValue());
                    if (newPath.contains("?")) {
                        String[] parts = newPath.split("\\?", 2);
                        request.setPath(parts[0]);
                        request.setQuery(parts[1]);
                    } else {
                        request.setPath(newPath);
                    }
                    String newFullPath = request.getPath();
                    if (!empty(request.getQueryString())) {
                        newFullPath += "?" + request.getQueryString();
                    }

                    Log.fine("Rewrote {0} to {1}", fullPath, newFullPath);
                }
            }
        }
    }

}