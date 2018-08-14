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

import io.stallion.Context;
import io.stallion.assetBundling.AssetHelpers;
import io.stallion.assets.*;
import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.Displayable;
import io.stallion.dataAccess.DisplayableModelController;
import io.stallion.dataAccess.Model;
import io.stallion.exceptions.*;
import io.stallion.hooks.HookRegistry;
import io.stallion.plugins.javascript.JsEndpoint;

import io.stallion.reflection.PropertyUtils;
import io.stallion.restfulEndpoints.*;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.users.OAuthApprovalController;
import io.stallion.users.UserController;
import io.stallion.users.Role;
import io.stallion.utils.ResourceHelpers;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;
import io.stallion.utils.json.RestrictedViews;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;

import static io.stallion.utils.Literals.*;

/**
 * Does the actual work of processing a Stallion request and
 * sending data to the Stallion response.
 *
 */
class RequestProcessor {
    private StRequest request;
    private StResponse response;


    public RequestProcessor(StRequest request, StResponse response) {
        this.request = request;
        this.response = response;
    }

    public void process() {
        Log.fine("handleRequest:{0}={1}", request.getMethod(), request.getPath());
        try {
            Context.setRequest(request);
            Context.setResponse(response);
            // Check CORS
            new CorsResponseHandler().handleIfNecessary(request, response);
            // Authorize via cookie?
            if (UserController.instance() != null) {
                UserController.instance().checkCookieAndAuthorizeForRequest(request);
            }
            // Authorize via an OAuth bearer token?
            if (!Context.getUser().isAuthorized() && Settings.instance().getoAuth().getEnabled()) {
                OAuthApprovalController.instance().checkHeaderAndAuthorizeUserForRequest(request);
            }
            // Defaults
            response.setDefaultContentType("text/html;charset=utf-8");
            response.setStatus(200);
            if (!"get".equals(request.getMethod().toLowerCase())) {
                response.addCookie(IRequest.RECENT_POSTBACK_COOKIE, String.valueOf(mils()), 15);
            }

            // trigger PreRequest hooks
            HookRegistry.instance().dispatch(
                    PreRequestHookHandler.class,
                    new RequestInfoHolder()
                            .setRequest(request)
                            .setResponse(response));
            // Dispatch the main request processing
            doProcess();

        } catch(ResponseComplete complete) {
            // Do nothing, the response completed successfully
        } catch(RedirectException e) {
            response.addHeader("Location", e.getUrl());
           // response.setStatus(e.getStatusCode());
        } catch(NotFoundException e) {
            try {
                handleNotFound(e.getMessage());
            } catch (Exception ex) {
                handleError(ex);
            }
        } catch(InvocationTargetException e) {
            if (e.getTargetException() instanceof ResponseComplete){
                // Do nothing, response completed successfully
            } else if (e.getTargetException() instanceof RedirectException) {
                RedirectException target = (RedirectException)e.getTargetException();
                response.addHeader("Location", target.getUrl());
                //response.setStatus(target.getStatusCode());
            } else if (e.getTargetException() instanceof NotFoundException) {
                try {
                    handleNotFound(e.getTargetException().getMessage());
                } catch (Exception ex) {
                    handleError(ex);
                }
            } else {
                handleError(e);
            }
        } catch(Exception ex) {
            handleError(ex);
        }

        HookRegistry.instance().dispatch(
                PostRequestHookHandler.class,
                new RequestInfoHolder()
                        .setRequest(request)
                        .setResponse(response));
        XFrameOptionsHandler.handle(request, response);

        Log.fine("status={0} {1}={2}", response.getStatus(), request.getMethod(), request.getPath());

    }


