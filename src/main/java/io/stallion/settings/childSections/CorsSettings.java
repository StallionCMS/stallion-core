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

package io.stallion.settings.childSections;

import io.stallion.settings.SettingMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


/**
 * Configure Cross-Origin Resource Sharing
 */
public class CorsSettings implements SettingsSection {
    @SettingMeta(valBoolean = false)
    private Boolean allowAll;

    @SettingMeta(valBoolean = true)
    private Boolean allowAllForFonts;

    @SettingMeta(cls = ArrayList.class)
    private List<String> originWhitelist;
    @SettingMeta(cls = ArrayList.class)
    private List<String> originRegexWhitelist;
    private List<Pattern> originPatternWhitelist;
    @SettingMeta(valBoolean = false)
    private boolean allowCredentials = false;
    @SettingMeta(val = "")
    private String urlsRegex;
    private Pattern urlPattern;
    @SettingMeta(cls=ArrayList.class)
    private List<String> exposeHeaders;
    @SettingMeta(cls=ArrayList.class)
    private List<String> allowHeaders;

    @SettingMeta(cls=ArrayList.class)
    private List<String> allowedMethods;

    private String allowedMethodsString;

    @SettingMeta(valInt = 86400)
    private Integer preflightMaxAge;


    private String exposeHeadersString;


    public void postLoad() {
        if (originPatternWhitelist == null) {
            originPatternWhitelist = list();
            for (String s:originRegexWhitelist) {
                originPatternWhitelist.add(Pattern.compile(s));
            }
        }
        if (urlPattern == null && !empty(urlsRegex)) {
            urlPattern = Pattern.compile(urlsRegex);
        }
        if (exposeHeadersString == null && exposeHeaders.size() > 0) {
            exposeHeadersString = String.join(",", exposeHeaders);
        }
        if (empty(allowHeaders)) {
            allowHeaders = list("x-requested-with", "content-type", "accept", "origin", "authorization", "x-csrftoken");
        }

        List<String> allowHeaderLowered = list();
        for (String header: allowHeaders) {
            allowHeaderLowered.add(header.toLowerCase());
        }
        allowHeaders = allowHeaderLowered;

        if (empty(allowedMethods)) {
            allowedMethods = list("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS");
        }

        if (empty(allowedMethodsString)) {
            allowedMethodsString = String.join(",", allowedMethods);
        }
    }

    public boolean isAllowAll() {
        return allowAll;
    }

    public CorsSettings setAllowAll(boolean allowAll) {
        this.allowAll = allowAll;
        return this;
    }

    public List<String> getOriginWhitelist() {
        return originWhitelist;
    }

    public CorsSettings setOriginWhitelist(List<String> originWhitelist) {
        this.originWhitelist = originWhitelist;
        return this;
    }

    public List<String> getOriginRegexWhitelist() {
        return originRegexWhitelist;
    }

    public CorsSettings setOriginRegexWhitelist(List<String> originRegexWhitelist) {
        this.originRegexWhitelist = originRegexWhitelist;
        return this;
    }

    public List<Pattern> getOriginPatternWhitelist() {
        return originPatternWhitelist;
    }

    public CorsSettings setOriginPatternWhitelist(List<Pattern> originPatternWhitelist) {
        this.originPatternWhitelist = originPatternWhitelist;
        return this;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public CorsSettings setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    public String getUrlsRegex() {
        return urlsRegex;
    }

    public CorsSettings setUrlsRegex(String urlsRegex) {
        this.urlsRegex = urlsRegex;
        return this;
    }

    public Pattern getUrlPattern() {
        return urlPattern;
    }

    public CorsSettings setUrlPattern(Pattern urlPattern) {
        this.urlPattern = urlPattern;
        return this;
    }

    public List<String> getExposeHeaders() {
        return exposeHeaders;
    }

    public CorsSettings setExposeHeaders(List<String> exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
        return this;
    }

    public String getExposeHeadersString() {
        return exposeHeadersString;
    }


    public List<String> getAllowHeaders() {
        return allowHeaders;
    }

    public CorsSettings setAllowHeaders(List<String> allowHeaders) {
        this.allowHeaders = allowHeaders;
        return this;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public CorsSettings setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
        return this;
    }

    public Integer getPreflightMaxAge() {
        return preflightMaxAge;
    }

    public CorsSettings setPreflightMaxAge(Integer preflightMaxAge) {
        this.preflightMaxAge = preflightMaxAge;
        return this;
    }

    public String getAllowedMethodsString() {
        return allowedMethodsString;
    }

    public boolean isAllowAllForFonts() {
        return allowAllForFonts;
    }

    public CorsSettings setAllowAllForFonts(boolean allowAllForFonts) {
        this.allowAllForFonts = allowAllForFonts;
        return this;
    }
}
