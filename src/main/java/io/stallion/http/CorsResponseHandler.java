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

package io.stallion.http;

import io.stallion.requests.IRequest;
import io.stallion.requests.ResponseComplete;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.CorsSettings;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;

@Priority(FilterPriorities.CORS_RESPONSE_HANDLER)
@Provider
public class CorsResponseHandler {

    public void handleIfNecessary(IRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (empty(origin)) {
            return;
        }
        if (request.getMethod().toUpperCase().equals("OPTIONS")) {
            handlePreflight(request, response);
        } else {
            handleSimpleRequest(request, response);
        }

    }

    protected void handlePreflight(IRequest request, HttpServletResponse response) {

        //Access-Control-Allow-Methods: GET, POST, PUT
        //Access-Control-Allow-Headers: X-Custom-Header
        //Content-Type: text/html; charset=utf-8

        String originAllowed = getOriginAllowedHeader(request);
        if (!empty(originAllowed)) {
            response.addHeader("Access-Control-Allow-Origin", originAllowed);
        }

        CorsSettings cors = Settings.instance().getCors();
        response.addHeader("Access-Control-Allow-Credentials", ((Boolean)cors.isAllowCredentials()).toString().toLowerCase());
        response.addHeader("Access-Control-Allow-Methods", cors.getAllowedMethodsString());

        List<String> allowHeaders = list();
        for(String requestHeader: or(request.getHeader("Access-Control-Request-Headers"), "").split(",")) {
            requestHeader = requestHeader.trim();
            String requestHeaderLower = requestHeader.toLowerCase();
            if (cors.getAllowHeaders().contains(requestHeaderLower)) {
                allowHeaders.add(requestHeader);
            }
        }
        if (allowHeaders.size() > 0) {
            response.addHeader("Access-Control-Allow-Headers", String.join(",", allowHeaders));
        }

        response.addHeader("Access-Control-Max-Age", cors.getPreflightMaxAge().toString());

        //response.setContentType("text/html; charset=utf-8");
        //response.setStatus(200);
        //throw new ResponseComplete();
    }


    protected void handleSimpleRequest(IRequest request, HttpServletResponse response) {
        String originAllowed = getOriginAllowedHeader(request);
        if (!empty(originAllowed)) {
            response.addHeader("Access-Control-Allow-Origin", originAllowed);
        }
        //handleOriginAllowed(request);
        CorsSettings cors = Settings.instance().getCors();
        if (!empty(cors.getExposeHeadersString())) {
            response.addHeader("Access-Control-Expose-Headers", cors.getExposeHeadersString());
        }

        response.addHeader("Access-Control-Allow-Credentials", ((Boolean)cors.isAllowCredentials()).toString().toLowerCase());
        //Access-Control-Expose-Headers: FooBar
    }

    /**
     * Adds the Access-Control-Allow-Origin header if allowed, it not, raise an exception
     * @param request
     */
    private String getOriginAllowedHeader(IRequest request) {
        CorsSettings cors = Settings.instance().getCors();


        String origin = request.getHeader("Origin");
        boolean matches = false;
        String baseUrl = request.getScheme() + "://" + request.getHost();

        if (isFontRequest(request) && cors.isAllowAllForFonts()) {
            return "*";
        }

        if (baseUrl.equals(origin)) {
            return null;
        }


        if (cors.getUrlPattern() != null) {
            if (!cors.getUrlPattern().matcher(request.getPath()).matches()) {
                throw new ClientErrorException("CORS request not allowed for this URL path", 400);
            }
        }

        if (cors.isAllowAll()) {
            return "*";
        } else if (cors.getOriginWhitelist().contains(origin)) {
            //request.addResponseHeader("Access-Control-Allow-Origin", request.getScheme() + "://" + origin);
            return origin;
            //request.addResponseHeader("Access-Control-Allow-Origin", origin);
        } else {
            for (Pattern pattern: cors.getOriginPatternWhitelist()) {
                if (pattern.matcher(origin).matches()) {
                    //request.addResponseHeader("Access-Control-Allow-Origin", request.getScheme() + "://" + origin);
                    //request.addResponseHeader("Access-Control-Allow-Origin", origin);
                    return origin;
                    //matches = true;
                    //break;
                }
            }
        }


        throw new ClientErrorException("CORS request not allowed for this origin", 400);




    }

    public boolean isFontRequest(IRequest request) {
        String ext = FilenameUtils.getExtension(request.getRequestUri().getPath());
        if (fontExtensions.contains(ext)) {
            return true;
        }
        return false;
    }

    private static final Set<String> fontExtensions = set("tff", "woff", "eot", "svg", "otf");

}
