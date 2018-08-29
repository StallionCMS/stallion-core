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

package io.stallion.jerseyProviders;

import com.fasterxml.jackson.databind.JsonNode;
import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;
import io.stallion.utils.json.JSON;
import org.apache.commons.io.IOUtils;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.stallion.utils.Literals.*;


/**
 * Base class for factories that can instantiate object of a given type.
 *
 * @param <T> the type of the objects that the factory creates
 */
public class BodyParamProvider<T> implements Factory<T>, ValueParamProvider {


    public BodyParamProvider() {

    }

    // org.glassfish.hk2.api.Factory<T>


    @PerLookup
    @Override
    public T provide() {
        Log.info("TypeFactory provide method called.");
        return null;
    }

    @Override
    public void dispose(T instance) {}

    // org.glassfish.jersey.server.spi.internal.ValueParamProvider

    @Override
    public Function<ContainerRequest, ?> getValueProvider(Parameter parameter) {
        if (parameter.isAnnotationPresent(BodyParam.class)) {
            return new MyParamValueProvider(parameter);
        }
        return null;
    }

    @Override
    public PriorityType getPriority() {
        return Priority.NORMAL;
    }

    // java.util.Function<ContainerRequest, T>

    /*
    @Override
    public T apply(ContainerRequest request) {
        ExtendedUriInfo extendedUriInfo = ((ExtendedUriInfo)request.getUriInfo());

        //return provide();
    }
    */

    public static class  MyParamValueProvider implements Function<ContainerRequest, Object> {
        private Parameter parameter;
        private BodyParam bpAnno;
        private String jsonFieldName = "";

        public MyParamValueProvider(Parameter parameter) {
            this.parameter = parameter;
            this.bpAnno = parameter.getAnnotation(BodyParam.class);
            if (empty(bpAnno.value())) {
                jsonFieldName = parameter.getSourceName();
            } else {
                jsonFieldName = bpAnno.value();
            }
        }

        @Override
        public Object apply(ContainerRequest containerRequest) {
            JsonNode jsonNode = (JsonNode)containerRequest.getProperty("_bodyparam_json_node");
            if (jsonNode == null) {
                try {
                    jsonNode = JSON.getMapper().reader().readTree(IOUtils.toString(containerRequest.getEntityStream(), UTF8));
                } catch (IOException e) {
                    throw new ClientErrorException("Could not read request as valid JSON string.", 400);
                }
                containerRequest.setProperty("_bodyparam_json_node", jsonNode);
            }
            if (!jsonNode.hasNonNull(jsonFieldName)) {
                return null;
            }
            JsonNode valNode = jsonNode.get(jsonFieldName);

            if (valNode.isNull()) {
                return null;
            }

            Object value = jsonNodeToJavaObject(valNode, 1);


            if (value == null) {
                throw new ClientErrorException("Passed in value for " + bpAnno.value() + " was not of an expected type.", 400);
            }


            if (!parameter.getType().equals(value.getClass())) {
                value = PropertyUtils.transform(value, parameter.getRawType());
            }
            return value;

        }
    }

    public static Object jsonNodeToJavaObject(JsonNode valNode, int depth) {
        if (depth > 10) {
            Log.warn("Node depth too great for a body param.");
            return null;
        }
        Object value = null;
        if (valNode.isNull()) {
            return null;
        } else if (valNode.isTextual())  {
            value = valNode.textValue();
        } else if (valNode.isDouble()) {
            value = valNode.doubleValue();
        } else if (valNode.canConvertToLong()) {
            value = valNode.asLong();
        } else if (valNode.isBoolean()) {
            value = valNode.booleanValue();
        } else if (valNode.isBigDecimal()) {
            value = valNode.decimalValue();
        } else if (valNode.isBigInteger()) {
            value = valNode.bigIntegerValue();
        } else if (valNode.isFloat()) {
            value = valNode.doubleValue();
        } else if (valNode.isArray()) {
            List listVal = list();
            for(int x = 0; x<valNode.size(); x++) {
                listVal.add(jsonNodeToJavaObject(valNode.get(x), depth + 1));
            }
            value = listVal;
        } else if (valNode.isObject()) {
            Map mapVal = map();
            Iterator<Map.Entry<String, JsonNode>> fields = valNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                mapVal.put(field.getKey(), jsonNodeToJavaObject(field.getValue(), depth + 1));
            }
            value = mapVal;
        }
        return value;
    }
}