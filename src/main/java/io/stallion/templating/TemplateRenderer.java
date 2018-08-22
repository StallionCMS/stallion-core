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

package io.stallion.templating;

import io.stallion.Context;
import io.stallion.assets.AssetsController;
import io.stallion.dataAccess.ModelController;
import io.stallion.exceptions.UsageException;
import io.stallion.fileSystem.FileSystemWatcherService;
import io.stallion.requests.MetaInformation;
import io.stallion.requests.Sandbox;
import io.stallion.requests.SandboxedRequest;
import io.stallion.requests.Site;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.EmptyUser;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.Sanitize;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.stallion.Context.dal;
import static io.stallion.Context.settings;
import static io.stallion.utils.Literals.empty;


public class TemplateRenderer {

    private static TemplateRenderer _instance;
    private JinjaTemplating templating;

    {
        templating = new JinjaTemplating(Settings.instance().getTargetFolder(), Settings.instance().getDevMode() == true);
    }

    public static TemplateRenderer instance() {
        if (_instance == null) {
            throw new UsageException("You must call TemplateRenderer.load() before calling instance.");
        }
        return _instance;
    }

    public static TemplateRenderer load() {
        _instance = new TemplateRenderer();
        if (new File(Settings.instance().getTargetFolder() + "/templates").isDirectory()) {
            FileSystemWatcherService.instance().registerWatcher(new TemplateFileChangeEventHandler()
                    .setWatchedFolder(Settings.instance().getTargetFolder() + "/templates")
                    .setWatchTree(true)
                    .setExtension(".jinja"));
        }
        return _instance;
    }

    public static void shutdown() {
        JinjaResourceLocator.clearCache();
        _instance = null;
    }

    public String render404Html() throws Exception {
        Map<String, Object> context = getErrorContext();

        if (getJinjaTemplating().templateExists("404.jinja")) {
            return renderTemplate("404.jinja", context);
        } else {
            URL url = getClass().getClassLoader().getResource("templates/public/404.jinja");
            return renderTemplate(url.toString(), context);
        }
    }

    public String render500Html(Exception e) {
        String friendlyMessage = "There was an error trying to handle your request.";
        if (e instanceof WebApplicationException) {
            friendlyMessage = e.getMessage();
        } else if (e instanceof InvocationTargetException) {
            if (((InvocationTargetException) e).getTargetException() != null) {
                if (((InvocationTargetException) e).getTargetException() instanceof WebApplicationException) {
                    friendlyMessage = ((InvocationTargetException) e).getTargetException().getMessage();
                }
            }
        }
        String error = "";
        if (Context.getSettings().getDebug()) {
            error += "\nStacktrace-----------------------------------\n\n";
            error += e.toString() + "\n\n";
            error += ExceptionUtils.getStackTrace(e);
            error = error.replace("\n", "<br>\n");
        }

        try {
            Map<String, Object> context = getErrorContext();
            context.put("errorDebugMessage", error);
            context.put("friendlyMessage", friendlyMessage);
            if (getJinjaTemplating().templateExists("templates/public/500.jinja")) {
                return renderTemplate("templates/public/500.jinja", context);
            } else {
                URL url = getClass().getClassLoader().getResource("templates/public/500.jinja");
                return renderTemplate(url.toString(), context);
            }
        } catch (Exception e2) {
            Log.warn("---------------Exception trying to render the 500 page-------------");
            Log.exception(e2, "500 rendering exception");
            Log.warn("---------------End 500 rendering exception-------------");
            String error2 = "";
            if (Context.getSettings().getDebug()) {
                error2 += "\nSecond Stacktrace trying to render the error!-----------------------------------\n\n";
                error2 += e2.toString() + "\n\n";
                error2 += ExceptionUtils.getStackTrace(e2);
                error2 = error2.replace("\n", "<br>\n");
            }
            String html = "The server encountered an error trying to handle your request. Please try again in a little bit." + error + error2;
            return html;
        }
    }

    public Map<String, Object> getErrorContext() {
        Map<String, Object> context = new HashMap<>();

        context.put("user", Context.getUser());
        context.put("org", Context.getOrg());
        context.put("request", Context.request());
        Site site = new Site();
        site.setUrl(Context.settings().getSiteUrl());
        AssetsController assetsController = new AssetsController();
        context.put("files", assetsController);
        context.put("assets", assetsController);
        context.put("site", site);
        context.put("styleSettings", Settings.instance().getStyles());
        return context;
    }

