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

package io.stallion.dataAccess;

import io.stallion.restfulEndpoints.SlugRegistry;
import io.stallion.services.Log;
import io.stallion.templating.TemplateRenderer;

import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class DisplayableModelController<T extends Displayable> extends StandardModelController<T> {
    private String defaultTemplate = "";

    @Override
    protected void preInitialize(DataAccessRegistration registration) {
        setDefaultTemplate(registration.getTemplatePath());
    }

    public String getDefaultTemplate() {
        return defaultTemplate;
    }
    public DisplayableModelController<T> setDefaultTemplate(String template) {
        this.defaultTemplate = template;
        return this;
    }

    public String getTemplate(T model) {
        if (!empty(model.getTemplate())) {
            return model.getTemplate();
        }
        return getDefaultTemplate();
    }

    public String render(T item, Map<String, Object> context) {
        String template = or(getTemplate(item), settings().getPageTemplate());
        return TemplateRenderer.instance().renderTemplate(template, context);
    }

    @Override
    public void onPostLoadItem(T obj) {
        StandardDisplayableModel item = (StandardDisplayableModel)obj;

        Log.fine("Add to slug registry slug={0} id={1} bucket=bucket:{2}", item.getSlug(), item.getId(), getBucket());
        SlugRegistry.instance().addDisplayable(item);
        if (!empty(item.getOldUrls())) {
            for (String url: item.getOldUrls()) {
                SlugRegistry.instance().registerRedirect(url, item.getSlug());
            }
        }
    }



}
