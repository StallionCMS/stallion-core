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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jdk.nashorn.api.scripting.JSObject;

import java.io.IOException;

/**
 * Created by pfitzsimmons on 7/27/14.
 * Inspired by https://github.com/reactor/reactor-js/blob/master/reactor-js-core/src/main/java/reactor/js/core/json/JSObjectSerializer.java
 */
class JSObjectSerializer extends JsonSerializer<JSObject> {
    @Override
    public void serialize(JSObject value,
                          JsonGenerator jgen,
                          SerializerProvider provider) throws IOException,
            JsonProcessingException {
        if (value.isArray()) {
            jgen.writeStartArray();
            int i = -1;
            for (; ; ) {
                if (value.hasSlot(++i)) {
                    jgen.writeObject(value.getSlot(i));
                } else {
                    break;
                }
            }
            jgen.writeEndArray();
        } else {
            jgen.writeStartObject();
            for (String key : value.keySet()) {
                jgen.writeObjectField(key, value.getMember(key));
            }
            jgen.writeEndObject();
        }
    }
}
