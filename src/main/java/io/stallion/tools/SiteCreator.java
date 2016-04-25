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


public class SiteCreator extends NewStallionSiteRunActionBase implements StallionRunAction {
    private String base;

    @Override
    public String getActionName() {
        return "new-site";
    }

    @Override
    public String getHelp() {
        return "generate the basic folders and files for a brand new Stallion site";
    }

    @Override
    public void loadApp(CommandOptionsBase options) {

    }

    public void execute() throws Exception {
        setBase(options.getTargetPath());
        makeFile("templates/page.jinja", getResource("page.jinja"));
        makeFile("assets/site.css", getResource("site.css"));
        makeFile("assets/site.bundle.css", CSS_BUNDLE);
        makeFile("assets/site.bundle.js", JS_BUNDLE);
        makeFile("pages/index.txt", getResource("index.txt"));
        makeFile("conf/stallion.toml", getResource("stallion.toml"));
        makeFile("conf/stallion.prod.toml", getResource("stallion.prod.toml"));
        makeDir("app-data");

    }



}
