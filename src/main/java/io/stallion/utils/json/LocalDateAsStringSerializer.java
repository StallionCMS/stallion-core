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

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.stallion.services.Log;


/**
 * Like com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer except
 * always serialize as a String instead of as an array.
 */
public class LocalDateAsStringSerializer extends LocalDateSerializer {

    protected LocalDateAsStringSerializer() {
        super();
    }

    protected LocalDateAsStringSerializer(LocalDateSerializer base, Boolean useTimestamp, DateTimeFormatter dtf) {
        super(base, useTimestamp, dtf);
    }

    public LocalDateAsStringSerializer(DateTimeFormatter formatter) {
        super(formatter);
    }

    protected LocalDateSerializer withFormat(Boolean useTimestamp, DateTimeFormatter dtf) {
        return new LocalDateAsStringSerializer(this, useTimestamp, dtf);
    }

    public void serialize(LocalDate date, JsonGenerator generator, SerializerProvider provider) throws IOException {

        String str = this._formatter == null ? date.toString() : date.format(this._formatter);
        generator.writeString(str);

    }

}