    public void doProcess() throws Exception {
        // Handle login to a page
        if ("true".equals(request.getParameter("stLogin"))
                && empty(Context.getUser().getId())
                && !Settings.instance().getUsers().getDisableStLoginParam()
                ) {
            String targetUrl = Settings.instance().getUsers().getLoginPage() + "?stReturnUrl=" + URLEncoder.encode(request.getRequestUrlWithQuery(), "UTF-8");
            throw new RedirectException(targetUrl, 302);
        }

        // Never hurts to have an extra check to prevent malicious requests
        if (request.getPath().contains("..")) {
            throw new ClientException("Request path contained double periods.", 400);
        }

        // try Asset Serving if the path matches
        tryRouteAssetRequest();

        // try render based on the RouteRegistry
        tryRenderRouteRequest();

        // try render based on the SlugRegistry
        tryRenderForSlug();

        // Try a redirect based on the settings
        String redirectUrl = Settings.instance().getRedirects().getOrDefault(request.getPath(), null);
        if (redirectUrl == null) {
            redirectUrl = Settings.instance().getRedirects().getOrDefault(request.getPath() + "/", null);
        }
        if (redirectUrl != null) {
            throw new RedirectException(redirectUrl, 301);
        }

        // Try a redirect based on old urls/moved pages
        redirectUrl = SlugRegistry.instance().returnRedirectIfExists(request.getPath());
        if (redirectUrl == null) {
            redirectUrl = SlugRegistry.instance().returnRedirectIfExists(request.getPath());
        }
        if (redirectUrl == null) {
            redirectUrl = HookRegistry.instance().find(FindRedirectHook.class, request);
        }
        if (redirectUrl != null) {
            throw new RedirectException(redirectUrl, 301);
        }

        if (request.getPath().equals("/favicon.ico") && new File(Settings.instance().getTargetFolder() + "/assets/favicon.ico").exists()) {
            throw new RedirectException("/st-assets/favicon.ico", 301);
        }


        markHandled(404, "No route matches");
        throw new NotFoundException("The page you requested could not be found.");

    }

    /******************************************
     * Slug routing
     */

    /**
     * Tries to route the request using a slug registered with the SlugRegistry
     *
     * Short circuits if the request is served, otherwise does nothings and returns
     *
     * @throws Exception
     */
    public void tryRenderForSlug() throws Exception {
        Displayable item = null;
        if (SlugRegistry.instance().hasUrl(request.getPath())) {
            item = SlugRegistry.instance().lookup(request.getPath());
        }
        if (item == null) {
            item = HookRegistry.instance().find(DisplayableBySlugHook.class, request);
        }

        if (item == null) {
            return;
        }


        Model baseItem = (Model)item;

        // Item has an override domain, but we are accessing from a different domain
        if (!empty(item.getOverrideDomain())) {
            if (!item.getOverrideDomain().equals(request.getHost())) {
                 return;
            }
        }

        // If the item is unpublished, return, unless we are a logged in staff user viewing the article preview
        if (!item.getPublished()) {
            String previewKey = request.getQueryParams().getOrDefault("stPreview", null);
            // No preview key in the query string, abort rendering
            if (previewKey == null) {
                return;
            }
            if (!request.getUser().isInRole(Role.STAFF_LIMITED)) {
                // Non-staff user with invalid preview key, abort rendering
                if (empty(item.getPreviewKey()) || !previewKey.equals(item.getPreviewKey())) {
                    return;
                }
            }
        }

        // In local mode, check for newer version from the file system so we do not load stale items
        if (Settings.instance().getLocalMode()) {
            if (!baseItem.getController().getPersister().isDbBacked()) {
                baseItem = baseItem.getController().getStash().reloadIfNewer(baseItem);
                item = (Displayable)baseItem;
            }
        }


        Map ctx = map(val("page", item), val("post", item), val("item", item));
        response.getMeta().setDescription(item.getMetaDescription());
        if (!empty(item.getTitleTag())) {
            response.getMeta().setTitle(item.getTitleTag());
        } else {
            response.getMeta().setTitle(item.getTitle());
        }
        response.getMeta().setBodyCssId(item.getSlugForCssId());
        response.getMeta().getCssClasses().add("st-" + ((ModelBase) item).getController().getBucket());
        response.getMeta().setOgType(item.getOgType());
        if (!empty(item.getRelCanonical())) {
            response.getMeta().setCanonicalUrl(item.getRelCanonical());
        }
        if (!empty(item.getContentType())) {
            response.setContentType(item.getContentType());
        }
        markHandled(200, MessageFormat.format("slug->displayableItemController for id={0} slug={1} controller={2}", baseItem.getId(), item.getSlug(), baseItem.getController().getClass().getName()));
        String output = ((DisplayableModelController)baseItem.getController()).render(item, ctx);
        sendContentResponse(output);
    }



