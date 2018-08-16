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

package io.stallion.jerseyProviders;

import io.stallion.requests.IRequest;
import io.stallion.requests.RequestWrapper;
import io.stallion.services.Log;
import io.stallion.settings.SecondaryDomain;
import io.stallion.settings.Settings;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import static io.stallion.utils.Literals.empty;


@Priority(700)
@PreMatching
@Provider
public class InternalRewriteRequestFilter  implements ContainerRequestFilter {

    @javax.ws.rs.core.Context
    private HttpServletRequest httpRequest;

    @javax.ws.rs.core.Context
    private HttpServletResponse httpResponse;



    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        rewriteSecondaryDomains(containerRequestContext);
        rewriteRequest(containerRequestContext);
        // redirect conditionally
        //if (shouldRedirect(reqContext)) {
        //    reqContext.setRequestUri(URI.create("/temp"));
        //}
    }


    private void rewriteSecondaryDomains(ContainerRequestContext containerRequestContext) {
        String orgPath = containerRequestContext.getUriInfo().getRequestUri().getPath();
        String host = new RequestWrapper(containerRequestContext).getHost();
        if (!orgPath.startsWith("/st-")) { // Don't re-map internal endpoints and asset endpoints.
            if (Settings.instance().getSecondaryDomains().size() > 0) {
                for (SecondaryDomain domain : Settings.instance().getSecondaryDomains()) {
                    if (host.equals(domain.getDomain()) && domain.isStripRootFromPageSlug()) {
                        String newPath = domain.getRewriteRoot() + orgPath;
                        Log.fine("SecondaryDomain rewrite {0} to {1}", orgPath, newPath);
                        containerRequestContext.setRequestUri(URI.create(newPath));
                        //request.setPath(path);
                        break;
                    }
                }
            }
        }
    }


    public void rewriteRequest(ContainerRequestContext containerRequestContext) {
        IRequest request = new RequestWrapper(containerRequestContext);
        String path = containerRequestContext.getUriInfo().getRequestUri().getPath();


        if (Settings.instance().getRewrites().containsKey(path)) {
            String newPath = Settings.instance().getRewrites().get(path);
            containerRequestContext.setRequestUri(URI.create(newPath));
            Log.fine("Non regex rewrite {0} to {1}", path, newPath);
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
                    URI newUri;
                    if (newPath.contains("?")) {
                        String[] parts = newPath.split("\\?", 2);

                        //request.setPath(parts[0]);
                        //request.setQuery(parts[1]);
                        newUri = URI.create(parts[0] + "?" + parts[1]);
                        containerRequestContext.setRequestUri(newUri);
                    } else {
                        //request.setPath(newPath);
                        newUri = URI.create(newPath);
                        containerRequestContext.setRequestUri(newUri);
                    }
                    String newFullPath = request.getPath();
                    if (!empty(request.getQueryString())) {
                        newFullPath += "?" + request.getQueryString();
                    }

                    Log.fine("Rewrote {0} to {1}", fullPath, newUri);
                }
            }
        }
    }
}
