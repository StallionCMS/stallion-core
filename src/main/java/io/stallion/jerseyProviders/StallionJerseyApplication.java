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

package io.stallion.jerseyProviders;

import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.stallion.assets.AssetsEndpointsResource;
import io.stallion.contentPublishing.ContentPublishingBooter;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.settings.Settings;
import io.stallion.users.UsersApiResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.CsrfProtectionFilter;

import java.util.Map;
import java.util.logging.Level;


public class StallionJerseyApplication extends ResourceConfig {
    public StallionJerseyApplication () {

        // Disable looking up services from METAINF since this leads to inconsistent behavior
        // between running via mvn and running via a compiled package. Inconsistency is always
        // bad.
        property(org.glassfish.jersey.CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);

        for (Map.Entry<String, String> param: Settings.instance().getJerseyInitParams().entrySet()) {
            property(param.getKey(), param.getValue());
        }

        //property("jersey.config.server.tracing.type", "ALL");
        //property("jersey.config.server.tracing.threshold", "VERBOSE");

        //property("org.glassfish.jersey.tracing.handler", "java.util.logging.ConsoleHandler");
        //register(new LoggingFeature());
        {
            //Logger logger = Logger.getLogger("io.stallion.jerseyProvier");
            //ConsoleHandler handler = new ConsoleHandler();
            //handler.setLevel(Level.FINEST);
            //logger.addHandler(handler);
            register(new LoggingFeature(
                    io.stallion.services.Log.getLogger(),
                    Level.FINEST,
                    LoggingFeature.Verbosity.HEADERS_ONLY,
                    1000000
            ));
        }



        register(MultiPartFeature.class);

        register(JacksonFeature.class);

        register(new BodyParamProvider<>());
        register(AssetsEndpointsResource.class);

        // Disabled, because wasn't being picked up by fat jar,
        // we have to register everything manually
        //packages("io.stallion.jerseyProviders");


        register(CsrfProtectionFilter.class);
        register(AssetsEndpointsResource.class);
        register(BodyParamProvider.class);
        register(CookiesAndHeadersResponseFilter.class);
        register(CorsRequestFilter.class);
        register(CorsResponseHandler.class);
        register(DefaultExceptionMapper.class);
        register(EndpointAuthorizationRequestFilter.class);

        register(HealthTrackingResponseFilter.class);
        register(InternalRewriteRequestFilter.class);
        register(PopulateContextRequestFilter.class);
        register(PostbackResponseFilter.class);
        register(ProducesDetectionRequestFilter.class);

        register(ResponseCompleteExceptionMapper.class);
        register(TearDownContextResponseFilter.class);
        register(UserAuthenticationRequestFilter.class);
        register(WebApplicationExceptionMapper.class);
        register(XFrameOptionsResponseFilter.class);


        ContentPublishingBooter.boot(this);
        UsersApiResource.registerMaybe(this);

        for (StallionJavaPlugin plugin: PluginRegistry.instance().getJavaPluginByName().values()) {
            plugin.buildResourceConfig(this);
        }


    }

}