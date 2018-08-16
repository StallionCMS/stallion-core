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
import io.stallion.requests.IRequest;
import io.stallion.requests.RequestWrapper;
import org.glassfish.jersey.server.ExtendedUriInfo;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;

//@Provider
public class XsrfRequestFilter implements ContainerRequestFilter {
    public static final String COOKIE_NAME = "XSRF-TOKEN";
    public static final String HEADER_NAME = "X-XSRF-TOKEN";

    @javax.ws.rs.core.Context
    private HttpServletResponse httpResponse;



    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        if (requiresValidXsrfTokens(containerRequestContext)) {
            if (!isTokenValid(containerRequestContext)) {
                addCookieIfNotExists(containerRequestContext);
                throw new ClientErrorException("To prevent Cross-Site Request Forgery attacks, this request requires a cookie XSRF-TOKEN that matches header X-XSRF-TOKEN. ", 403);
            }
        }
        addCookieIfNotExists(containerRequestContext);

    }


    private boolean requiresValidXsrfTokens(ContainerRequestContext containerRequestContext) {
        if (isSameOrigin(containerRequestContext)) {
            return false;
        }
        IRequest request = new RequestWrapper(containerRequestContext);
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

    private boolean isSameOrigin(ContainerRequestContext containerRequestContext) {
        IRequest request = new RequestWrapper(containerRequestContext);
        String baseUrl = request.getScheme() + "://" + request.getHost();
        String origin = request.getHeader("Origin");
        if (!empty(origin)) {
            if (baseUrl.equals(origin)) {
                return true;
            }
        }
        String referrer = request.getHeader("Referer");
        if (!empty(referrer) && referrer.startsWith(baseUrl)) {
            return true;
        }
        return false;
    }


    private boolean isTokenValid(ContainerRequestContext containerRequestContext) {
        IRequest request = new RequestWrapper(containerRequestContext);

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

    public void addCookieIfNotExists(ContainerRequestContext containerRequestContext) {
        {
            Cookie cookie = new RequestWrapper(containerRequestContext).getCookie(COOKIE_NAME);
            if (cookie != null && !empty(cookie.getValue())) {
                return;
            }

        }
        if (httpResponse != null) {
            javax.servlet.http.Cookie newCookie = new javax.servlet.http.Cookie(COOKIE_NAME, UUID.randomUUID().toString());
            newCookie.setMaxAge(20 * 365 * 86400);
            httpResponse.addCookie(newCookie);
        }
    }

}

