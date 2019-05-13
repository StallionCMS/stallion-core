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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static io.stallion.utils.Literals.*;

import com.mashape.unirest.http.Unirest;
import io.stallion.assets.AssetsController;
import io.stallion.asyncTasks.AsyncCoordinator;
import io.stallion.asyncTasks.ForceTaskAction;
import io.stallion.asyncTasks.SimpleAsyncRunner;
import io.stallion.boot.*;
import io.stallion.contentPublishing.NewDraftPageAction;
import io.stallion.contentPublishing.SiteMapController;
import io.stallion.contentPublishing.SlugRegistry;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.db.*;
import io.stallion.dataAccess.file.ListingExporter;
import io.stallion.dataAccess.filtering.FilterCache;
import io.stallion.exceptions.CommandException;
import io.stallion.exceptions.UsageException;
import io.stallion.fileSystem.FileSystemWatcherService;
import io.stallion.http.StallionJerseyResourceConfig;
import io.stallion.http.ServeJettyRunAction;
import io.stallion.jobs.JobCoordinator;
import io.stallion.jobs.JobStatusController;
import io.stallion.monitoring.HealthTracker;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.reflection.PropertyUtils;
import io.stallion.secrets.SecretsAction;
import io.stallion.secrets.SecretsDecryptAction;
import io.stallion.services.*;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import io.stallion.testing.TestingRunAction;
import io.stallion.tools.ExportToHtml;
import io.stallion.tools.NewProjectBuilder;
import io.stallion.users.UserAdder;
import io.stallion.users.UserController;
import io.stallion.utils.Literals;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;


public abstract class StallionApplication extends StallionJavaPlugin {
    private static StallionApplication _instance = null;

    public static StallionApplication instance() {
        return _instance;
    }


    public void run(String[] args) throws Exception {
        run(args, new StallionJavaPlugin[]{});
    }

    public void run(String[] args, StallionJavaPlugin[] plugins) throws Exception {
        if (_instance != null) {
            throw new UsageException("Global StallionApplication instance was not null. Only one StallionApplication can exist at a time. You must call shutdown first, before calling run again.");
        }
        _instance = this;
        plugins = ArrayUtils.add(plugins, this);
        String targetPath = getTargetPath(args);


        // Load the plugin jars, to get additional actions
        PluginRegistry.loadWithJavaPlugins(targetPath, plugins);
        StallionRunAction action = loadTheRunAction(args);
        CommandOptionsBase options = loadCommandLineOptions(targetPath, action, args, plugins);
        registerShutdownHooks();
        loadSettings(options, action);
        loadLogger(options);


        // Initialize the ambient environment
        action.initializeRegistriesAndServices(this, Settings.instance().getModeFlags());

        try {

            for (StallionJavaPlugin plugin : PluginRegistry.instance().getJavaPluginByName().values()) {
                plugin.preExecuteAction(action.getActionName(), options);
            }
            action.execute(options);
        } finally {
            shutdownAll();
        }


    }


    public void loadForTests(String targetPath, StallionJavaPlugin ...plugins) {
        String env = or(System.getProperty("stallionEnv"), "test");
        loadForTests(targetPath, plugins, env);
    }
    public void loadForTests(String targetPath, StallionJavaPlugin[] plugins, String env) {
        if (_instance != null) {
            throw new UsageException("Global StallionApplication instance was not null. Only one StallionApplication can exist at a time. You must call shutdown first, before calling run again.");
        }
        _instance = this;
        plugins = ArrayUtils.add(plugins, this);
        PluginRegistry.loadWithJavaPlugins(targetPath, plugins);

        TestingRunAction action = new TestingRunAction();
        CommandOptionsBase options = new CommandOptionsBase();
        options.setTargetPath(targetPath);
        options.setEnv(env);

        loadSettings(options, action);
        loadLogger(options);
        action.initializeRegistriesAndServices(this, Settings.instance().getModeFlags());


    }

    public void loadForTestsLightweight(String targetPath, StallionJavaPlugin[] plugins, String env) {
        if (_instance != null) {
            throw new UsageException("Global StallionApplication instance was not null. Only one StallionApplication can exist at a time. You must call shutdown first, before calling run again.");
        }
        _instance = this;
        if (plugins == null) {
            plugins = new StallionJavaPlugin[0];
        }
        plugins = ArrayUtils.add(plugins, this);
        PluginRegistry.loadWithJavaPlugins(targetPath, plugins);

        TestingRunAction action = new TestingRunAction();
        CommandOptionsBase options = new CommandOptionsBase();
        options.setEnv(env);
        options.setTargetPath(targetPath);

        loadSettings(options, action);
        loadLogger(options);

    }


