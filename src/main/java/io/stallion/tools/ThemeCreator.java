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

package io.stallion.tools;

import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.StallionRunAction;
import io.stallion.exceptions.UsageException;
import io.stallion.utils.GeneralUtils;

import java.io.Console;
import java.io.File;

import static io.stallion.utils.Literals.*;


public class ThemeCreator extends NewStallionSiteRunActionBase implements StallionRunAction {


    @Override
    public String getActionName() {
        return "new-theme";
    }

    @Override
    public String getHelp() {
        return "generates the scaffold files for a new theme (templates, css, bundles)";
    }

    @Override
    public void loadApp(CommandOptionsBase options) {

    }

    private String themeName;

    public void execute() throws Exception {
        setBase(options.getTargetPath());
        if (empty(themeName)) {
            Console console = System.console();
            themeName = GeneralUtils.slugify(console.readLine("Enter your theme name: "));
            if (empty(themeName)) {
                throw new UsageException("Missing or invalid theme name!");
            }
        }
        if (!new File(getBase() + "/conf/stallion.toml").exists()) {
            throw new UsageException("Target is not a valid stallion site. No conf/stallion.toml found in folder " + getBase());
        }
        put("prefix", themeName + "/");
        put("pageSlug", "/" + themeName);

        makeFile("templates/" + themeName + "/page.jinja", getResource("page.jinja"));
        makeFile("assets/" + themeName + "/site.css", getResource("site.css"));
        makeFile("assets/" + themeName + "/site.js", getResource("site.js"));
        makeFile("assets/" + themeName + "/site.bundle.js", JS_BUNDLE);
        makeFile("assets/" + themeName + "/site.bundle.css", CSS_BUNDLE);
        makeFile("pages/" + themeName + ".txt", getResource("index.txt"));
        System.out.printf("Theme created. View your theme in your browser by running your site and going to the path: /" + themeName);
    }

    public ThemeCreator setThemeName(String themeName) {
        themeName = GeneralUtils.slugify(themeName);
        this.themeName = themeName;
        return this;
    }


}
