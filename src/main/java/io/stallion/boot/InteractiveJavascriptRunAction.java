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

import io.stallion.plugins.javascript.JavascriptShell;
import jdk.nashorn.tools.Shell;
import jline.console.ConsoleReader;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.BufferedOutputStream;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class InteractiveJavascriptRunAction implements StallionRunAction<ServeCommandOptions> {
    @Override
    public String getActionName() {
        return "javascript-shell";
    }

    @Override
    public String getHelp() {
        return "Runs an interactive javascript prompt";
    }

    @Override
    public ServeCommandOptions newCommandOptions() {
        return new ServeCommandOptions();
    }

    @Override
    public void loadApp(ServeCommandOptions options) {
        AppContextLoader.loadCompletely(options);
    }

    @Override
    public void execute(ServeCommandOptions options) throws Exception {
        JavascriptShell.main(new String[]{});
    }
}
