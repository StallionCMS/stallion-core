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

package io.stallion.boot;

import io.stallion.Context;
import io.stallion.asyncTasks.AsyncCoordinator;
import io.stallion.plugins.PluginRegistry;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.requests.RequestHandler;
import io.stallion.restfulEndpoints.RestEndpointBase;
import io.stallion.restfulEndpoints.SlugRegistry;
import io.stallion.services.Log;
import io.stallion.settings.Settings;

import org.eclipse.jetty.server.Server;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import javax.servlet.MultipartConfigElement;
import java.text.MessageFormat;
import java.util.Map;


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

    @Override
    public void execute(ServeCommandOptions options) throws Exception {




        // Start the server
        Server server = new Server(Settings.instance().getPort());
        server.setHandler(RequestHandler.instance());


        server.start();

        System.out.print("-------------------------------------------------------\n");
        System.out.print(MessageFormat.format(
                "Final registration count: {1} controllers. {2} plugins. {3} endpoints. {4} slugs. V=3\n",
                Settings.instance().getPort(),
                Context.dal().size(),
                PluginRegistry.instance().getJavaPluginByName().size(),
                EndpointsRegistry.instance().getEndpoints().size(),
                SlugRegistry.instance().getSlugMap().size()
        ));


        // If we are in debug mode, file system changes will send a signal, which
        // will then be caught, triggering a reload.
        if (MainRunner.isIsDebugRunner()) {
            sun.misc.Signal.handle(new Signal("INT"), new InterruptHandler(server));
            sun.misc.Signal.handle(new Signal("USR2"), new ReloadHandler(server));
            MainRunner.setupWatchers(Settings.instance().getTargetFolder());
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
