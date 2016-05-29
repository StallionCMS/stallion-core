/*
 * Stallion: A Modern Content Management System
 *
 * Copyright (C) 2015 - 2016 Patrick Fitzsimmons.
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

package io.stallion.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stallion.requests.StRequest;
import io.stallion.services.Log;

import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;


public class MockRequest extends StRequest {
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

    public MockRequest(String path)  {
        this.path = path;
        init();
    }

    public MockRequest(String path, String method) {
        setPath(path);
        this.method = method;
        init();
    }

    private void init() {

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

    public MockRequest addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public MockRequest addData(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public MockRequest setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public MockRequest setDataObject(Object dataObject) {
        this.dataObject = dataObject;
        return this;
    }

    public MockRequest setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public String getContent() {
        if (content != null) {
            return content;
        }
        if (dataObject != null) {
            content = dataObjectToString();
        } else if (data != null && data.size() > 0) {
            content = dataToString();
        }
        if (content == null) {
            content = "";
        }
        return content;
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


    public StRequest setHandled(Boolean val) {

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
    public String getParameter(String paramName) {
        return getQueryParams().getOrDefault(paramName, null);
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    /* Private helpers */
    @Override
    public Map<String, String> getQueryParams()
    {
        if (queryMap == null) {
            queryMap = new HashMap<String, String>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > -1) {
                    try {
                        queryMap.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Log.exception(e, "Error parsing query {0}", query);
                    }
                }
            }
        }
        return queryMap;
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
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this.dataObject);
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

    public StRequest setCookies(Cookie ...cookies) {
        this.cookies = list(cookies);
        return this;
    }

    @Override
    public void setQuery(String q) {
        this.query = q;
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

}
