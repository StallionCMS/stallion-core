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

package io.stallion.plugins.javascript;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class JsModelSerializer extends JsonSerializer<BaseJavascriptModel> {
    @Override
    public void serialize(BaseJavascriptModel baseJavascriptModel, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        for(String key: baseJavascriptModel.keySet()) {
            if (key.equals("attributes") || key.equals("isNewInsert")) {
                continue;
            }
            if (!baseJavascriptModel.getJsonIgnoredColumns().contains(key)) {
                map.put(key, baseJavascriptModel.get(key));
            }
        }
        for(String key: baseJavascriptModel.getDynamicProperties().keySet()) {
            BaseDynamicColumn col = baseJavascriptModel.getDynamicProperties().get(key);
            map.put(key, col.func(baseJavascriptModel));
        }
        jsonGenerator.writeObject(map);
    }
}
