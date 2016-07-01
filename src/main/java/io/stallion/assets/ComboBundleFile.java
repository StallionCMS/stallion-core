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

package io.stallion.assets;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

import java.util.List;
import java.util.Map;

import io.stallion.services.Log;
import io.stallion.settings.Settings;

/**
 * A combo bundle file contains both CSS and Javascript. This is needed to process
 * vue components.
 */
public abstract class ComboBundleFile extends BundleFile {
    private String rawContent = null;
    private String css = null;
    private String javascript = null;

    public String getCss() {
        if (css == null || Settings.instance().getDevMode()) {
            process();
        }
        return css;
    }

    public String getJavascript() {
        if (javascript == null || Settings.instance().getDevMode()) {
            process();
        }
        return javascript;
    }

    protected <T extends ComboBundleFile> T setJavascript(String js) {
        this.javascript = js;
        return (T)this;
    }

    protected <T extends ComboBundleFile> T setCss(String css) {
        this.css = css;
        return (T)this;
    }


    public String getRawContent() {
        return rawContent;
    }

    protected <T extends ComboBundleFile> T  setRawContent(String rawContent) {
        this.rawContent = rawContent;
        return (T)this;
    }

    protected abstract void process();



}
