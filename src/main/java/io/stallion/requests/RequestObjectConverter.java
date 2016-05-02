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

package io.stallion.requests;

import io.stallion.dal.base.Setable;
import io.stallion.dal.base.SettableOptions;
import io.stallion.exceptions.ClientException;
import io.stallion.exceptions.JsonMappingException;
import io.stallion.reflection.PropertyUtils;
import io.stallion.restfulEndpoints.RequestArg;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;
import io.stallion.utils.json.JSON;

import java.lang.reflect.Method;
import java.util.Map;

public class RequestObjectConverter {
    private RequestArg arg;
    private StRequest request;
    public RequestObjectConverter(RequestArg arg, StRequest request) {
        this.arg = arg;
        this.request = request;
    }

    public Object convert() throws JsonMappingException {
        String content = request.getContent();
        if (empty(content)) {
            throw new ClientException("Required a JSON body for this request, but content was empty.");
        }

        try {
            Object instance = JSON.parse(content, arg.getTargetClass());
            if (arg.getSettableAllowedForClass().isAssignableFrom(SettableOptions.Unrestricted.class)) {
                return instance;
            }
            for(Map.Entry<String, Object> entry: PropertyUtils.getProperties(instance).entrySet()) {
                // Any field that is not marked with the restricted annotation, gets nulled out

                if (entry.getValue() == null) {
                    continue;
                }
                if (arg.getSettableAllowedForClass() == null) {
                    continue;
                }
                Method getter = PropertyUtils.getGetter(instance, entry.getKey());
                Setable fieldSetable = (Setable)getter.getDeclaredAnnotation(Setable.class);

                if (fieldSetable == null) {
                    Log.finer("You passed in value {0} for {1}.{2} but that field does not have the required annotation {3}, so the value was dropped and nulled out.", entry.getValue(), instance.getClass().getSimpleName(), entry.getKey(), entry.getClass(), arg.getSettableAllowedForClass().getName());
                    PropertyUtils.setProperty(instance, entry.getKey(), null);
                    continue;
                }


                if (fieldSetable.value().equals(SettableOptions.Unrestricted.class)) {
                    continue;
                }
                if (fieldSetable.creatable() && arg.getSettableAllowedForClass().isAssignableFrom(SettableOptions.Createable.class)) {
                    continue;
                }

                // arg allowedFor is AnyUpdateable, field is OwnerUpdateable, then not allowed
                // arg allowedFor is OwnerUpdateable, field is AnyUpdateable then allowed
                // arg allowedFor is OwnerUpdateable, field is Unrestricted then allowed
                // OwnerUpdateable can be assigned from AnyUpdateable, because OwnerUpdateable is the super class
                // Thus, if allowedFor can be assignable from the field annotation class, then it is allowed
                // and we do not null out.
                if (arg.getSettableAllowedForClass().isAssignableFrom(fieldSetable.value())) {
                    continue;
                }
                Log.finer("You passed in value {0} for {1}.{2} but that field does not have the required annotation {3}, so the value was dropped and nulled out.", entry.getValue(), instance.getClass().getSimpleName(), entry.getKey(), entry.getClass(), arg.getSettableAllowedForClass().getName());
                PropertyUtils.setProperty(instance, entry.getKey(), null);
            }
            return instance;
        } catch (Exception e) {
            throw new JsonMappingException("Error parsing JSON to target object.", e);
        }
    }
}
