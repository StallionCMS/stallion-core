/*
 * Copyright (c) 2011-2014 GoPivotal, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.stallion.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.stallion.exceptions.JsonWriteException;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.runtime.ScriptObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSON {

    private static final ObjectMapper mapper;


    static {

        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        mapper.registerModule(new JavaTimeModule());
        //mapper.findAndRegisterModules();

        SimpleModule mod = new SimpleModule("nashornConverter", Version.unknownVersion());

        mod.addSerializer(JSObject.class, new JSObjectSerializer());
        mod.addSerializer(ScriptObject.class, new ScriptObjectSerializer());
        mod.addDeserializer(JSObject.class, new JSObjectDeserializer());


        //SimpleModule mod2 = new SimpleModule("rawJsonConverter", Version.unknownVersion());
        //mod2.addSerializer(RawJson.class, new RawJsonSerializer());

        mapper.registerModules(mod);

    }

    public static ObjectMapper getMapper() {
        return mapper;
    }


    public static Map<String, Object> parseMap(String json) {
        return parse(json, new TypeReference<Map<String, Object>>(){});
    }

    public static <T> T parse(String json, TypeReference<T> typeReference) throws io.stallion.exceptions.JsonMappingException  {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new io.stallion.exceptions.JsonMappingException("Exception parsing json", e);
        }
    }

    /*
    public static Object parse(String json, Class cls) throws IOException {
        return mapper.readValue(json, cls);
    }
    */

    public static <T> T parse(String json, Class<T> cls) throws io.stallion.exceptions.JsonMappingException {
        try {

            return mapper.readValue(json, cls);
        } catch (IOException e) {
            throw new io.stallion.exceptions.JsonMappingException("Exception parsing: json" , e);
        }
    }

    public static JSObject parse(String json) throws io.stallion.exceptions.JsonMappingException {
        try {
            return mapper.readValue(json, JSObject.class);
        } catch (IOException e) {
            throw new io.stallion.exceptions.JsonMappingException("Exception parsing json", e);
        }
    }

    public static List parseList(String json) throws io.stallion.exceptions.JsonMappingException {
        try {
            return mapper.readValue(json, ArrayList.class);
        } catch (IOException e) {
            throw new io.stallion.exceptions.JsonMappingException("Exception parsing json", e);
        }
    }


    public static String stringify(Object obj) throws JsonWriteException {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonWriteException(e);
        }
    }

    public static String stringify(Object obj, Class view) throws JsonProcessingException {
        return stringify(obj, view, false);
    }

    public static String stringify(Object obj, Class view, Boolean excludeByDefault) throws JsonProcessingException {
        ObjectMapper objectMapper = mapper;
        if (excludeByDefault) {
            objectMapper = mapper.copy();
            objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        }
        ObjectWriter writer = objectMapper.writerWithView(view);
        return writer.writeValueAsString(obj);
    }

}