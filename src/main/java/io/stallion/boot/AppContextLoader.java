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

import io.stallion.assets.AssetsController;
import io.stallion.asyncTasks.AsyncCoordinator;
import io.stallion.asyncTasks.SimpleAsyncRunner;

import io.stallion.contentPublishing.UploadedFileEndpoints;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.Tickets;
import io.stallion.dataAccess.file.ListingEndpoints;
import io.stallion.dataAccess.file.ListingExporter;
import io.stallion.dataAccess.file.TextItemController;
import io.stallion.dataAccess.filtering.FilterCache;
import io.stallion.exceptions.CommandException;
import io.stallion.exceptions.UsageException;
import io.stallion.fileSystem.FileSystemWatcherRunner;
import io.stallion.fileSystem.FileSystemWatcherService;
import io.stallion.forms.SimpleFormTag;
import io.stallion.hooks.HookRegistry;
import io.stallion.jobs.JobCoordinator;
import io.stallion.jobs.JobStatusController;
import io.stallion.monitoring.HealthTracker;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.plugins.PluginRegistry;
import io.stallion.reflection.PropertyUtils;
import io.stallion.requests.RoutesRegistry;
import io.stallion.services.*;
import io.stallion.sitemaps.SiteMapController;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.restfulEndpoints.SlugRegistry;
import io.stallion.settings.Settings;
import io.stallion.requests.RequestHandler;
import io.stallion.templating.JinjaTemplating;
import io.stallion.templating.TemplateRenderer;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.users.UsersApiResource;

import java.io.File;

import static io.stallion.utils.Literals.*;

/**
 * Helper methods for loading all the settings and services we need
 * to run Stallion
 */
public class AppContextLoader {

    private static AppContextLoader _app;

    public static boolean isNull() {
        return _app == null;
    }

    public static AppContextLoader instance() {
        if (_app == null) {
            throw new UsageException("App instance is null. You must call a creation method before you can access the app instance.");
        }
        return _app;
    }

    /**
     * Instaniates the _app singleton but only hydrates the settings.
     * Does not load any of the controllers or anything else.
     * @param options
     * @return
     */
    public static AppContextLoader loadWithSettingsOnly(CommandOptionsBase options) {
        if (_app == null) {
            _app = new AppContextLoader();
        }
        File confFile = new File(options.getTargetPath() + "/conf/stallion.toml");
        if (!confFile.exists()) {
             throw new CommandException("Cannot load site because the 'conf/stallion.toml' file is missing. You either targeted the wrong directory, or need to create a site first. Stallion expected to find the conf file at " + confFile.getAbsolutePath());
        }

        Settings.init(options.getEnv(), options);

        Settings.instance().setDevMode(options.isDevMode());

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
        return _app;
    }

    /**
     * Calls load completely as non-script, in non-test mode
     * @param options
     * @return
     */
    public static AppContextLoader loadCompletely(CommandOptionsBase options) {
        return loadCompletely(options, false);
    }

    /**
     * Calls load completely in script mode, in non-test mode
     * @param options
     * @return
     */
    public static AppContextLoader loadCompletelyForScript(CommandOptionsBase options) {
        return loadCompletely(options, true);
    }

    /**
     * Calls load completely in non-test mode
     * @param options
     * @return
     */
    public static AppContextLoader loadCompletely(CommandOptionsBase options, boolean isScript) {
        return loadCompletely(options, isScript, false);
    }

    /**
     * Loads the entire Stallion application.
     *
     * First, it loads all registries -- the SlugRegistry, the EndpointRegistry, and the FileSystemWatcherService
     * Second, it loads the database, caching layer, data access layer, assets, template engine
     * Third,  it loads builtin controllers and endpoints -- the UserController, the AdminEndpoints
     * Fourth, it loads the JobRunner and the AsyncRunners
     *
     * It does not start any the job or async services -- it only loads them.
     *
     * @param options
     * @param isScript
     * @param testMode
     * @return
     */
    public static AppContextLoader loadCompletely(CommandOptionsBase options, boolean isScript, boolean testMode) {
        if (_app == null || Settings.isNull()) {
            loadWithSettingsOnly(options);
        }

        // Load registries
        SiteMapController.load();
        FileSystemWatcherService.load();
        HookRegistry.load();
        SlugRegistry.load();
        EndpointsRegistry.load();


        // Data sources
        DB.load();
        FilterCache.load();
        DataAccessRegistry.load();
        AssetsController.load();
        HostSettings.load();

        //DefinedBundle.load();
        TemplateRenderer.load();
        TemplateRenderer.instance().getJinjaTemplating().registerTag(new SimpleFormTag());
        RoutesRegistry.load();

        // Load users, admin and other default functionality
        TransactionLogController.register();
        AuditTrailController.register();
        UserController.load();
        UsersApiResource.register();
        ListingEndpoints.register();
        ListingExporter.register();

        JobStatusController.selfRegister();



        UploadedFileEndpoints.registerIfEnabled();

        SimpleAsyncRunner.load();

        if (testMode) {
            AsyncCoordinator.initEphemeralSynchronousForTests();
        } else {
            if (options instanceof ServeCommandOptions && ((ServeCommandOptions) options).isNoTasks()) {

            } else {
                AsyncCoordinator.init();
            }
        }

        // Load plugins
        PluginRegistry.instance().bootJarPlugins();
        PluginRegistry.instance().loadAndRunJavascriptPlugins(true);

        return _app;
    }