    /*******************************************
     * Endpoint Routing
     */

    /**
     * Tries to route the request using an endpoint registered with RoutesRegistry
     *
     * Short circuits if the request is routed, otherwise returns and does nothing.
     *
     * @throws Exception
     */
    public void tryRenderRouteRequest() throws Exception {
        RouteResult result = RoutesRegistry.instance().route(request.getPath());
        if (result == null) {
            result = RoutesRegistry.instance().routeForEndpoints(request, EndpointsRegistry.instance().getEndpoints());
        }
        if (result == null) {
            return;
        }
        String output;
        if (result.getEndpoint() != null) {
            response.setContentType(result.getEndpoint().getProduces());
            if ("application/json".equals(result.getEndpoint().getProduces())) {
                request.setIsJsonRequest(true);
            }
            output = dispatchWsEndpoint(result);
            sendContentResponse(output);
        } else if (!empty(result.getRedirectUrl())) {
            markHandled(301, "configured 301-route to " + result.getRedirectUrl());
            throw new RedirectException(result.getRedirectUrl(), 301);
        } else if (result != null) {
            response.getMeta().setTitle(result.getPageTitle());
            response.getMeta().setDescription(result.getMetaDescription());
            markHandled(200, MessageFormat.format("templateRenderer for template {0}", result.getTemplate()));
            output = TemplateRenderer.instance().renderTemplate(result.getTemplate(), routeResultToContext(result));
            sendContentResponse(output);
        }

    }


