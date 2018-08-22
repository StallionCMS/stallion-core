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

package io.stallion.monitoring;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.stallion.asyncTasks.AsyncTaskController;
import io.stallion.jerseyProviders.MinRole;
import io.stallion.jerseyProviders.XSRF;
import io.stallion.jobs.JobCoordinator;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.users.Role;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;

import static io.stallion.Context.request;
import static io.stallion.Context.settings;
import static io.stallion.utils.Literals.*;

@Path("/st-internal")
@Produces("application/json")
public class InternalEndpoints  {


    @GET
    @Path("/health")
    public Response checkHealth(@QueryParam("secret") String secret, @QueryParam("failOnWarnings") Boolean failOnWarnings, @QueryParam("sections") String sections) {
        failOnWarnings = or(failOnWarnings, false);
        sections = or(sections, "all");
        checkSecret(secret);
        HealthInfo info = buildHealthInfo(sections);
        int status = 200;
        if (info.getErrors().size() > 0) {
            status = 515;
        }
        if (failOnWarnings && info.getWarnings().size() > 0) {
            status = 515;
        }
        info.setHttpStatusCode(status);
        return Response.status(status).entity(info).build();
    }

    @POST
    @Path("/run-job")
    @XSRF(false)
    public Object runJob(@QueryParam("secret") String secret, @QueryParam("job") String job) {
        checkSecret(secret);
        JobCoordinator.instance().forceRunJob(job, true);
        return true;
    }


    private HealthInfo buildHealthInfo(String sectionsString) {
        List<String> sections = Arrays.asList(sectionsString.split(","));
        if ("all".equals(sectionsString)) {
            sections = list("http", "jobs", "tasks", "endpoints", "system");
        }
        HealthInfo info = new HealthInfo();

        if (sections.contains("http")) {
            info.setHttp(HealthTracker.instance().getHttpHealthInfo());
        }
        if (sections.contains("jobs")) {
            info.setJobs(JobCoordinator.instance().buildJobHealthInfos());
        }
        if (sections.contains("tasks")) {
            info.setTasks(AsyncTaskController.instance().buildHealthInfo());
        }
        if (sections.contains("endpoints")) {
            info.setEndpoints(checkEndpointHealth());
        }
        if (sections.contains("system")) {
            info.setSystem(new SystemHealth().hydrateSystemHealth());
        }

        info.hydrateErrors();

        return info;
    }

    private void checkSecret(String secret) {
        if (empty(settings().getHealthCheckSecret())) {
            throw new ClientErrorException("You must define a setting value for 'healthCheckSecret' in your stallion.toml. This should be a random string that only you know. Then you should pass this in via the query string parameter 'secret' or the header 'X-Healthcheck-Secret", 500);
        }
        if (empty(secret)) {
            secret = request().getHeader("X-Healthcheck-Secret");
        }
        if (empty(secret)) {
            throw new ClientErrorException("You must pass in a either a query param 'secret' or a header 'X-Healthcheck-Secret'. The passed in secret should match the value defined in stallion.toml for the setting name 'healthCheckSecret'.  ", 403);
        }
        if (!secret.equals(Settings.instance().getHealthCheckSecret())) {
            throw new ClientErrorException("Invalid healthcheck secret", 400);
        }
    }

    private List<EndpointHealthInfo> checkEndpointHealth() {
        List<EndpointHealthInfo> infos = new ArrayList<>();
        for (String[] endPointArray: settings().getHealthCheckEndpoints()) {
            EndpointHealthInfo endPointHealth = new EndpointHealthInfo();
            infos.add(endPointHealth);
            endPointHealth.setUrl(endPointArray[0]);
            HttpResponse<String> httpResponse = null;
            try {
                httpResponse = Unirest.get("http://localhost:" + settings().getPort() + "" + endPointArray[0]).asString();
            } catch (UnirestException e) {
                endPointHealth.setStatusCode(999);
                continue;
            }
            endPointHealth.setStatusCode(httpResponse.getStatus());
            if (endPointArray.length > 1) {
                endPointHealth.setFoundString(httpResponse.getBody().contains(endPointArray[1]));
            } else {
                endPointHealth.setFoundString(true);
            }
        }
        return infos;
    }

    @GET
    @Path("/info")
    public SystemInformation getInfo(@QueryParam("secret") String secret) {
        checkSecret(secret);
        SystemInformation info = new SystemInformation();
        info.setTargetPath(settings().getTargetFolder());
        info.setxRealIp(request().getHeader("X-Real-Ip"));
        info.setxForwardedFor(request().getHeader("x-Fowarded-For"));
        info.setGuessedHost(request().getHost());
        info.setGuessedScheme(request().getScheme());
        info.setxForwardedProto(request().getHeader("X-Forwarded-Proto"));
        info.setRemoteAddr(request().getRemoteAddr());
        info.setDeployDate(System.getenv("STALLION_DEPLOY_TIME"));
        info.setGuessedIp(request().getActualIp());
        info.setInstanceHostName(System.getenv("STALLION_HOST"));
        info.setInstanceDomain(System.getenv("STALLION_DOMAIN"));
        info.setxForwardedHost(request().getHeader("x-forwarded-host"));
        info.setxUpstreamForwardedProto(request().getHeader("x-upstream-forwarded-proto"));
        info.setEnv(Settings.instance().getEnv());
        info.setPort(settings().getPort());
        List<ClassLoader> loaders = new ArrayList<>();
        loaders.add(getClass().getClassLoader());
        for (StallionJavaPlugin booter: PluginRegistry.instance().getJavaPluginByName().values()) {
            loaders.add(booter.getClass().getClassLoader());
        }
        for (ClassLoader loader: loaders) {
            try {
                Enumeration<URL> resources = loader.getResources("META-INF/MANIFEST.MF");
                for (Integer i : safeLoop(1000)) {
                    if (!resources.hasMoreElements()) {
                        break;
                    }
                    URL url = resources.nextElement();
                    Manifest manifest = new Manifest(url.openStream());
                    String buildTime = manifest.getMainAttributes().getValue("Build-Time");
                    String key = url.toString();
                    if (!empty(buildTime)) {
                        info.getJarBuildDates().put(key, buildTime);
                    }
                }
            } catch (IOException ex) {
                Log.exception(ex, "Error loading MANIFEST.MF");
            }
        }
        return info;
    }

    @GET
    @Path("/exceptions")
    @MinRole(value = Role.ADMIN, redirect = true)
    @Produces("text/html")
    public String viewExceptions() {
        Map<String, Object> context = map();
        ArrayList<ExceptionInfo> exceptions = new ArrayList<>();
        for(ExceptionInfo info: HealthTracker.instance().getExceptionQueue()) {
            exceptions.add(info);
        }
        Collections.reverse(exceptions);
        context.put("exceptions", exceptions);
        return TemplateRenderer.instance().renderTemplate(getClass().getResource("/templates/exceptions.jinja"), context);
    }

    @POST
    @Produces("text/html")
    @Path("/force-exception")
    @XSRF(false)
    public String forceException(@QueryParam("secret") String secret) {
        checkSecret(secret);
        throw new WebApplicationException("A forced exception!", 500);
    }

    @GET
    @Produces("text/html")
    @Path("/force-exception-get")
    @XSRF(false)
    public String forceExceptionGet(@QueryParam("secret") String secret) {
        checkSecret(secret);
        throw new WebApplicationException("A forced exception!", 500);
    }
}