    protected StallionRunAction loadTheRunAction(String[] args) {
        List<StallionRunAction> actions = listAllRunActions();
        // Load the action executor
        StallionRunAction action = null;
        for(StallionRunAction anAction: actions) {
            if (empty(anAction.getActionName())) {
                throw new UsageException("The action class " + anAction.getClass().getName() + " has an empty action name");
            }
            if (args.length > 0 && anAction.getActionName().equals(args[0])) {
                action = anAction;
            }
        }
        if (action == null) {
            String msg = "\n\nError! You must pass in a valid action as the first command line argument. For example:\n\n" +
                    ">stallion serve -port=8090 -targetPath=~/my-stallion-site\n";
            if (args.length > 0) {
                msg += "\n\nYou passed in '" + args[0] + "', which is not a valid action.\n";
            }
            msg += "\nAllowed actions are:\n\n";
            for (StallionRunAction anAction: actions) {
                //Log.warn("Action: {0} {1} {2}", action, action.getActionName(), action.getHelp());
                msg += anAction.getActionName() + " - " + anAction.getHelp() + "\n";
            }
            msg += "\n\nYou can get help about an action by running: >stallion <action> help\n\n";
            System.err.println(msg);
            System.exit(1);
        }
        return action;
    }


    protected CommandOptionsBase loadCommandLineOptions(String targetPath, StallionRunAction action, String[] args, StallionJavaPlugin[] plugins) {

        // Load the command line options
        CommandOptionsBase options = action.newCommandOptions();

        CmdLineParser parser = new CmdLineParser(options);

        if (args.length > 1 && "help".equals(args[1])) {
            System.out.println(action.getActionName() + " - " + action.getHelp() + "\n\n");
            System.out.println("\n\nOptions for command " + action.getActionName() + "\n\n");
            parser.printUsage(System.out);
            System.exit(0);
        }

        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println("\n\nError!\n\n" + e.getMessage());
            System.err.println("\n\nAllowed options: \n");
            parser.printUsage(System.err);
            System.err.println("\n");
            System.exit(1);
        }

        options.setExtraPlugins(Arrays.asList(plugins));

