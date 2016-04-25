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

package io.stallion.plugins;

import io.stallion.boot.StallionRunAction;
import io.stallion.dal.DalRegistry;
import io.stallion.dal.base.ModelBase;
import io.stallion.dal.base.DalRegistration;
import io.stallion.hooks.HookRegistry;
import io.stallion.hooks.ChainedHook;
import io.stallion.hooks.HookHandler;
import io.stallion.restfulEndpoints.RestEndpointBase;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.JavaRestEndpoint;
import io.stallion.restfulEndpoints.ResourceToEndpoints;
import io.stallion.services.Log;

import java.util.Collections;
import java.util.List;

public abstract class StallionJavaPlugin {
    private PluginRegistry pluginRegistry;

    public abstract String getPluginName();

    /**
     * Get a list of available actions
     *
     * @return
     */
    public List<? extends StallionRunAction> getActions() {
        return Collections.EMPTY_LIST;
    }

    /**
     *  Override this to implement configuring this plugin via the command-line.
     */
    public void commandlineConfigure() {
        System.out.println("No special configuration needed for this plugin.");
    }

    /**
     * The main method that gets called to load the plugin -- do everything here,
     * such as loading controllers, loading endpoints, etc.
     *
     * @throws Exception
     */
    public abstract void boot() throws Exception;

    /**
     * This gets called after plugin load, when Stallion is running as an actual
     * web server. Specifically, this gets called by AppContextLoader.startAllServices().
     * Override this if there are background services that do not need
     * to be run when executing most Stallion actions, but do need to be run
     * when running a script action.
     *
     */
    public void startServices() {


    }

    /**
     * Called by AppContextLoader.startServicesForTests(), which is called when loading
     * an app context via the BaseIntegrationTestCase JUnit test case base class.
     *
     */
    public void startServicesForTests() {

    }


    /**
     * Override this to shutdown services when the Stallion AppContext shutsdown
     */
    public void shutdown() {

    }

    public void bootForPackage(String packagePath) {

        // TODO: use the Reflect library to find all classes matching each giving interface, insantiate, and load them
    }


    @Deprecated
    public void registerModels(Class<? extends ModelBase> ...models) throws Exception {
        for(Class model: models) {
            Log.finer("Register model {0}", model.getName());
            DalRegistration registration = new DalRegistration()
                    .setPath(model.getName())
                    .setModelClass(model)
                    .setNameSpace("st-" + getPluginName())
                    .setUseDataFolder(true);
            DalRegistry.instance().registerDal(registration);
        }
    }

    @Deprecated
    public void registerEndpoints(JavaRestEndpoint...endpoints) {
        for(JavaRestEndpoint e: endpoints) {
            EndpointsRegistry.instance().addEndpoints(endpoints);
        }
    }

    @Deprecated
    public void registerResources(EndpointResource...resources) {
        ResourceToEndpoints converter = new ResourceToEndpoints("/_stx/" + getPluginName());
        for (EndpointResource resource: resources) {
            Log.finer("Register resource {0}", resource.getClass().getName());
            EndpointsRegistry.instance().addEndpoints(converter.convert(resource).toArray(new RestEndpointBase[]{}));
        }
    }

    @Deprecated
    public void registerHookHandlers(HookHandler...hooks) {
        for (HookHandler hookHandler: hooks) {
            HookRegistry.instance().register(hookHandler);
        }
    }

    @Deprecated
    public void registerChainHandlers(ChainedHook...chains) {
        for (ChainedHook chain: chains) {
            HookRegistry.instance().register(chain);
        }
    }


    public void setPluginRegistry(PluginRegistry pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
    }
}
