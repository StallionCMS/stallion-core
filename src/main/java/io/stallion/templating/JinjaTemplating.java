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

package io.stallion.templating;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.loader.ResourceNotFoundException;
import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.stallion.utils.Literals.list;

public class JinjaTemplating implements Templating {
    private static String targetFolder = "";
    private static Jinjava jinjava;
    private boolean devMode = false;

    public JinjaTemplating(String folder, boolean devMode) {
        this.devMode = devMode;
        init(folder);
    }

    @Override
    public void init(String folder) {
        this.targetFolder = folder;
        reset();
    }

    private static List<Tag> tags;
    private static List<Filter> filters;

    @Override
    public void reset() {

        //JinjavaConfig config = new JinjavaConfig();
        //config.setResourceLocator(new MyCustomResourceLocator());

        if (tags == null) {
            tags = list();
        }
        if (filters == null) {
            filters = list(new MarkdownFilter());
        }


        //JinjavaConfig config = JinjavaConfig.newBuilder().withMaxRenderDepth(0).build();
        //jinjava = new Jinjava(config);
        jinjava = new Jinjava();

        jinjava.setResourceLocator(new JinjaResourceLocator(targetFolder, devMode));
        for (Filter filter: filters) {
            jinjava.getGlobalContext().registerFilter(filter);
        }
        for (Tag tag: tags) {
            jinjava.getGlobalContext().registerTag(tag);
        }

        JinjaResourceLocator.clearCache();
    }

    @Override
    public Boolean templateExists(String path) {
        if (path.contains("\n")) {
            return true;
        }

        String result = null;
        try {
            result = jinjava.getResourceLocator().getString(path, Charset.forName("UTF-8"), null);
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (StringUtils.isEmpty(result)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String renderTemplate(String template, Map<String, Object> context) {
        String content = "";
        try {
            String templateString = "";
            if (template.contains("\n")) {
                templateString = template;
            } else {
                templateString = jinjava.getResourceLocator().getString(template, Charset.forName("UTF-8"), null);
            }
            if (templateString == null && Settings.instance().isStrict()) {
                throw new UsageException("Could not find the template: " + template);
            } else if (templateString == null) {
                templateString = "";
            }
            RenderResult result = jinjava.renderForResult(templateString, context);
            List fatalErrors = (List)result.getErrors().stream().filter((error) -> {
                return error.getSeverity() == TemplateError.ErrorType.FATAL;
            }).collect(Collectors.toList());
            if(!fatalErrors.isEmpty()) {
                Log.warn("Exception when rendering template: {0}", template);
                throw new FatalTemplateErrorsException(template, fatalErrors);
            } else {
                return result.getOutput();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class FatalTemplateErrorsException extends InterpretException {
        private final String templatePath;
        private final Iterable<TemplateError> errors;

        public FatalTemplateErrorsException(String templatePath, Iterable<TemplateError> errors) {
            super("Fatal error rendering template \"" + templatePath + "\".\n" + generateMessage(errors));
            this.templatePath = templatePath;
            this.errors = errors;
        }

        private static String generateMessage(Iterable<TemplateError> errors) {
            StringBuilder msg = new StringBuilder();
            Iterator var2 = errors.iterator();

            while(var2.hasNext()) {
                TemplateError error = (TemplateError)var2.next();
                msg.append(error.toString()).append('\n');
                if(error.getException() != null) {
                    msg.append(ExceptionUtils.getStackTrace(error.getException())).append('\n');
                }
            }

            return msg.toString();
        }

        public String getTemplatePath() {
            return this.templatePath;
        }

        public Iterable<TemplateError> getErrors() {
            return this.errors;
        }
    }



    public void registerTag(Tag tag) {
        tags.add(tag);
        jinjava.getGlobalContext().registerTag(tag);

    }

    public void registerFilter(Filter filter) {
        filters.add(filter);
        jinjava.getGlobalContext().registerFilter(filter);

    }

}
