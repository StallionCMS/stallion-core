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

import io.stallion.exceptions.CommandException;
import io.stallion.fileSystem.BaseWatchEventHandler;
import io.stallion.fileSystem.FileSystemWatcherRunner;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.Console;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Contains main method that gets run when stallion is executed. The entrypoint into the Stallion service.
 */
public class MainRunner

{
    /**
     * Run Stallion
     * @param args  - passed in via the command line
     * @throws Exception
     */
    public static void main( String[] args ) throws Exception {
        System.setProperty("java.awt.headless", "true");
        mainWithPlugins(args);
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
    public static void mainWithPlugins(String[] args, StallionJavaPlugin...plugins) throws Exception {
        try {
            if (ArrayUtils.contains(args, "-autoReload")) {
                runWithAutoReload(args, plugins);
            } else {
                doMain(args, plugins);
            }
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
    public static void doMain(String[] args, StallionJavaPlugin[] plugins) throws Exception {
        new Booter().boot(args, plugins);
    }




    private static boolean isDebugRunner = false;
    private static boolean doReload = false;
    private static FileSystemWatcherRunner watcher = null;
    private static Thread watcherThread = null;
    private static StallionServer server = null;

    /**
     * Runs the main Stallion service in auto-reload mode, which means that if a configuration file,
     * or a server-side javascript file, or a plugin file is touched, the entire application context
     * will reload and all server-side javascript will be re-processed. Use this when developing
     * to avoid having to manually restart every time you change a file.
     *
     *
     * @param args
     * @param plugins
     * @throws Exception
     */
    public static void runWithAutoReload( String[] args, StallionJavaPlugin[] plugins ) throws Exception {
        isDebugRunner = true;

        Console console = System.console();
        while (true) {
            Log.info("(re)start in debug reloading mode.");
            try {
                reboot(args, plugins);
                if (doReload) {
                    doReload = false;
                    continue;
                }
                System.out.println("Interrupted. Type q to quit, any other key to reboot.");
                String input = console.readLine();
                if (input.startsWith("q")) {
                    break;
                }
            } catch (Exception e) {
                ExceptionUtils.printRootCauseStackTrace(e);
                System.out.println("Other exception. Type q to quit, any other key to reboot.");
                String input = console.readLine();
                if (input.startsWith("q")) {
                    break;
                }
            }
        }
        Log.info("Shutting down javascript and conf file watcher.");
        if (watcher != null) {
            watcher.setShouldRun(false);
        }
        watcherThread.interrupt();
        watcherThread.join();
        System.out.println("Exiting.");
        System.exit(0);
    }

    static void setupWatchersIfDebugMode(String targetPath) {
        if (isIsDebugRunner()) {
            setupWatchers(targetPath);
        } else {
            return;
        }
    }

    static void setupWatchers(String targetPath) {
        List<File> directories = new ArrayList<>();
        directories.add(new File(targetPath + "/js"));
        directories.add(new File(targetPath + "/conf"));
        if (new File(targetPath + "/plugins").isDirectory()) {
            for(File file: new File(targetPath + "/plugins").listFiles()) {
                if (file.isDirectory()) {
                    directories.add(file);
                }
            }
        }
        watcher = new FileSystemWatcherRunner();


        for (File dir: directories) {
            Log.info("Watch folder {0}", dir.toString());
            if (!dir.isDirectory()) {
                continue;
            }
            BaseWatchEventHandler handler = new DebugFileChangeHandler()
                    .setWatchTree(true)
                    .setWatchedFolder(dir.getAbsolutePath());
            if (dir.getAbsolutePath().endsWith("/conf")) {
                handler.setExtension(".toml");
            } else {
                handler.setExtension(".js");
            }
            watcher.registerWatcher(handler);
        }

        watcherThread = new Thread(watcher);
        watcherThread.setName("stallion-dev-mode-source-code-watcher");
        watcherThread.start();

    }


    /**
     * Reboot the application context.
     *
     * @param args
     * @param plugins
     * @throws Exception
     */
    public static void reboot(String[] args, StallionJavaPlugin[] plugins) throws Exception {
        AppContextLoader.shutdown();
        doMain(args, plugins);
    }

    static boolean isDoReload() {
        return doReload;
    }
    static void setDoReload(boolean doReload) {
        MainRunner.doReload = doReload;
    }

    static boolean isIsDebugRunner() {
        return isDebugRunner;
    }

    static void setIsDebugRunner(boolean isDebugRunner) {
        MainRunner.isDebugRunner = isDebugRunner;
    }

}