    public String dispatchWsEndpoint(RouteResult result) throws Exception {
        RestEndpointBase endpoint = result.getEndpoint();

        if (endpoint instanceof JavaRestEndpoint) {
            ((JavaRestEndpoint) endpoint).getResource().preRequest(
                    (JavaRestEndpoint)endpoint,
                    request,
                    response
            );
        }

        if (!XSRFHooks.checkXsrfAllowed(request, endpoint)) {
            throw new ClientException("This request was blocked by the Cross-Site Request Forgery checker. Make sure you have a header called X-XSRF-TOKEN that matches the value of the cookie XSRF-TOKEN.");
        }
        if (!Context.currentUserCanAccessEndpoint(endpoint)) {
            if ("text/html".equals(endpoint.getProduces()) && !Context.getUser().isAuthorized()) {
                throw new RedirectException(Settings.instance().getUsers().getLoginPage() + "?stReturnUrl=" + URLEncoder.encode(request.requestUrl(), "utf-8"), 302);
            }
            if (empty(Context.getUser().getId())) {
                throw new ClientException("You must be logged in as an authorized user to view this content.", 403);
            } else if (!Context.getUser().getApproved()) {
                throw new ClientException("Your user has not yet been approved. You cannot access this content.", 403);
            } else {
                throw new ClientException("You do not have the privileges to access this content.", 403);
            }
        }
        List<Object> methodArgs = new ArrayList();
        for(RequestArg arg: endpoint.getArgs()) {
            Log.finest("ParamType: {0}", arg.getType());
            Object val = null;
            switch(arg.getType()) {
                case "PathParam":
                    val = result.getParams().get(arg.getName());
                    break;
                case "QueryParam":
                    val = request.getParameter(arg.getName());
                    break;
                case "BodyParam":
                    val = request.getBodyParam(arg.getName());
                    break;
                case "ObjectParam":
                    val = new RequestObjectConverter(arg, request).convert();
                    break;
                case "MapParam":
                    val = request.getBodyMap();
                    break;
                default:
                    val = arg.getDefaultValue();
            }
            if (arg.getTargetClass() != null && val != null && !"ObjectParam".equals(arg.getType())) {
                try {
                    val = PropertyUtils.transform(val, arg.getTargetClass());
                } catch (Exception e) {
                    throw new ClientException("The argument: " + arg + " could not be coerced to type " + arg.getTargetClass().getSimpleName(), 400);
                }
            }
            if ("BodyParam".equals(arg.getType())) {
                validateRequestArgument(arg, val);
            }
            methodArgs.add(val);
        }
        Log.finest("PositionalArguments: count={0} values={1}", methodArgs.size(), methodArgs);
        for(Object arg:methodArgs) {
            if (arg != null) {
                Log.finest("Arg: class={0} value={1}", arg.getClass().getName(), arg);
            } else {
                Log.finest("Arg: null");
            }
        }
        Object out = null;
        Object[] argsArray = methodArgs.toArray();
        if (endpoint instanceof JavaRestEndpoint) {
            JavaRestEndpoint javaRestEndpoint = (JavaRestEndpoint) endpoint;
            Method javaMethod = javaRestEndpoint.getJavaMethod();
            List<Object> coercedArgs = list();
            int x = 0;
            Class[] paramTypes = javaMethod.getParameterTypes();
            for(Object arg: methodArgs) {
                if (x >= paramTypes.length) {
                    throw new UsageException("Passed in " + methodArgs.size() + " arguments to the method " + javaMethod.getName() + " but the method only accepts " + paramTypes.length + " arguments.");
                }
                Class type = paramTypes[x];
                Object transformed;
                try {
                    transformed = PropertyUtils.transform(arg, type);
                } catch (Exception e) {
                    throw new ClientException("The argument: " + arg + " could not be coerced to type " + type.getSimpleName(), 400);
                }
                //Log.info("Coercering arg {0} {1} to type {2} result is {3}:{4}", x, arg, type.getSimpleName(), transformed.getClass().getSimpleName(), transformed);
                coercedArgs.add(transformed);
                x++;
            }
            Object[] coercedArgsArray = coercedArgs.toArray();
            logHandled(request, "java-endpoint:{0}:{1}({2})", javaRestEndpoint.getRoute(), javaRestEndpoint.getJavaMethod().getName(), paramTypes);
            out = javaRestEndpoint.getJavaMethod().invoke(javaRestEndpoint.getResource(), coercedArgsArray);
            if (endpoint instanceof JavaRestEndpoint) {
                ((JavaRestEndpoint) endpoint).getResource().postRequest(
                        (JavaRestEndpoint)endpoint,
                        request,
                        response,
                        out
                );
            }

        } else {
            JsEndpoint jsEndpoint = (JsEndpoint)endpoint;
            // There has to be a better way to do this ... but I don't know what it is
            // creating a variable args method on the interface simply passes the javascript an array as the first argument
            logHandled(request, "javascript-endpoint:{0}", jsEndpoint.getRoute());
            if (argsArray.length == 0) {
                out = jsEndpoint.getHandler().handle();
            } else if (argsArray.length == 1) {
                out = jsEndpoint.getHandler().handle(argsArray[0]);
            } else if (argsArray.length == 2) {
                out = jsEndpoint.getHandler().handle(argsArray[0], argsArray[1]);
            } else if (argsArray.length == 3) {
                out = jsEndpoint.getHandler().handle(argsArray[0], argsArray[1], argsArray[2]);
            }  else if (argsArray.length == 4) {
                out = jsEndpoint.getHandler().handle(argsArray[0], argsArray[1], argsArray[2], argsArray[3]);
            } else if (argsArray.length == 5) {
                out = jsEndpoint.getHandler().handle(argsArray[0], argsArray[1], argsArray[2], argsArray[3], argsArray[4]);
            } else if (argsArray.length == 6) {
                out = jsEndpoint.getHandler().handle(argsArray[0], argsArray[1], argsArray[2], argsArray[3], argsArray[4], argsArray[5]);
            } else {
                throw new RuntimeException("A javascript handler cannot have more than 6 arguments. Try making one of your arguments a dictionary or object instead!");
            }

        }
        if (out == null) {
            return "";
        }

        return responseObjectToString(out, endpoint);
    }

    private void validateRequestArgument(RequestArg arg, Object value) {
        if (!arg.isRequired() && value == null) {
            return;
        }
        if (arg.isRequired() && value == null) {
            throw new ClientException("Body field " + arg.getName() + " must be provided.");
        }
        if (!arg.isAllowEmpty()) {
            if (emptyObject(value)) {
                throw new ClientException("Body field " + arg.getName() + " must be non-empty.");
            }
        }
        if (arg.getMinLength() > 0) {
            if (value instanceof String) {
                if (((String) value).length() < arg.getMinLength()) {
                    throw new ClientException("Field " + arg.getName() + " must have at least " + arg.getMinLength() + " characters.");
                }
            } else if (value instanceof Collection) {
                if (((Collection) value).size() < arg.getMinLength()) {
                    throw new ClientException("Field " + arg.getName() + " must have at least " + arg.getMinLength() + " entries.");
                }
            }
        }
        if (arg.isEmailParam()) {
            String email = (String)value;
            if (!GeneralUtils.isValidEmailAddress(email)) {
                throw new ClientException("Field " + arg.getName() + " must be a valid email address.");
            }
        }
        if (arg.getValidationPattern() != null) {
            String s = (String)value;
            if (!arg.getValidationPattern().matcher(s).matches()) {
                throw new ClientException("Field " + arg.getName() + " does not pass validation.");
            }
        }
    }

