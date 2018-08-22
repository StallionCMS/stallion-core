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

import io.stallion.asyncTasks.AsyncCoordinator;
import io.stallion.jobs.JobCoordinator;


public class WorkerCommandAction  implements StallionRunAction<ServeCommandOptions> {

    @Override
    public String getActionName() {
        return "worker";
    }

    @Override
    public String getHelp() {
        return "runs specified asynchronous jobs and/or recurring jobs";
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





        System.out.print("-------------------------------------------------------\n");
        String art = "" +
                "         _,_\n" +
                "        ;'._\\\n" +
                "       ';) \\._,     Stallion worker now running." +
                "        /  /`-'\n" +
                "     ~~( )/\n" +
                "        )))\n" +
                "        \\\\\\";
        System.out.print(art);
        System.out.print("\n-------------------------------------------------------\n");


        JobCoordinator.currentThread().join();

        System.out.println("Shutting down async coordinator");
        AsyncCoordinator.gracefulShutdown();
    }
}
