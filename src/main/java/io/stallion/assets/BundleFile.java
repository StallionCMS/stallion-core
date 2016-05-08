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

package io.stallion.assets;

import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

import java.util.Map;
import static io.stallion.utils.Literals.*;

public class BundleFile {
    //private String path;
    private String query = "";
    //private String fullPath;
    private long timeStamp = 0;
    private Map<String, String> queryParams = new HashMap<>();
    private String pluginName;
    private String liveUrl;
    private String debugUrl;
    private String developerUrl;
    private String assetKey;
    private String processor = null;

    public BundleFile() {

    }

    // http://stackoverflow.com/questions/13592236/parse-the-uri-string-into-name-value-collection-in-java
    private Map<String, String> splitQuery(String query) {
        Map<String, String> query_pairs = new HashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.exception(e, "Error parsing query string {0}", query);
            }
        }
        return query_pairs;
    }

    public String getAssetKey() {
        return assetKey;
    }

    public BundleFile setAssetKey(String assetKey) {
        this.assetKey = assetKey;
        return this;
    }


    public String getQuery() {
        if (query == null) {
            URI uri = URI.create(getLiveUrl());
            query = uri.getQuery();
        }
        return query;
    }

    public BundleFile setQuery(String query) {
        this.query = query;
        return this;
    }


    public Map<String, String> getQueryParams() {
        if (queryParams == null) {
            queryParams = splitQuery(getQuery());
        }
        return queryParams;
    }

    public BundleFile setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
        return this;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public BundleFile setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public String getPluginName() {
        return pluginName;
    }

    public BundleFile setPluginName(String pluginName) {
        this.pluginName = pluginName;
        return this;
    }


    public String getLiveUrl() {
        return liveUrl;
    }

    public BundleFile setLiveUrl(String liveUrl) {
        this.liveUrl = liveUrl;
        return this;
    }

    public String getDebugUrl() {
        if (debugUrl == null) {
            return getLiveUrl();
        }
        return debugUrl;
    }

    public BundleFile setDebugUrl(String debugUrl) {
        this.debugUrl = debugUrl;
        return this;
    }

    public String getDeveloperUrl() {
        if (developerUrl == null) {
            return getDebugUrl();
        }
        return developerUrl;
    }

    public BundleFile setDeveloperUrl(String developerUrl) {
        this.developerUrl = developerUrl;
        return this;
    }

    @Override
    public int hashCode() {
        if (empty(liveUrl)) {
            throw new UsageException("You called hashCode before defining the url!");
        }
        return new HashCodeBuilder(139, 367). // two randomly chosen prime numbers
                append(assetKey).
                append(pluginName).
                append(liveUrl).
                toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  BundleFile)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        BundleFile bf = (BundleFile)o;
        return new EqualsBuilder().
                append(pluginName, bf.pluginName).
                append(assetKey, bf.assetKey).
                append(liveUrl, bf.liveUrl).
                isEquals();
    }


    public String getProcessor() {
        if (processor == null) {
            processor = getQueryParams().getOrDefault("processor", "");
        }
        return processor;
    }

    public BundleFile setProcessor(String processor) {
        this.processor = processor;
        return this;
    }
}