        if (!empty(options.getLogLevel())) {
            try {
                Log.setLogLevel(Level.parse(options.getLogLevel()));
            } catch (IllegalArgumentException e) {
                System.err.println("\nError! Invalid log level: " + options.getLogLevel() + "\n\n" + CommandOptionsBase.ALLOWED_LEVELS);
                System.exit(1);
            }
        }
        options.setTargetPath(targetPath);
        return options;

    }


    protected void registerShutdownHooks() {


        // Shutdown hooks
        Thread shutDownHookThread = new Thread() {
            public void run() {
                try {
                    Unirest.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        shutDownHookThread.setName("stallion-shutdown-hook");
        Runtime.getRuntime().addShutdownHook(shutDownHookThread);

    }



    protected void loadSettings(CommandOptionsBase options, StallionRunAction action) {
        File confFile = new File(options.getTargetPath() + "/conf/stallion.toml");
        if (!confFile.exists()) {
            throw new CommandException("Cannot load site because the 'conf/stallion.toml' file is missing. You either targeted the wrong directory, or need to create a site first. Stallion expected to find the conf file at " + confFile.getAbsolutePath());
        }

        Settings.init(options.getEnv(), options, action);

        for(Map.Entry<String, String> entry: Settings.instance().getSystemProperties().entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }




    }

    protected void loadLogger(CommandOptionsBase options) {
        if (empty(options.getLogLevel())) {
            Log.setLogLevelFromSettings();
        }
        if (Settings.instance().getLogToFile()) {
            Log.enableFileLogger();
        }

        Log.setAlwaysIncludeLineNumber(options.isLoggingAlwaysIncludesLineNumber());

        if (!Settings.instance().getLogToConsole()) {
            Log.disableConsoleHandler();
        }
    }

    public void initializeRegistriesAndServices(boolean testMode) {
        // Load registries
        SiteMapController.load();
        FileSystemWatcherService.load();
        SlugRegistry.load();

        // Data sources
        DB.load();
        FilterCache.load();
        DataAccessRegistry.load();
        AssetsController.load();
        DynamicSettings.load();

        TemplateRenderer.load();

        // Load users, admin and other default functionality
        TransactionLogController.register();
        AuditTrailController.register();
        UserController.load();

        //ListingEndpoints.register();
        ListingExporter.register();

        JobStatusController.selfRegister();

        SimpleAsyncRunner.load(true);

        AsyncCoordinator.init(testMode);


        // Load plugins
        for(StallionJavaPlugin plugin: PluginRegistry.instance().listPluginsInAlphabeticOrder()) {
            plugin.onRegisterAll();
        }

    }

    public ResourceConfig buildJerseyResourceConfig() {
        StallionJerseyResourceConfig jerseyApplication = new StallionJerseyResourceConfig();
        onBuildJerseyResourceConfig(jerseyApplication);
        for(StallionJavaPlugin plugin: PluginRegistry.instance().listPluginsInAlphabeticOrder()) {
            plugin.onBuildResourceConfig(jerseyApplication);
        }
        return jerseyApplication;
    }

    protected void onBuildJerseyResourceConfig(ResourceConfig config) {

    }


    public void preloadData(ModeFlags mode) {
        DataAccessRegistry.instance().preloadStashData();
    }

    public void startJobAndTaskProcessingServices(boolean testMode) {
        if (!testMode) {
            if (AsyncCoordinator.instance() != null) {
                AsyncCoordinator.startup();
            }
            JobCoordinator.startUp();
            FileSystemWatcherService.start();
            HealthTracker.start();
            for(StallionJavaPlugin plugin: PluginRegistry.instance().listPluginsInAlphabeticOrder()) {
                plugin.onStartAll(testMode);
            }
            FilterCache.start();
            LocalMemoryCache.start();
        } else {
            for(StallionJavaPlugin plugin: PluginRegistry.instance().listPluginsInAlphabeticOrder()) {
                plugin.onStartAll(testMode);
            }
            FilterCache.start();
            LocalMemoryCache.start();
        }
    }


    /**
     * Shutsdown all services, destroys all singletons, de-loads everything from memory.
     */
    public void shutdownAll() {
        // This should be roughly the opposite order of loading

        AsyncCoordinator.gracefulShutdown();
        JobCoordinator.shutdown();
        FileSystemWatcherService .shutdown();

        for (StallionJavaPlugin plugin : PluginRegistry.instance().listPluginsInAlphabeticOrder()) {
            plugin.onShutdown();
        }
        PluginRegistry.shutdown();

        //DefinedBundle.shutdown();
        AssetsController.shutdown();
        TemplateRenderer.shutdown();



        DataAccessRegistry.shutdown();
        SiteMapController.shutdown();
        DynamicSettings.shutdown();

        DB.shutdown();

        SimpleAsyncRunner.shutdown();
        FilterCache.shutdown();
        HealthTracker.shutdown();
        LocalMemoryCache.shutdown();

        PropertyUtils.resetCache();



        Settings.shutdown();

        _instance = null;

    }









    public List<StallionRunAction> listAllRunActions() {
        List<StallionRunAction> actions = list();
        actions.addAll(listDefaultRunActions());
        actions.addAll(PluginRegistry.instance().getAllPluginDefinedStallionRunActions());

        return actions;
    }

    public List<StallionRunAction> listDefaultRunActions() {
        return Literals.list(
                new ServeJettyRunAction(),
                new UserAdder(),
                new NewProjectBuilder(),
                new NewDraftPageAction(),
                //new NewJavaPluginRunAction(),
                new ExportToHtml(),
                new SecretsAction(),
                new SqlMigrationAction(),
                new SqlCheckNeedsMigrationAction(),
                new SqlGenerationAction(),
                new SqlObsoleteAction(),
                new ForceTaskAction(),
                new SecretsDecryptAction()

        );
    }


    public String getTargetPath(String[] args) {
        String targetPath = new File(".").getAbsolutePath();
        for(String arg: args) {
            if (arg.startsWith("-targetPath=")) {
                targetPath = StringUtils.split(arg, "=", 2)[1];
            }
        }
        if (!new File(targetPath).isDirectory() || empty(targetPath) || targetPath.equals("/") || targetPath.equals("/.")) {
            throw new CommandException("You ran stallion with the target path " + targetPath + " but that is not a valid folder.");
        }
        return targetPath;

    }


    public static class DefaultApplication extends StallionApplication {
        public static final String DEFAULT_APP_NAME = "DefaultStallionApplication";

        @Override
        public String getName() {
            return DEFAULT_APP_NAME;
        }

        @Override
        public void onRegisterAll() {

        }


    }

}
