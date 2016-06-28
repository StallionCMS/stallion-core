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

package io.stallion.tools;

import io.stallion.boot.CommandOptionsBase;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StopOptionHandler;

import java.util.ArrayList;
import java.util.List;


public class ScriptOptions extends CommandOptionsBase {

    @Option(name="-env", usage="The environment you are running in. The file settings.(env).toml will be merged into your settings.")        // no usage
    private String env = "local";


    @Option(name="--", handler= StopOptionHandler.class)
    private List<String> stopOptions = new ArrayList<>();

    public List<String> getStopOptions() {
        return stopOptions;
    }

    public void setStopOptions(List<String> stopOptions) {
        this.stopOptions = stopOptions;
    }

    @Override
    public String getEnv() {
        return env;
    }

    @Override
    public void setEnv(String env) {
        this.env = env;
    }


}
