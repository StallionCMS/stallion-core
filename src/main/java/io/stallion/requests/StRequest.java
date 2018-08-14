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

import javax.persistence.Column;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stallion.Context;
import io.stallion.exceptions.ClientException;
import io.stallion.plugins.javascript.Sandbox;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.EmptyOrg;
import io.stallion.users.EmptyUser;
import io.stallion.users.IOrg;
import io.stallion.users.IUser;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jetty.server.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

import static io.stallion.utils.Literals.*;

public class StRequest implements IRequest {
    private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));


    private HttpServletRequest request;
    private Request baseRequest;
    private String path;
    private String query = null;
    private IUser user = new EmptyUser();
    private Long valetUserId = 0L;
    private String valetEmail = "";
    private boolean scoped = false;
    private Set<String> scopes;
    private IOrg org = new EmptyOrg();
    private String rawRequestBody = null;
    private Map<String, Object> bodyMap;
    private String content = null;
    private Object bodyObject = null;
    private Boolean isJsonRequest = null;
    private Map<String, String> queryParams = null;
    private Map<String, Object> items = map();
    private SandboxedRequest sandboxedRequest;
    private String scheme;


    public StRequest() {

    }

    public StRequest(HttpServletRequest request) {
        this.path = request.getPathInfo().toString();
        this.request = request;
        this.query = request.getQueryString();
        this.baseRequest = null;
    }


    public StRequest(String path, Request baseRequest, HttpServletRequest request) {
        this.setPath(path);
        this.request = request;
        this.baseRequest = baseRequest;
        this.query = request.getQueryString();
    }


    @Override
    public void setAsMultiPartRequest() {
        if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
            if (baseRequest != null) {
                baseRequest.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
            }
        }
    }

    public HttpServletRequest getHttpServletRequest() {
        return this.request;
    }



    public Part getPart(String name) {
        try {
            return request.getPart(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the absolute url of the original request.
     * Reconstructs the URL using the x-forwarded-host, if necessary,
     * so this will work in the case of proxies.
     * @return
     */
    @Override
    public String requestUrl() {
        String host = getHost();
        String proto = getScheme();
        String baseUrl = proto + "://" + host;
        return baseUrl + this.getPath();
    }



    @Override
    public String getScheme() {
        if (empty(scheme) && this.request != null) {
            if (!empty(getHeader("x-upstream-forwarded-proto"))) {
                scheme = getHeader("x-upstream-forwarded-proto");
            }
            if (empty(scheme) && !empty(getHeader("x-forwarded-proto"))) {
                scheme = getHeader("x-forwarded-proto");
            }
        }
        if (empty(scheme)) {
            String baseUrl = "";
            if (Context.getSettings() != null ) {
                baseUrl = Context.getSettings().getSiteUrl();
            }
            if (baseUrl != null) {
                int i = baseUrl.indexOf("://");
                if (i > -1) {
                    scheme = baseUrl.substring(0, i);
                }
            }
        }
        if (empty(scheme) && this.request != null) {
            scheme = this.request.getScheme();
        }
        if (empty(scheme)){
            scheme = "http";
        }
        return scheme;
    }

    @Override
    public String getHost() {
        if (this.request == null) {
            return "http://localhost" + this.getPath();
        }
        String host = this.request.getHeader("host");
        if (!empty(getHeader("x-forwarded-host"))) {
            host = getHeader("x-forwarded-host");
        }
        return host;
    }

    @Override
    public String getQueryString() {
        if (query == null) {
            if (!requestUrl().contains("?")) {
                query = "";
            } else {
                query = requestUrl().split("\\?", 2)[1];
            }
        }
        return query;
    }

    @Override
    public String getRemoteAddr() {
        if (request != null) {
            return request.getRemoteAddr();
        } else {
            return "0.0.0.0";
        }
    }

    @Override
    public String getActualIp() {
        if (!empty(Settings.instance().getIpHeaderName())) {
            return or(request.getHeader(Settings.instance().getIpHeaderName()), getRemoteAddr());
        }
        return getRemoteAddr();
    }

    @Override
    public Object getBodyObject(Class clazz) {

        if (bodyObject == null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                bodyObject = mapper.readValue(getContent(), clazz);
            } catch (IOException e) {
                Log.exception(e, "Could not parse JSON body for the request: " + content);
                throw new ClientException("Could not parse JSON body for the request.");
            }
        }
        return bodyObject;
    }

    @Override
    public Map<String, Object> getBodyMap() {
        if (bodyMap == null) {
            if (or(getHeader("Content-type"), "").startsWith("application/x-www-form-urlencoded")) {
                bodyMap = new HashMap<>();

                for (NameValuePair pair: URLEncodedUtils.parse(getContent(), Charset.forName(or(request.getCharacterEncoding(), "UTF8")))) {
                    if (bodyMap.containsKey(pair.getName())) {
                        Object existing = bodyMap.get(pair.getName());
                        if (existing instanceof List) {
                            ((List)existing).add(pair.getValue());
                        } else {
                            bodyMap.put(pair.getName(), list(existing, pair.getValue()));
                        }
                    } else {
                        bodyMap.put(pair.getName(), pair.getValue());
                    }
                }
            } else {
                ObjectMapper mapper = new ObjectMapper();
                String content = getContent();
                if (StringUtils.isEmpty(content)) {
                    bodyMap = new HashMap<>();
                } else {
                    try {
                        bodyMap = mapper.readValue(getContent(),
                                    new TypeReference<HashMap<String, Object>>() {
                                    });
                    } catch (IOException e) {
                        Log.exception(e, "Could not parse JSON body for the request: " + content);
                        throw new ClientException("Could not parse JSON body for the request.");
                    }

                }
            }
        }
        return bodyMap;
    }

    @Override
    public Object getBodyParam(String name) {

        return getBodyMap().getOrDefault(name, null);
    }

    @Override
    public Map<String, String> getQueryParams() {
        if (queryParams == null) {
            queryParams = queryToParams();
        }
        return queryParams;
    }


    private Map<String, String> queryToParams() {
        Map<String, String> query_pairs = new HashMap<String, String>();
        if (empty(getQueryString())) {
            return map();
        }
        String[] pairs = getQueryString().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx < 0) {
                continue;
            }
            try {
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.exception(e, "Error parsing url {0}", getQueryString());
            }
        }
        return query_pairs;
    }


    @Override
    public Cookie[] getCookies() {
        return this.request.getCookies();
    }

    @Override
    public Cookie getCookie(String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getHeader(String name) {
        return this.request.getHeader(name);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return this.request.getReader();
    }

    @Override
    public String getContent() {
        if (content == null) {
            try {
                //this.content = IOUtils.toString(getReader());
                this.content = IOUtils.toString(this.request.getInputStream(), "UTF8");
            //} catch (java.lang.IllegalStateException e) {
            //    try {
            //        this.content = IOUtils.toString(this.request.getInputStream());
            //   } catch (IOException e2) {
            //        throw new RuntimeException(e);
            //    }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return content;

    }

    @Override
    public Enumeration<String> getHeaderNames() {
        if (request == null) {
            //return (Enumeration<String>)new ArrayList<String>();
            return new Vector().elements();
        } else {
            return request.getHeaderNames();
        }

    }





    @Override
    public IUser getUser() {
        return user;
    }

    @Override
    public void setUser(IUser user) {
        this.user = user;
    }

    @Override
    public IOrg getOrg() {
        return org;
    }

    @Override
    public void setOrg(IOrg org) {
        this.org = org;
    }

    @Override
    public String getMethod() {
        return this.request.getMethod();
    }

    @Override
    public String getParameter(String paramName) {
        return this.request.getParameter(paramName);
    }

    @Override
    public Boolean getIsJsonRequest() {
        return isJsonRequest;
    }

    @Override
    public void setIsJsonRequest(Boolean isJsonRequest) {
        this.isJsonRequest = isJsonRequest;
    }

    @Override
    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public Map<String, Object> getItems() {
        return items;
    }

    @Override
    public void setItems(Map<String, Object> items) {
        this.items = items;
    }

    @Override
    public SandboxedRequest getSandboxedRequest(Sandbox box) {
        if (sandboxedRequest == null) {
            sandboxedRequest = new SandboxedRequest(box, this);
        }
        return sandboxedRequest;
    }

    @Override
    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public StRequest setScopes(Set<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    @Override
    public boolean isScoped() {
        return scoped;
    }

    @Override
    public StRequest setScoped(boolean scoped) {
        this.scoped = scoped;
        return this;
    }

    @Column
    public Long getValetUserId() {
        return valetUserId;
    }

    public StRequest setValetUserId(Long valetUserId) {
        this.valetUserId = valetUserId;
        return this;
    }

    @Column
    public String getValetEmail() {
        return valetEmail;
    }

    public StRequest setValetEmail(String valetEmail) {
        this.valetEmail = valetEmail;
        return this;
    }
}