    /**
     * Same as calling loadCompletely() and startServicesForTests(), but configures in a way suitable for tests
     * Some services, such as jobs, will not be started.
     * It will run asyncTasks in ephemeral synchronous mode, rather than the normal background thread mode
     *
     * @param targetFolder
     * @return
     */
    public static AppContextLoader loadAndStartForTests(String targetFolder) {
        CommandOptionsBase options = new CommandOptionsBase();
        options.setEnv(or(System.getProperty("stallionEnv"), "test"));
        options.setTargetPath(targetFolder);
        return loadAndStartForTests(options);
    }

    /**
     * Same as calling loadCompletely() and startServicesForTests(), but configures in a way suitable for tests
     * Some services, such as jobs, will not be started.
     * It will run asyncTasks in ephemeral synchronous mode, rather than the normal background thread mode
     *
     * @param options
     * @return
     */
    public static AppContextLoader loadAndStartForTests(CommandOptionsBase options) {
        PluginRegistry.loadWithJavaPlugins(options.getTargetPath());
        loadCompletely(options, false, true);
        _app.startServicesForTests();
        SimpleAsyncRunner.setSyncMode(true);
        return _app;
    }

    /**
     * Shutsdown all services, destroys all singletons, de-loads everything from memory.
     */
    public static void shutdown() {
        // This should be roughly the opposite order of loading

        AsyncCoordinator.gracefulShutdown();
        JobCoordinator.shutdown();
        FileSystemWatcherService .shutdown();

        PluginRegistry.shutdown();

        //DefinedBundle.shutdown();
        AssetsController.shutdown();
        TemplateRenderer.shutdown();

        RoutesRegistry.shutdown();

        DataAccessRegistry.shutdown();
        SiteMapController.shutdown();
        HostSettings.shutdown();

        DB.shutdown();

        HookRegistry.shutdown();
        EndpointsRegistry.shutdown();
        SimpleAsyncRunner.shutdown();
        FilterCache.shutdown();
        HealthTracker.shutdown();
        LocalMemoryCache.shutdown();

        PropertyUtils.resetCache();



        _app = null;
    }

    /**
     * Starts all services
     *
     * - starts the async task processing
     * - starts the job runner
     * - starts watching the file system for changes
     * - etc.
     *
     * @return
     */
    public AppContextLoader startAllServices() {
        if (AsyncCoordinator.instance() != null) {
            AsyncCoordinator.startup();
        }
        JobCoordinator.startUp();
        FileSystemWatcherService.start();
        HealthTracker.start();
        for (StallionJavaPlugin plugin: PluginRegistry.instance().getJavaPluginByName().values()) {
            plugin.startServices();
        }
        FilterCache.start();
        LocalMemoryCache.start();
        return _app;
    }

    /**
     * Starts only the services that are applicable for doing integration tests.
     * Some services, such as jobs, will not be started
     * AsyncTasks in ephemeral synchronous mode, rather than the normal background thread mode
     * @return
     */
    public AppContextLoader startServicesForTests() {
        for (StallionJavaPlugin plugin: PluginRegistry.instance().getJavaPluginByName().values()) {
            plugin.startServicesForTests();
        }
        FilterCache.start();
        LocalMemoryCache.start();

        return _app;
    }

    @Deprecated
    public Class loadClass(String className) {
        Class cls = null;
        try {
            cls = getClass().getClassLoader().loadClass(className);
            return cls;
        } catch (ClassNotFoundException e) {

        }
        for (StallionJavaPlugin booter: PluginRegistry.instance().getJavaPluginByName().values()) {
            try {
                cls = getClass().getClassLoader().loadClass(className);
                return cls;
            } catch (ClassNotFoundException e) {

            }
        }
        return null;
    }

    @Deprecated
    public Class loadClass(String pluginName, String className) {
        try {
            Class cls = PluginRegistry.instance().getJavaPluginByName().get(pluginName).getClass().getClassLoader().loadClass(className);
            return cls;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public String getEnv() {
        return Settings.instance().getEnv();
    }


    @Deprecated
    public String getTargetFolder() {
        return Settings.instance().getTargetFolder();
    }


    @Deprecated
    public TextItemController getPageController() {
        return DataAccessRegistry.instance().getPages();
    }


    @Deprecated
    public TextItemController getPostController() {
        return DataAccessRegistry.instance().getPosts();
    }

    @Deprecated
    public Settings getSettings() {
        return Settings.instance();
    }


    @Deprecated
    public DataAccessRegistry getDal() {
        return DataAccessRegistry.instance();
    }

    @Deprecated
    public DataAccessRegistry dal() {
        return DataAccessRegistry.instance();
    }

    @Deprecated
    public Integer getPort() {
        return Settings.instance().getPort();
    }


    @Deprecated
    public RequestHandler getHandler() {
        return RequestHandler.instance();
    }

    @Deprecated
    public FileSystemWatcherRunner getFileSystemWatcherRunner() {
        return FileSystemWatcherService.instance();
    }

    @Deprecated
    public TemplateRenderer getTemplateRenderer() {
        return TemplateRenderer.instance();
    }

    @Deprecated
    public AssetsController getAssetsController() {
        return AssetsController.instance();
    }

    @Deprecated
    public SlugRegistry getSlugRegistry() {
        return SlugRegistry.instance();
    }

    @Deprecated
    public UserController<User> getUserController() {
        return (UserController)dal().get("users");
    }

    @Deprecated
    public Tickets getTickets() {
        return DataAccessRegistry.instance().getTickets();
    }

    @Deprecated
    public PluginRegistry getPluginRegistry() {
        return PluginRegistry.instance();
    }

    @Deprecated
    public JinjaTemplating getJinjaTemplating() {
        return TemplateRenderer.instance().getJinjaTemplating();
    }


}
