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

package io.stallion.dataAccess.db.converters;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stallion.utils.json.JSON;

import java.io.IOException;

import static io.stallion.utils.Literals.empty;


public class JsonTypedObjectConverter extends TypedCollectionAttributeConverter<Object, String> {

    @Override
    public String convertToDatabaseColumn(Object dbData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(dbData);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String json) {
        if (empty(json)) {
            return null;
        }
        JavaType type = JSON.getMapper().getTypeFactory().constructType(getElementClass());
        try {
            return JSON.getMapper().readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
