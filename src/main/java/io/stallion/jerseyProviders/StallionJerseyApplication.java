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

import io.stallion.assets.AssetsEndpointsResource;
import io.stallion.contentPublishing.ContentPublishingBooter;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.users.UsersApiResource;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.CsrfProtectionFilter;

import java.util.logging.Level;


public class StallionJerseyApplication extends ResourceConfig {
    public StallionJerseyApplication () {


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






        register(new BodyParamProvider<>());
        register(AssetsEndpointsResource.class);
        register(CsrfProtectionFilter.class);
        packages("io.stallion.assets");
        packages("io.stallion.jerseyProviders");

        ContentPublishingBooter.boot(this);
        UsersApiResource.registerMaybe(this);

        for (StallionJavaPlugin plugin: PluginRegistry.instance().getJavaPluginByName().values()) {
            plugin.buildResourceConfig(this);
        }


    }

}
