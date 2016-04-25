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

package io.stallion.plugins.javascript;

import io.stallion.Context;
import io.stallion.requests.SandboxedRequest;
import io.stallion.requests.SandboxedResponse;
import io.stallion.requests.Site;
import io.stallion.templating.TemplateRenderer;
import io.stallion.users.EmptyUser;
import io.stallion.users.IUser;

import java.util.Map;

import static io.stallion.Context.*;


public class SandboxedContext {

    private JsPluginSettings pluginSettings;
    private SandboxedDal dal;
    private String pluginFolder;
    private Sandbox sandbox;
    private Site site;


    public SandboxedContext(String pluginFolder, Sandbox sandbox, JsPluginSettings pluginSettings) {
        this.pluginFolder = pluginFolder;
        this.pluginSettings = pluginSettings;
        this.dal = new SandboxedDal(sandbox);
        this.sandbox = sandbox;
        this.site = new Site();
        this.site
                .setName(settings().getSiteName())
                .setTitle(settings().getDefaultTitle())
                .setUrl(settings().getSiteUrl());
    }

    public String renderTemplate(String template, Map<String, Object> context) {
        return TemplateRenderer.instance().renderSandboxedTemplate(sandbox, template, context);
    }


    public JsPluginSettings getPluginSettings() {
        return pluginSettings;
    }

    public IUser getUser() {
        if (sandbox.getUsers().isCanAccess()) {
            return Context.getUser();
        } else {
            return new EmptyUser();
        }
    }

    public SandboxedRequest getRequest() {
        return Context.getRequest().getSandboxedRequest(sandbox);
    }

    public SandboxedDal getDal() {
        return dal;
    }

    public SandboxedResponse getResponse() {
        return Context.getResponse().getSandboxedResponse();
    }

    public String getPluginFolder() {
        return pluginFolder;
    }

    public SandboxedContext setPluginFolder(String pluginFolder) {
        this.pluginFolder = pluginFolder;
        return this;
    }

    public Site getSite() {
        return site;
    }

    public SandboxedContext setSite(Site site) {
        this.site = site;
        return this;
    }
}
