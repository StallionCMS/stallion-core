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

package io.stallion.plugins;

import io.stallion.StallionApplication;
import io.stallion.boot.CommandOptionsBase;
import io.stallion.http.ServeCommandOptions;
import io.stallion.boot.StallionRunAction;
import io.stallion.services.Log;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static io.stallion.utils.Literals.*;

public abstract class StallionJavaPlugin {

    public abstract String getName();

    public List<String> getSqlMigrations() {
        if (getClass().equals(StallionApplication.DefaultApplication.class)) {
            return list();
        }
        URL resource = getClass().getResource("/sql/migrations.txt");
        if (resource != null) {
            try {
                List<String> migrations = list();
                Log.fine("Found /sql/migrations file for plugin {0}", getClass().getCanonicalName());
                for(String migration: IOUtils.toString(resource, UTF8).split("\\n")) {
                    migration = StringUtils.strip(migration);
                    if (migration.startsWith("#") || migration.startsWith("//")) {
                        continue;
                    }
                    if (!empty(migration)) {
                          migrations.add(migration);
                    }
                }
                return migrations;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return list();
    }


    /**
     * The main method that gets called to load the plugin -- do everything here,
     * such as loading controllers, loading endpoints, etc.
     *
     * @throws Exception
     */
    public abstract void onRegisterAll();

    /**
     * This gets called after plugin load, when Stallion is running as an actual
     * web server or job server. Specifically, this gets called by
     * StallionApplication.startJobAndTaskProcessingServices().
     *
     * Override this if there are background services that do not need
     * to be run when executing most Stallion actions, but do need to be run
     * when running a script action.
     *
     */
    public void onStartAll(boolean testMode) {


    }

    public void preExecuteAction(String action, CommandOptionsBase options) {

    }

    /**
     * Get a list of available actions
     *
     * @return
     */
    public List<? extends StallionRunAction> getExtraActions() {
        return Collections.EMPTY_LIST;
    }



    public void onBuildResourceConfig(ResourceConfig rc) {

    }

    public void preStartJetty(Server server, HandlerCollection handlerCollection, ServeCommandOptions options) {

    }

    public void postStartJetty(Server server, ServeCommandOptions options) {

    }


    /**
     * Override this to shutdown services when the Stallion AppContext shutsdown
     */
    public void onShutdownJetty(Server server) {

    }

    /**
     * Override this to shutdown services when the Stallion AppContext shutsdown
     */
    public void onShutdown() {

    }




}
