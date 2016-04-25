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

import io.stallion.Context;
import io.stallion.boot.CommandOptionsBase;
import io.stallion.exceptions.UsageException;
import io.stallion.utils.SimpleTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.stallion.utils.Literals.*;


public abstract class NewStallionSiteRunActionBase<T extends NewStallionSiteRunActionBase<T>> {
    private String base;
    private Map<String, Object> context = new HashMap<>();
    private boolean overwrite = false;
    protected CommandOptionsBase options;

    public void execute(CommandOptionsBase options) throws Exception {
        if (empty(base) && Context.app() != null) {
            base = Context.settings().getTargetFolder();
        }
        put("prefix", "");
        put("pageSlug", "/");
        this.options = options;
        execute();
    }
    public abstract void execute() throws Exception;

    public T setBase(String path) {
        base = path;
        return (T)this;
    }

    protected void makeDir(String relativePath) {
        File dir = new File(base + "/" + relativePath);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
    }

    protected String getResource(String url) throws IOException {
        URL u = getClass().getResource("/default-site/" + url);
        if (u == null) {
            throw new UsageException("Resourec file does not exist: " + "/default-site/" + url);
        }
        return IOUtils.toString(u);

    }

    protected void makeFile(String relativePath, URL url) throws IOException {
        makeFile(relativePath, IOUtils.toString(url));
    }

    protected void makeFile(String relativePath, String content) throws IOException {
        content = new SimpleTemplate(content, context, SimpleTemplate.MatchPattern.DOLLAR).render();
        File file = new File(base + "/" + relativePath);
        File parent = new File(file.getParent());
        if (!parent.isDirectory()) {
            parent.mkdirs();
        }
        if (!file.exists() || overwrite) {
            FileUtils.write(file, content);
        }

    }

    public T put(String key, Object o) {
        this.context.put(key, o);
        return (T)this;
    }

    protected String getBase() {
        return base;
    }


    protected final String CSS_BUNDLE = "//=resource:stallion|stallion.css|stallion.css|http://local.stallion.io/core/assets/stallion.css\n" +
            "//=resource:stallion|pure-min.css\n" +
            "//=resource:stallion|grids-responsive-min.css|grids-responsive-min.css\n" +
            "//=resource:comments|public.css|public.css|http://local.stallion.io/comments/assets/public.css\n" +
            "//=$$prefix$$site.css";

    protected final String JS_BUNDLE = "//=key:jquery|https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js|https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.js|http://local-assets.pfitz.net/assets/jquery-1.11.2.js\n" +
            "//=resource:stallion|stallion.js|stallion.js|http://local.stallion.io/assets/stallion.js\n" +
            "//=$$prefix$$site.js";


}
