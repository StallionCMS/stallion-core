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
import io.stallion.asyncTasks.AsyncTaskController;
import io.stallion.jobs.JobCoordinator;
import io.stallion.plugins.PluginRegistry;
import io.stallion.requests.RequestHandler;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.restfulEndpoints.SlugRegistry;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.eclipse.jetty.server.Server;
import sun.misc.Signal;

import java.text.MessageFormat;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class ForceTaskAction implements StallionRunAction<ForceActionOptions> {

    @Override
    public String getActionName() {
        return "force-action";
    }

    @Override
    public String getHelp() {
        return "Force run a particular task or a particular job, regardless of whether it is scheduled to run or not.";
    }

    @Override
    public void loadApp(ForceActionOptions options) {
        AppContextLoader.loadCompletely(options);
        AppContextLoader.instance().startAllServices();
    }

    @Override
    public ForceActionOptions newCommandOptions() {
        return new ForceActionOptions();
    }

    @Override
    public void execute(ForceActionOptions options) throws Exception {

        if (options.getTaskId() > 0L) {
            Log.info("Run AsyncTask with id {0}", options.getTaskId());
            AsyncCoordinator.instance().runTaskForId(options.getTaskId(), options.getForce());
            return;
        }

        if (!empty(options.getJobName())) {
            Log.info("Force run job with name {0}", options.getJobName());
            JobCoordinator.instance().forceRunJob(options.getJobName(), options.getForce());
        }

        AsyncCoordinator.gracefulShutdown();
        JobCoordinator.shutdown();
    }
}
