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

package io.stallion.utils;

import io.stallion.exceptions.UsageException;
import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.safeLoop;


/**
 * SimpleTemplating excepts a String with context tokens in the form of {object.attribute.attribute}
 * You can populate the template with context objects.
 * Then you render the template, and all tokens get replaced with the attribute values.
 *
 * For example:
 *
 * String result = new SimpleTemplate("My name is {user.displayName}")
 *     .put("user", new User().setDisplayName("Peter Pan")
 *     .render();
 *
 * Result will equal: "My name is Peter Pan"
 *
 *
 */
public class SimpleTemplate {
    private static Pattern curlyPattern = Pattern.compile("\\{\\s*([\\w\\.]+)\\s*\\}");
    private static Pattern doubleCurlyPattern = Pattern.compile("\\{\\{\\s*([\\w\\.]+)\\s*\\}\\}");
    private static Pattern dollarPattern = Pattern.compile("\\$\\$\\s*([\\w\\.]+)\\s*\\$\\$");
    private Map<String, Object> context;
    private String template;
    private Boolean strict = false;
    private MatchPattern pattern = MatchPattern.DOLLAR.SINGLE_CURLY;

    public static enum MatchPattern {
        DOUBLE_CURLY,
        SINGLE_CURLY,
        DOLLAR
    }

    public SimpleTemplate(String template) {
        this.template = template;
        this.context = new HashMap<>();
    }


    public SimpleTemplate(String template, Map<String, Object> context) {
        this(template, context, null);
    }

    public SimpleTemplate(String template, Map<String, Object> context, MatchPattern pattern) {
        if (context == null) {
            context = new HashMap<>();
        }
        if (pattern != null) {
            this.pattern = pattern;
        }
        this.context = context;
        this.template = template;
    }

    public SimpleTemplate putAll(Map<String, Object> context) {
        this.context.putAll(context);
        return this;
    }

    public SimpleTemplate put(String key, Object val) {
        this.context.put(key, val);
        return this;
    }

    public String render() {
        Matcher matcher;
        if (pattern.equals(MatchPattern.DOLLAR)) {
            matcher = dollarPattern.matcher(template);
        }  else if (pattern.equals(MatchPattern.DOUBLE_CURLY)) {
            matcher = doubleCurlyPattern.matcher(template);
        } else {
            matcher = curlyPattern.matcher(template);
        }
        StringBuffer result = new StringBuffer();
        for(Object c: safeLoop(1000)) {
            if (!matcher.find()) {
                break;
            }
            String replacement = getReplacement(matcher);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String getReplacement(Matcher matcher) {
        String[] parts = matcher.group(1).split("\\.");
        StringBuilder builder = new StringBuilder();
        Object thing = context;
        for (String part: parts) {
            Object oldThing = thing;
            if (thing instanceof Map) {
                thing = ((Map)thing).get(part);
            } else {
                thing = PropertyUtils.getProperty(thing, part);
            }
            if (thing == null) {
                String msg = "In token " + matcher.group(1) + " attribute " + part + " is null.";
                if (strict) {
                    throw new UsageException(msg);
                } else {
                    Log.warn(msg);
                    return "";
                }
            }
        }
        return thing.toString();
    }

    public Boolean getStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }
}
