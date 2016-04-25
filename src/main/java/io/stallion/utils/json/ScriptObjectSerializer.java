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

package io.stallion.utils.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.stallion.services.Log;
import jdk.nashorn.internal.runtime.ScriptObject;

import java.io.IOException;

class ScriptObjectSerializer extends JsonSerializer<ScriptObject> {
    @Override
    public void serialize(ScriptObject value,
                          JsonGenerator jgen,
                          SerializerProvider provider) throws IOException,
            JsonProcessingException {
        if (value.isArray()) {
            jgen.writeStartArray();
            int i = -1;
            for (; ; ) {
                if (value.has(++i)) {
                    jgen.writeObject(value.get(i));
                } else {
                    break;
                }
            }
            jgen.writeEndArray();
        } else {
            jgen.writeStartObject();
            for (Object key : value.keySet()) {
                jgen.writeObjectField(key.toString(), value.get(key));
            }
            jgen.writeEndObject();
        }
    }
}

