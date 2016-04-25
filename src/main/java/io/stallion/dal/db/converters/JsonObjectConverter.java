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

package io.stallion.dal.db.converters;

import com.fasterxml.jackson.databind.ObjectMapper;



public class JsonObjectConverter implements AttributeConverter<Object, String> {

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
        return json;
    }
}
