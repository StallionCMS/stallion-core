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

import io.stallion.plugins.javascript.Sandbox;
import io.stallion.services.Log;
import io.stallion.users.IOrg;
import io.stallion.users.IUser;
import org.eclipse.jetty.server.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.list;

public interface IRequest {
    public static final String RECENT_POSTBACK_COOKIE = "st-recent-postback";


    public default void setAsMultiPartRequest() {

    }

    public default Part getPart(String name) {
        return null;
    }

    public default HttpServletRequest getHttpServletRequest() {
        return null;
    }

    /**
     * Get the full, externally facing URL used to intiate this request.
     *
     * @return
     */
    public String requestUrl();

    /**
     * Alis for requestUrl()
     * @return
     */
    public default String getRequestUrl() {
        return requestUrl();
    }

    public String getScheme();


    public default URI getRequestUri() {
        try {
            return new URI(requestUrl());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Absolute URL of the request with the query string.
     */
    public default String getRequestUrlWithQuery() {
        String url = requestUrl();
        if (!empty(getQueryString())) {
            url += "?" + getQueryString();
        }
        return url;
    }

    /**
     * Get the request query string, everything after the ? in the URL
     *
     * @return
     */
    public String getQueryString();

    public default List<String> getQueryParamAsList(String key) {
        List<String> values = list();
        String[] pairs = getQueryString().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx < 0) {
                continue;
            }
            try {
                String name = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                if (name.equals(key)) {
                    values.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                Log.exception(e, "Error parsing url {0}", getQueryString());
            }
        }
        return values;
    }

    /**
     * Get the RemoteAddr field from the underlying HttpServletRequest object,
     * which gets it from the socket connection. Note that if you have nginx
     * running as a proxy in front of Stallion, then remoteAddr will be localhost,
     * since the socket connection is coming from Nginx. If you have a load balancer
     * in front of Stallion, the remoteAddr will be of the load balancer
     *
     * @return
     */
    public String getRemoteAddr();


    /**
     * Gets the host by first looking for the x-forwarded-for header, and then
     * the Host: header.
     *
     * @return
     */
    public String getHost();

    /**
     * Tries to guess the actual IP address of the end-user, by looking at the
     * IP address header as defined in settings.ipHeaderName (x-real-ip by default) and
     * then getRemoteAddr() if the header does not exist.
     *
     * @return
     */
    public String getActualIp();

    /**
     * Interpret the body of the request as JSON and parse it into the given object.
     *
     * @param clazz
     * @return
     */
    public Object getBodyObject(Class clazz);

    /**
     * Parse the request body based on the content-type, usually either form encoded or JSON,
     * and then parse it into a Map
     *
     * @return
     */
    public Map<String, Object> getBodyMap();

    /**
     * Internally, gets the request body as Map, and then returns the value for the given key
     *
     * @param name
     * @return
     */
    public Object getBodyParam(String name);

    /**
     * Parses the query string into a map
     *
     * @return
     */
    public Map<String, String> getQueryParams();

    public Cookie[] getCookies();

    public Cookie getCookie(String cookieName);

    /**
     * Get the request path
     *
     * @return
     */
    public String getPath();


    public void setPath(String path);

    /**
     * Get the given request header, case insensitive
     * @param name
     * @return
     */
    public String getHeader(String name);

    /**
     * Get a Reader that reads the request body
     *
     * @return
     * @throws IOException
     */
    public BufferedReader getReader() throws IOException;

    /**
     * Gets the request body as a string
     *
     * @return
     */
    public String getContent();

    public Enumeration<String> getHeaderNames();

    /**
     * Gets the user associated with the current request
     *
     * @return
     */
    public IUser getUser();

    public void setUser(IUser user);

    public IOrg getOrg();

    public void setOrg(IOrg org);

    /**
     * Gets the HTTP method - GET, POST, PUT, DELETE, etc.
     * @return
     */
    public String getMethod();

    /**
     * Gets the parameter from either the query string or the request body
     * @param paramName
     * @return
     */
    public String getParameter(String paramName);

    /**
     * Returns true if this request is expected to produce JSON, used to determine
     * what kind of response to give in cases of errors.
     *
     * @return
     */
    public Boolean getIsJsonRequest();

    public void setIsJsonRequest(Boolean isJsonRequest);


    public void setQuery(String query);

    /**
     * Get an arbitrary hashmap of data that lives the lifetime of this request,
     * can be used as a very short lived cache, or for any other arbitrary use.
     *
     * @return
     */
    public Map<String, Object> getItems();

    public void setItems(Map<String, Object> items);

    /**
     * Get a sandboxed version of this request object. Sandboxing usually limits
     * access to the headers and cookies, so that a plugin cannot steal authentication
     * tokens.
     *
     * @param box
     * @return
     */
    public SandboxedRequest getSandboxedRequest(Sandbox box);

    /**
     * If this is a scoped, OAuth request, return the scopes.
     * @return
     */
    public Set<String> getScopes();

    public IRequest setScopes(Set<String> scopes);

    /**
     * Return true if this is scope-limited OAuth request
     * @return
     */
    public boolean isScoped();

    public IRequest setScoped(boolean scoped);
}
