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

package io.stallion.requests.validators;

import io.stallion.exceptions.ClientException;

import java.text.MessageFormat;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * A helper class for extracting required parameters from a user provided
 * hashmap.
 *
 * So instead of doing:
 * Map params = request().getBodyMap();
 * String code = params.getOrDefault("code", null);
 * if (empty(code)) {
 *     throw new ClientException("Could not find required parameter: code");
 * }
 *
 * String redirectUri = params.getOrDefault("redirectUri", null);
 * if (empty(code)) {
 *     throw new ClientException("Could not find required parameter: redirectUri");
 * }
 *
 * You do:
 *
 * ParamExtractor&lt;String&gt; params = new ParamExtractor(request().getBodyMap(), "Required post body parameter {0} was not found.");
 * String code = params.get("code");
 * String redirectUri = params.get("redirect_uri");
 *
 * And the exception with proper messaging automatically gets bubbled up to the user if the parameter is empty.
 *
 * @param <T>
 */
public class ParamExtractor<T> {
    private Map<Object, T> map;
    private String messageTemplate;

    public ParamExtractor(Map<Object, T> map) {
        this(map, null);
    }


    /**
     *
     * @param map
     * @param messageTemplate - Formatted using MessageFormat use {0} for the parameter name.
     */
    public ParamExtractor(Map map, String messageTemplate) {
        this.map = map;
        if (empty(messageTemplate)) {
            messageTemplate = "Could not find required parameter: {0}";
        }
        this.messageTemplate = messageTemplate;

    }

    /**
     * New RequiredParamMapper from the current request body.
     *
     * @param <T>
     * @return
     */
    public <T> ParamExtractor<T> fromRequest() {
        return new ParamExtractor<T>(request().getBodyMap(), "Required POST body parameter {0} was not found.");
    }

    /**
     * Gets the parameter, throws a ClientException 400 error with the message template if the parameter
     * is empty.
     *
     * @param key
     * @return
     */
    public T get(Object key) {
        T value = map.getOrDefault(key, null);
        if (empty(value)) {
            throw new ClientException(MessageFormat.format(messageTemplate, key));
        }
        return value;
    }



}
