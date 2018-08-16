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



import java.util.HashMap;
import java.util.Map;


public class RouteResult {
    /*(var template:String, var params:Map<String, String>, redirectUrl:String, var preempt:Boolean, var name:String, var group:String)*/
    private String template = "";
    private Map<String, String> params = new HashMap<String, String>();
    private String redirectUrl = "";
    private Boolean preempt = false;
    private String name = "";
    private String group = "";

    private String pageTitle = "";
    private String metaDescription = "";

    public String getTemplate() {
        return template;
    }

    public RouteResult setTemplate(String template) {
        this.template = template;
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public RouteResult setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public RouteResult setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    public Boolean getPreempt() {
        return preempt;
    }

    public RouteResult setPreempt(Boolean preempt) {
        this.preempt = preempt;
        return this;
    }

    public String getName() {
        return name;
    }

    public RouteResult setName(String name) {
        this.name = name;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public RouteResult setGroup(String group) {
        this.group = group;
        return this;
    }



    public String getPageTitle() {
        return pageTitle;
    }

    public RouteResult setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public RouteResult setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
        return this;
    }
}