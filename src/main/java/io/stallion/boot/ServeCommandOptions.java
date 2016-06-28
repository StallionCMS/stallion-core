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

import io.stallion.settings.Settings;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;


public class ServeCommandOptions extends CommandOptionsBase {

    @Option(name="-port", usage="The port number to boot the server on")
    private int port = 8090;

    @Option(name="-localMode", usage="Set to 'false' if you want to simulate a server environment, with bundled assets, logging to file instead of console, etc.")
    private String localMode = null;


    @Override
    public Settings hydrateSettings(Settings settings) {
        super.hydrateSettings(settings);
        settings.setEnv(getEnv());
        settings.setTargetFolder(getTargetPath());
        settings.setPort(port);
        settings.setDevMode(isDevMode());
        if (getLocalMode() != null) {
            settings.setLocalMode(getLocalMode());
        }
        return settings;
    }

    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }



    public Boolean getLocalMode() {
        if (localMode == null) {
            return null;
        }
        return localMode.toLowerCase().equals("true");
    }

    public void setLocalMode(String localMode) {
        this.localMode = localMode;
    }


}
