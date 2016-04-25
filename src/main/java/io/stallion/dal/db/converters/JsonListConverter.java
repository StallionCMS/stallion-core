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

import io.stallion.utils.json.JSON;
import org.apache.commons.collections.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class JsonListConverter implements JsonAttributeConverter<List> {
    @Override
    public String convertToDatabaseColumn(List attr) {
        return JSON.stringify(attr);
    }

    @Override
    public List convertToEntityAttribute(String dbData) {
        if (empty(dbData)) {
            return list();
        }
        return JSON.parse(dbData, ArrayList.class);
    }
}
