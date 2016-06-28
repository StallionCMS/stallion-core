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

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.StallionRunAction;

/**
 * Command-line action for running javascript unittests.
 */
public class PluginTestsRunAction implements StallionRunAction<PluginTestsOptions> {

    @Override
    public PluginTestsOptions newCommandOptions() {
        return new PluginTestsOptions();
    }


    @Override
    public String getActionName() {
        return "test";
    }

    @Override
    public String getHelp() {
        return "Runs the tests for a given plugin";
    }

    @Override
    public void loadApp(PluginTestsOptions options) {
        AppContextLoader.loadAndStartForTests(options);

    }

    @Override
    public void execute(PluginTestsOptions options) throws Exception {
        PluginRegistry.instance().runJsTests(options.getPlugin(), options.getMatching());
    }
}
