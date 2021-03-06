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

package io.stallion.tests.sql;

import com.fasterxml.jackson.core.type.TypeReference;
import io.stallion.dataAccess.db.converters.JsonAttributeConverter;
import io.stallion.utils.json.JSON;

import java.util.List;

import static io.stallion.utils.Literals.*;


public class PicnicAttendeesConverter implements JsonAttributeConverter<List<PicnicAttendence>> {
    @Override
    public String convertToDatabaseColumn(List<PicnicAttendence> attr) {
        return JSON.stringify(attr);
    }

    @Override
    public List<PicnicAttendence> convertToEntityAttribute(String dbData) {
        if (empty(dbData)) {
            return list();
        }
        TypeReference<List<PicnicAttendence>> typeRef = new TypeReference<List<PicnicAttendence>>() {};
        return (List<PicnicAttendence>)JSON.parse(dbData, typeRef);
    }
}