    String responseObjectToString(Object obj, RestEndpointBase endpoint) throws Exception {

        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof jdk.nashorn.internal.runtime.ConsString) {
            return obj.toString();
        } else {
            if (obj instanceof Boolean) {
                return JSON.stringify(map(val("succeeded", obj)));
            } else if (endpoint.getJsonViewClass() == null) {
                return JSON.stringify(obj, RestrictedViews.Member.class, false);
            } else if (endpoint.getJsonViewClass().isAssignableFrom(RestrictedViews.Unrestricted.class)) {
                return JSON.stringify(obj);
            }  else {
                return JSON.stringify(obj, endpoint.getJsonViewClass(), false);
            }
        }
    }

    private void sendContentResponse(String content) throws Exception {
        response.getWriter().print(content);
        complete();
    }



    /*******************************************
     * Asset Routing
     ******************************************/

    /**
     * If the path matches one our asset routing functions, serve the asset.
     * Else, do nothing.
     */
    public void tryRouteAssetRequest() throws Exception{
        return;

        /*
        if (request.getPath().startsWith("/st-assets") && "true".equals(request.getParameter("isConcatenatedFileBundle"))) {
            serveFileBundle();
        } else if (request.getPath().startsWith("/st-assets") && "true".equals(request.getParameter("isBundleFile"))) {
            serveFileBundleAsset();
        } else if (request.getPath().startsWith("/st-resource") && "true".equals(request.getParameter("isFullResourceBundle"))) {
            serveResourceBundle();
        } else if (request.getPath().startsWith("/st-resource/")) {
            serveResourceAsset();

        } else if (request.getPath().startsWith("/st-assets/")) {
            serveFolderAsset(request.getPath().substring(11));
        // Deprecated
        } else if (request.getPath().startsWith("/st-resource-bundle/")) {
            serveResourceBundle();
        // Deprecated
        } else if (request.getPath().startsWith("/st-bundle-v2/")) {
            serveResourceBundle();
        }
         */
    }

    public void serveFileBundleAsset() throws Exception {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        path = path.substring(i + 1);

        FileSystemAssetBundleRenderer br = new FileSystemAssetBundleRenderer(path);
        String filePath = request.getParameter("bundleFilePath");
        String content = br.renderFile(filePath);
        sendContentResponse(content, request.getPath());
    }

    public void serveFileBundle() throws Exception {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        path = path.substring(i + 1);

        FileSystemAssetBundleRenderer br = new FileSystemAssetBundleRenderer(path);
        sendContentResponse(br.renderProductionContent(), path);
    }

    public void serveResourceAsset() throws Exception  {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        String assetPath = path.substring(i + 1);

        String[] parts = assetPath.split("/", 2);
        String plugin = parts[0];
        assetPath = parts[1];
        if (parts.length < 2) {
            throw new ClientException("Invalid resource path " + assetPath);
        }
        assetPath = "/" + assetPath;

        if (request.getQueryParams().containsKey("bundlePath")) {
            String bundlePath = AssetsController.ensureSafeAssetsPath(request.getQueryParams().get("bundlePath"));
            URL url = ResourceHelpers.getUrlOrNotFound(plugin, bundlePath);
            String content = null;
            if (Settings.instance().getDevMode()) {
                File file = ResourceHelpers.findDevModeFileForResource(plugin, bundlePath);
                if (file != null) {
                    content = AssetHelpers.renderDebugModeBundleFileByPath(file.getAbsolutePath(), assetPath);
                }
            }
            if (empty(content)) {
                content = AssetHelpers.renderDebugModeBundleFileByPath(url.getPath(), assetPath);
            }
            sendContentResponse(content, assetPath);
        } else {
            assetPath = AssetsController.ensureSafeAssetsPath(assetPath);
            if (!empty(request.getParameter("processor"))) {
                String content = ResourceHelpers.loadAssetResource(plugin, assetPath);
                //if (!empty(request.getParameter("nocache"))) {
                //content = AssetsController.instance().convertUsingProcessorNoCache(request.getParameter("processor"), path, content);
                //} else {
                //content = AssetsController.instance().convertUsingProcessor(request.getParameter("processor"), assetPath, content);
                //}
                markHandled(200, "resource-asset");
                sendContentResponse(content);
            } else {
                markHandled(200, "resource-asset");
                URL url = ResourceHelpers.pluginPathToUrl(plugin, assetPath);
                // If not found, and no referer, throw generic 404
                if (url == null && empty(request.getHeader("Referer"))) {
                    throw new NotFoundException("Asset resource not found: " + plugin + ":" + assetPath);
                } else if (url == null) {
                    // If not found, and referer, may mean there is a bug
                    throw new WebException("Requested linked resource that is not found: " + plugin + ":" + assetPath);
                }
                new ServletFileSender(request, response).sendResource(url, assetPath);
                complete();
            }
        }
    }

    public void serveResourceBundle() throws Exception {
        String path = request.getPath();
        int i = path.indexOf("/st-");
        i = path.indexOf("/", i + 3);
        path = path.substring(i + 1);
        String[] parts = path.split("/", 2);
        String plugin = parts[0];
        path = parts[1];
        path = AssetsController.ensureSafeAssetsPath(path);
        String content = new ResourceAssetBundleRenderer(plugin, path).renderProductionContent();
        sendContentResponse(content, path);
    }

    public void serveFolderAsset(String path) throws Exception  {
        String fullPath = Settings.instance().getTargetFolder() + "/assets/" + path;
        Log.fine("Asset path={0} fullPath={1}", path, fullPath);
        File file = new File(fullPath);
        String preProcessor = request.getParameter("preprocessor");
        if (!file.isFile() && empty(preProcessor)) {
            notFound("Asset for path " + path + " is not found.");
        }
        if (!empty(request.getParameter("processor"))) {
            String contents = IOUtils.toString(new FileReader(fullPath));
            //contents = AssetsController.instance().convertUsingProcessor(request.getParameter("processor"), path, contents);
            markHandled(200, "folder-asset-with-processor");
            sendContentResponse(contents, file.lastModified(), fullPath);
        } else {
            //if (!empty(preProcessor)) {
            //    AssetsController.instance().externalPreprocessIfNecessary(preProcessor, path);
            //}
            markHandled(200, "folder-asset");
            sendAssetResponse(file);
        }
    }

    public void sendContentResponse(String content, String fullPath)  {
        sendContentResponse(content, 0, fullPath);
    }

    public void sendContentResponse(String content, long modifyTime, String fullPath)  {
        new ServletFileSender(request, response).sendContentResponse(content, modifyTime, fullPath);
        complete();
    }

    public void sendAssetResponse(File file) {
        try {
            sendAssetResponse(new FileInputStream(file), file.lastModified(), file.length(), file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAssetResponse(InputStream stream, long modifyTime, long contentLength, String fullPath)  {
        new ServletFileSender(request, response).sendAssetResponse(stream, modifyTime, contentLength, fullPath);
        complete();
    }






    /*********************
    /* Error handlers */
    /********************/


    public void handleError(Exception ex) {

        Log.exception(ex, "Error handling request " + request.getMethod() + " " + request.getPath());
        String out = "Server error handling request.";
        String message = "Server error handling request";
        Throwable anEx = ex;
        int status = 500;
        Map extra = null;
        for (int x =0; x < 10; x++) {  // because while loops are evil
            if (anEx instanceof WebException) {
                WebException webEx = (WebException) anEx;
                message = webEx.getMessage();
                status = webEx.getStatusCode();
                extra = webEx.getExtra();
            } else {
                anEx = anEx.getCause();
            }
            if (anEx == null) {
                break;
            }
        }
        markHandled(status, message);
        if (isJsonRequest()) {
            Map<String, Object> result = new HashMap<>();
            result.put("succeeded", false);
            result.put("message", message);
            if (Context.getSettings().getDebug()) {
                result.put("debug", ex.toString());
            }
            if (extra != null) {
                result.putAll(extra);
            }
            try {
                out = JSON.stringify(result);
                response.addHeader("Content-type", "application/json");
            } catch (Exception e) {
                Log.exception(e, "Error stringifying");
            }
        } else {
            out = TemplateRenderer.instance().render500Html(ex);
        }
        try {
            response.getWriter().print(out);
        } catch (IOException e) {
            Log.exception(e, "Exception writing output to response stream");
            throw new RuntimeException(e);
        }
    }

    public void handleNotFound() throws Exception {
        handleNotFound("");
    }

    public void handleNotFound(String message) throws Exception {
        markHandled(404, "Page not found.");
        String out = "Page not found.";
        message = or(message, "Page not found.");
        if (isJsonRequest()) {
            Map<String, Object> result = new HashMap<>();
            result.put("succeeded", false);
            result.put("message", message);
            try {
                out = JSON.stringify(result);
                response.addHeader("Content-type", "application/json");
            } catch (Exception e) {
                Log.exception(e, "Error stringifying");
            }
        } else {
            out = TemplateRenderer.instance().render404Html();
        }
        response.getWriter().print(out);
    }

    /*********************
     /* Logging */
    /********************/


    private void logHandled(int status, StRequest request, String msg, Object...args) {
        msg = MessageFormat.format(msg, args);
        Log.logForFrame(2, Level.FINE, "status={0} handler=\"{1}\" {2}={3}", status, msg, request.getMethod(), request.getPath());
    }

    private void logHandled(StRequest request, String msg, Object...args) {
        msg = MessageFormat.format(msg, args);
        Log.logForFrame(2, Level.FINE, "handler=\"{0}\" {1}={2}", msg, request.getMethod(), request.getPath());
    }

    public void markHandled(int status, String message) {
        response.setStatus(status);
        Log.logForFrame(2, Level.FINE, "status={0} handler=\"{1}\" {2}={3}", status, message, request.getMethod(), request.getPath());
    }

    public void notFound(String message) {
        markHandled(404, message);
        throw new NotFoundException(message);
    }

    public void complete(int status, String message, Object...args) {
        markHandled(status, message);
        throw new ResponseComplete();
    }

    public void complete() {
        throw new ResponseComplete();
    }


    /*********************
     /* Helpers */
    /********************/


    /**
     * Excutes a bunch of heuristics to figure out if we should return errors
     * as HTML or JSON
     * @return
     */
    public Boolean isJsonRequest() {

        if (request.getIsJsonRequest() != null) {
            return request.getIsJsonRequest();
        }
        if (response.isContentTypeSet() && !StringUtils.isEmpty(response.getContentType())) {
            if ("application/json".equals(response.getContentType())) {
                return true;
            } else {
                return false;
            }
        }
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true;
        }
        String contentType = request.getHeader("Content-type");
        if (!StringUtils.isEmpty(contentType)) {
            if (contentType.contains("application/json")) {
                return true;
            }
        }
        if ("POST".equals(request.getMethod())) {
            return true;
        }
        return false;
    }


    public HashMap<String, Object> routeResultToContext(RouteResult result) throws Exception {
        //val context = HashMap<String, kotlin.Any>()
        HashMap<String, Object> context = new HashMap<String, Object>();
        RouteResult routeResult = new RouteResult()
                .setParams(new HashMap<String, String>())
                .setTemplate("")
                .setRedirectUrl("")
                .setPreempt(false)
                .setName("")
                .setGroup("");
        if (result != null) {
            routeResult = result;
        }
        HashMap route = new HashMap<String, String>();
        route.put("name", routeResult.getName());
        route.put("group", routeResult.getGroup());
        for(Map.Entry<String, String> entry: routeResult.getParams().entrySet()) {
            route.put(entry.getKey(), entry.getValue());

        }
        context.put("route", route);
        return context;
    }





    public StRequest getRequest() {
        return request;
    }

    public void setRequest(StRequest request) {
        this.request = request;
    }

    public StResponse getResponse() {
        return response;
    }

    public void setResponse(StResponse response) {
        this.response = response;
    }
}