    public String renderTemplate(String path) {
        HashMap<String, Object> context = new HashMap<String, Object>();
        return renderTemplate(path, context);
    }

    public String renderTemplate(URL url, Map<String, Object> context) {
        return renderTemplate(url.toString(), context);
    }


    public String renderTemplate(String path, Map<String, Object> context) {
        if (empty(path)) {
            throw new UsageException("No template selected for renderTemplate");
        }
        if (!context.containsKey("user")) {
            context.put("user", Context.getUser());
        }


        context.put("utils", new GeneralUtils());
        context.put("dateUtils", new DateUtils());
        context.put("sanitize", new Sanitize());

        context.put("request", Context.request());
        context.put("now", DateUtils.localNow());
        context.put("styleSettings", Settings.instance().getStyles());

        // TODO: store meta information, css in request?
        MetaInformation meta = new MetaInformation();
        meta.getCssClasses().add("st-template-" + GeneralUtils.slugify(path));
        context.put("meta", meta);

        /*
        if (Context.response() != null) {

            Context.response().getMeta().getCssClasses().add("st-template-" + GeneralUtils.slugify(path));
            context.put("meta", Context.response().getMeta());
        } else {
            context.put("meta", map());
        }
        */

        Site site = new Site();
        site.setTitle(Settings.instance().getDefaultTitle());
        site.setName(Settings.instance().getSiteName());
        site.setUrl(settings().getSiteUrl());
        site.setMetaDescription(settings().getMetaDescription());
        context.put("site", site);
        context.put("env", Settings.instance().getEnv());
        context.put("isProd", "prod".equals(Settings.instance().getEnv()));

        context.put("files", AssetsController.instance());
        context.put("assets", AssetsController.instance());

        for(Map.Entry<String, ModelController> entry: dal().entrySet()) {
            context.put(entry.getKey(), entry.getValue().getReadonlyWrapper());
        }







        if (path.endsWith(".html") || path.endsWith(".jinja") || path.contains("\n")) {
            JinjaTemplating templating = getJinjaTemplating();
            String html = templating.renderTemplate(path, context);
            return html;
        } else {
            throw new UsageException("Unknown extension for template path: " + path);
        }
    }


    public String renderSandboxedTemplate(Sandbox sandbox, String path, Map<String, Object> context) {
        context.put("utils", new GeneralUtils());
        context.put("dateUtils", new DateUtils());
        context.put("sanitize", new Sanitize());
        context.put("now", DateUtils.localNow());

       // TODO: store meta information, css in request?
        MetaInformation meta = new MetaInformation();
        meta.getCssClasses().add("st-template-" + GeneralUtils.slugify(path));
        context.put("meta", meta);

        /*
        if (Context.response() != null) {

            Context.response().getMeta().getCssClasses().add("st-template-" + GeneralUtils.slugify(path));
            context.put("meta", Context.response().getMeta());
        } else {
            context.put("meta", map());
        }
        */



        Site site = new Site();
        site.setTitle(Settings.instance().getDefaultTitle());
        site.setName(Settings.instance().getSiteName());
        site.setUrl(settings().getSiteUrl());
        site.setMetaDescription(settings().getMetaDescription());
        context.put("site", site);
        context.put("env", Settings.instance().getEnv());
        context.put("isProd", "prod".equals(Settings.instance().getEnv()));

        context.put("styleSettings", Settings.instance().getStyles());
        context.put("files", AssetsController.wrapper());
        context.put("assets", AssetsController.wrapper());
        // Request, user, both pre-inserted

        context.put("request", new SandboxedRequest(sandbox, Context.request()));
        if (sandbox.getUsers().isCanAccess()) {
            context.put("user", Context.getUser());
        } else {
            context.put("user", new EmptyUser());
        }
        context.put("lb", "{");

        for(Map.Entry<String, ModelController> entry: dal().entrySet()) {
            if (sandbox.isCanReadAllData() ||
                    sandbox.getWhitelist().getReadBuckets().contains(entry.getKey()) ||
                    sandbox.getWhitelist().getWriteBuckets().contains(entry.getKey())) {
                context.put(entry.getKey(), entry.getValue().getReadonlyWrapper());
            }
        }
        if (path.endsWith(".html") || path.endsWith(".jinja") || path.contains("\n")) {
            JinjaTemplating templating = getJinjaTemplating();
            String html = templating.renderTemplate(path, context);
            return html;
        } else {
            throw new UsageException("Unknown extension for template path: " + path);
        }
    }


    public JinjaTemplating getJinjaTemplating() {
        return templating;
    }
}
