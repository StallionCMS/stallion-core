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

package io.stallion.boot;

import io.stallion.Context;
import io.stallion.asyncTasks.AsyncCoordinator;
import io.stallion.contentPublishing.SlugRegistry;
import io.stallion.jerseyProviders.StallionJerseyApplication;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.settings.Settings;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.text.MessageFormat;


public class StallionServer implements StallionRunAction<ServeCommandOptions> {

    @Override
    public String getActionName() {
        return "serve";
    }

    @Override
    public String getHelp() {
        return "run the Stallion HTTP server";
    }

    @Override
    public void loadApp(ServeCommandOptions options) {
        AppContextLoader.loadCompletely(options);
        AppContextLoader.instance().startAllServices();
    }

    @Override
    public ServeCommandOptions newCommandOptions() {
        return new ServeCommandOptions();
    }

    public static ResourceConfig buildResourceConfig() {
        return new StallionJerseyApplication();
    }

    @Override
    public void execute(ServeCommandOptions options) throws Exception {

        // TODO: share the stallion logger
        // https://stackoverflow.com/questions/25786592/how-to-enable-logging-in-jetty
        //System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        //System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG");

        //Log.setLog(new StdErrLog());
        //Log.setLog(new JavaUtilLog());



        // Start the server
        Server server = new Server(Settings.instance().getPort());

        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.setServer(server);
        handlerCollection.setHandlers(new Handler[] {});



        for(StallionJavaPlugin plugin: PluginRegistry.instance().getJavaPluginByName().values()) {
            plugin.preStartJetty(server, handlerCollection, options);
        }


        // Initialize Jersey and add Jersey context as the default handler
        ResourceConfig rc = buildResourceConfig();

        ServletContextHandler ctx =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

        ctx.setInitParameter("com.sun.jersey.config.feature.Trace", "true");

        //ctx.setInitParameter("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature");



        ctx.setContextPath("/");
        ServletContainer sc = new ServletContainer(rc);
        ctx.addServlet(new ServletHolder(sc), "/*");

        handlerCollection.addHandler(ctx);





        // Add the handler collection and start the server
        server.setHandler(handlerCollection);

        server.start();

        System.out.print("-------------------------------------------------------\n");
        System.out.print(MessageFormat.format(
                "Final registration count: {1} controllers. {2} plugins. {3} slugs. V=3\n",
                Settings.instance().getPort(),
                Context.dal().size(),
                PluginRegistry.instance().getJavaPluginByName().size(),
                SlugRegistry.instance().getSlugMap().size()
        ));


        // If we are in debug mode, file system changes will send a signal, which
        // will then be caught, triggering a reload.
        if (MainRunner.isIsDebugRunner()) {
            sun.misc.Signal.handle(new Signal("INT"), new InterruptHandler(server));
            sun.misc.Signal.handle(new Signal("USR2"), new ReloadHandler(server));
            MainRunner.setupWatchers(Settings.instance().getTargetFolder());
        }

        for(StallionJavaPlugin plugin: PluginRegistry.instance().getJavaPluginByName().values()) {
            plugin.postStartJetty(server, options);
        }


        System.out.print("-------------------------------------------------------\n");
        String art = "" +
                "         _,_\n" +
                "        ;'._\\\n" +
                "       ';) \\._,     Stallion server now running on port " + Settings.instance().getPort() + ".\n" +
                "        /  /`-'\n" +
                "     ~~( )/\n" +
                "        )))\n" +
                "        \\\\\\";
        System.out.print(art);
        System.out.print("\n-------------------------------------------------------\n");


        server.join();

        System.out.println("Shutting down plugins");
        for(StallionJavaPlugin plugin: PluginRegistry.instance().getJavaPluginByName().values()) {
            plugin.shutdownJetty(server);
        }

        System.out.println("Shutting down async coordinator");
        AsyncCoordinator.gracefulShutdown();



    }

    public class ReloadHandler implements SignalHandler {
        private Server server;

        public ReloadHandler(Server server) {
            this.server = server;
        }

        @Override
        public void handle(sun.misc.Signal signal) {
            if (server.isStopped()) {
                return;
            }
            try {
                MainRunner.setDoReload(true);
                AsyncCoordinator.gracefulShutdown();
                server.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }


    public class InterruptHandler implements SignalHandler {
        private Server server;

        public InterruptHandler(Server server) {
            this.server = server;
        }

        @Override
        public void handle(sun.misc.Signal signal) {
            System.out.println("Interrupted!");
            if (server.isStopped()) {
                System.exit(0);
            }
            try {
                AsyncCoordinator.gracefulShutdown();
                server.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
