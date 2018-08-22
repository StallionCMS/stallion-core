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


import com.fasterxml.jackson.databind.ObjectMapper;
import io.stallion.services.Log;
import io.stallion.users.IOrg;
import io.stallion.users.IUser;
import io.stallion.utils.IEmpty;
import io.stallion.utils.json.JSON;
import io.stallion.utils.json.RestrictedViews;

import javax.ws.rs.core.Cookie;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.stallion.utils.Literals.list;

public class EmptyRequest implements IRequest, IEmpty {

    private boolean handled = false;
    private Map<String, String> headers = new HashMap<>();
    private String path = "";
    private String query = "";
    private String method = "GET";
    private String content = null;
    private Map<String, Object> data = new HashMap<>();
    private Object dataObject = null;
    private Map<String, String> queryMap = null;
    private List<Cookie> cookies = list();

    public EmptyRequest()  {
        this.path = "";
        init();
    }

    public EmptyRequest(String path) {
        setPath(path);
        this.method = method;
        init();
    }

    private void init() {

    }

    @Override
    public String requestUrl() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public Object getBodyObject(Class clazz) {
        return null;
    }

    @Override
    public String getQueryParam(String name) {
        return null;
    }

    @Override
    public String getBodyString() {
        return null;
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public void setProperty(String name, Object obj) {

    }

    @Override
    public Iterable<String> getHeaderNames() {
        return null;
    }

    @Override
    public String getQueryParam(String name, String defaultValue) {
        return null;
    }

    @Override
    public IUser getUser() {
        return null;
    }

    @Override
    public void setUser(IUser user) {

    }

    @Override
    public IOrg getOrg() {
        return null;
    }

    @Override
    public void setOrg(IOrg org) {

    }

    @Override
    public Boolean getIsJsonRequest() {
        return null;
    }

    @Override
    public void setIsJsonRequest(Boolean isJsonRequest) {

    }

    @Override
    public SandboxedRequest getSandboxedRequest(Sandbox box) {
        return null;
    }

    @Override
    public Set<String> getScopes() {
        return null;
    }

    @Override
    public IRequest setScopes(Set<String> scopes) {
        return null;
    }

    @Override
    public boolean isScoped() {
        return false;
    }

    @Override
    public IRequest setScoped(boolean scoped) {
        return null;
    }

    @Override
    public Long getValetUserId() {
        return null;
    }

    @Override
    public String getValetEmail() {
        return null;
    }

    public static Map<String, String> splitQuery(URL url) {
        Map<String, String> query_pairs = new HashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.exception(e, "Error parsing url {0}", url);
            }
        }
        return query_pairs;
    }

    public EmptyRequest addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public EmptyRequest addData(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public EmptyRequest setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public EmptyRequest setDataObject(Object dataObject) {
        this.dataObject = dataObject;
        return this;
    }

    public EmptyRequest setContent(String content) {
        this.content = content;
        return this;
    }




    public BufferedReader getReader() {
        if (content != null) {
            return contentToReader();
        }
        if (dataObject != null) {
            content = dataObjectToString();
        } else if (data.size() > 0) {
            content = dataToString();
        } else {
            content = "";
        }
        return contentToReader();

    }


    public IRequest setHandled(Boolean val) {

        handled = val;
        return this;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        int q = path.indexOf("?");
        if (q != -1) {
            this.path = path.substring(0, q);
            this.query = path.substring(q + 1);
        } else {
            this.path = path;
        }
    }

    public String getHeader(String name) {
        if (headers.containsKey(name)) {
            return headers.get(name);
        } else {
            return "";
        }
    }


    @Override
    public String getMethod() {
        return this.method;
    }



    @Override
    public String getActualIp() {
        return "127.0.0.100";
    }

    protected String dataToString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this.data);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String dataObjectToString() {
        try {
            return JSON.stringify(this.dataObject, RestrictedViews.Internal.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected BufferedReader contentToReader() {
        StringReader sr = new StringReader(content);
        BufferedReader br = new BufferedReader(sr);
        return br;
    }

    public Cookie getCookie(String name) {
        for(Cookie cookie: cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    public EmptyRequest setCookies(Cookie ...cookies) {
        this.cookies = list(cookies);
        return this;
    }


    public void setQuery(String q) {
        this.query = q;
    }

    @Override
    public String getQueryString() {
        if (query == null) {
            if (!this.getRequestUrl().contains("?")) {
                query = "";
            } else {
                query = requestUrl().split("\\?", 2)[1];
            }
        }
        return query;
    }

}
