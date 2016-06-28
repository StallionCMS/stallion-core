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

package io.stallion.secrets;

import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.StallionRunAction;


public class SecretsAction implements StallionRunAction<CommandOptionsBase> {

    @Override
    public String getActionName() {
        return "secrets";
    }

    @Override
    public String getHelp() {
        return "Add, edit, or update your enrypted secrets file.";
    }

    @Override
    public void loadApp(CommandOptionsBase options) {

    }

    @Override
    public void execute(CommandOptionsBase options) throws Exception {
        SecretsCommandLineManager manager = new SecretsCommandLineManager();
        manager.start(options);
    }
}