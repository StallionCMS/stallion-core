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

import java.io.IOException;
import java.util.UUID;

import static io.stallion.utils.Literals.*;

import io.stallion.exceptions.ClientException;
import io.stallion.requests.StRequest;
import io.stallion.restfulEndpoints.XSRF;
import org.glassfish.jersey.server.ExtendedUriInfo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

//@Provider
public class XsrfRequestFilter implements ContainerRequestFilter {
    public static final String COOKIE_NAME = "XSRF-TOKEN";
    public static final String HEADER_NAME = "X-XSRF-TOKEN";

    @javax.ws.rs.core.Context
    private HttpServletRequest httpRequest;

    @javax.ws.rs.core.Context
    private HttpServletResponse httpResponse;

    private StRequest request;

    private StRequest getRequest() {
        if (request == null) {
            request = new StRequest(httpRequest);
        }
        return request;
    }


    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        if (requiresValidXsrfTokens(containerRequestContext)) {
            if (!isTokenValid(containerRequestContext)) {
                addCookieIfNotExists();
                throw new ClientException("To prevent Cross-Site Request Forgery attacks, this request requires a cookie XSRF-TOKEN that matches header X-XSRF-TOKEN. ", 403);
            }
        }
        addCookieIfNotExists();

    }


    private boolean requiresValidXsrfTokens(ContainerRequestContext containerRequestContext) {
        if (isSameOrigin()) {
            return false;
        }
        StRequest request = new StRequest(httpRequest);
        XSRF anno = ((ExtendedUriInfo)containerRequestContext.getUriInfo())
                .getMatchedResourceMethod()
                .getInvocable()
                .getHandlingMethod()
                .getAnnotation(XSRF.class);
        if (anno == null) {
            anno = ((ExtendedUriInfo)containerRequestContext.getUriInfo())
                    .getMatchedResourceMethod()
                    .getParent()
                    .getClass()
                    .getAnnotation(XSRF.class);
        }
        if (anno != null && anno.value() == false) {
            return false;
        }
        if (anno != null && anno.value() == true) {
            return true;
        }



        // If no explicit XSRF annotation, then we require the check for
        // everything but an "text/html" GET that returns a String

        if (!"GET".equals(request.getMethod().toLowerCase())) {
            return true;
        }

        if (!String.class.equals(((ExtendedUriInfo)containerRequestContext.getUriInfo())
                .getMatchedResourceMethod()
                .getInvocable()
                .getHandlingMethod()
                .getReturnType())) {
            return true;
        }

        // X-Requested-With: XMLHttpRequest
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }

        String[] contentTypes = {"text/html"};

        Produces panno = ((ExtendedUriInfo)containerRequestContext.getUriInfo())
                .getMatchedResourceMethod()
                .getInvocable()
                .getHandlingMethod()
                .getAnnotation(Produces.class);
        if (panno == null) {
            panno = ((ExtendedUriInfo)containerRequestContext.getUriInfo())
                    .getMatchedResourceMethod()
                    .getParent()
                    .getClass()
                    .getAnnotation(Produces.class);
        }
        if (panno != null) {
            contentTypes = panno.value();
        }

        if (contentTypes.length == 1 && "text/html".equals(contentTypes[0])) {
            return false;
        }

        return true;



    }

    private boolean isSameOrigin() {
        String baseUrl = getRequest().getScheme() + "://" + getRequest().getHost();
        String origin = getRequest().getHeader("Origin");
        if (!empty(origin)) {
            if (baseUrl.equals(origin)) {
                return true;
            }
        }
        String referrer = getRequest().getHeader("Referer");
        if (!empty(referrer) && referrer.startsWith(baseUrl)) {
            return true;
        }
        return false;
    }


    private boolean isTokenValid(ContainerRequestContext containerRequestContext) {


        Cookie cookie = request.getCookie(COOKIE_NAME);
        if (cookie == null || empty(cookie.getValue())) {
            return false;
        }
        String header = request.getHeader(HEADER_NAME);
        if (empty(header)) {
            return false;
        }
        return header.equals(cookie.getValue());
    }

    public void addCookieIfNotExists() {
        {
            Cookie cookie = new StRequest(httpRequest).getCookie(COOKIE_NAME);
            if (cookie != null && !empty(cookie.getValue())) {
                return;
            }
        }
        {
            Cookie newCookie = new Cookie(COOKIE_NAME, UUID.randomUUID().toString());
            newCookie.setMaxAge(20 * 365 * 86400);
            httpResponse.addCookie(newCookie);
        }
    }

}

