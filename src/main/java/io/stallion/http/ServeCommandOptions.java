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

package io.stallion.http;

import io.stallion.boot.CommandOptionsBase;
import io.stallion.settings.Settings;
import org.kohsuke.args4j.Option;


public class ServeCommandOptions extends CommandOptionsBase {

    @Option(name="-port", usage="The port number to boot the server on")
    private int port = 8090;


    @Option(name="-noTasks", usage = "Do not execute asynchronous tasks.")
    private boolean noTasks = false;


    @Override
    public Settings hydrateSettings(Settings settings) {
        super.hydrateSettings(settings);
        settings.setEnv(getEnv());
        settings.setTargetFolder(getTargetPath());
        settings.setPort(port);

        return settings;
    }

    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }




    public boolean isNoTasks() {
        return noTasks;
    }

    public ServeCommandOptions setNoTasks(boolean noTasks) {
        this.noTasks = noTasks;
        return this;
    }
}
