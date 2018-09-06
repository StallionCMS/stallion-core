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

package io.stallion;

import io.stallion.exceptions.CommandException;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.Log;

import java.util.logging.Level;

/**
 * Contains main method that gets run when stallion is executed. The entrypoint into the Stallion service.
 */
public class Main

{
    /**
     * Run Stallion
     * @param args  - passed in via the command line
     * @throws Exception
     */
    public static void main( String[] args ) throws Exception {
        System.setProperty("java.awt.headless", "true");
        mainWithPlugins(new StallionApplication.DefaultApplication(), args);
    }

    /**
     * Run main, and manually register plugins, rather than have plugins loaded via the /jars folder.
     *
     * This method is used in plugin development, when we do not have a pre-built jar of the plugin available.
     *
     * @param args
     * @param plugins
     * @throws Exception
     */
    public static void mainWithPlugins(StallionApplication app, String[] args, StallionJavaPlugin...plugins) throws Exception {
        if (app == null) {
            app = new StallionApplication.DefaultApplication();
        }
        if (plugins == null) {
            plugins = new StallionJavaPlugin[0];
        }
        try {
            doMain(app, args, plugins);
        } catch (CommandException ex) {
            System.err.println("\n\nError! \n\n" + ex.getMessage() + "\n\n");
            if (Log.getLogLevel().intValue() <= Level.FINE.intValue()) {
                Log.exception(ex, "Command exception");
            }
        }
    }

    /**
     * Actually boot and run Stallion
     *
     * @param args
     * @param plugins
     * @throws Exception
     */
    public static void doMain(StallionApplication app, String[] args, StallionJavaPlugin[] plugins) throws Exception {
        app.run(args, plugins);
    }







}
