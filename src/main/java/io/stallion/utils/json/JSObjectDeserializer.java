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

package io.stallion.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import jdk.nashorn.api.scripting.JSObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by pfitzsimmons on 7/27/14.
 * Inspired by https://github.com/reactor/reactor-js/blob/master/reactor-js-core/src/main/java/reactor/js/core/json/JSObjectDeserializer.java
 */
class JSObjectDeserializer  extends JsonDeserializer<JSObject> {
    @SuppressWarnings("unchecked")
    @Override
    public JSObject deserialize(JsonParser jp,
                                DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        Object obj = jp.readValueAs(Object.class);

        if (Map.class.isInstance(obj)) {
            return new JSMap((Map) obj);
        } else if (List.class.isInstance(obj)) {
            return new JSList((List) obj);
        } else {
            throw new JsonMappingException("Cannot convert value to a valid JSON object", jp.getCurrentLocation());
        }
    }
}
