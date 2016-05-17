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

package io.stallion.dataAccess.file;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.Model;

import static io.stallion.utils.Literals.*;

import io.stallion.utils.json.JSON;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class JsonFilePersister<T extends Model> extends FilePersisterBase<T> {

    private static final Set<String> extensions = new HashSet<>(list("json"));

    @Override
    public Set<String> getFileExtensions() {
        return extensions;
    }

    public T doFetchOne(File file) {
        T o = null;
        try {
            String json = FileUtils.readFileToString(file);
            o = JSON.parse(json, this.getModelClass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return o;
    }


    @Override
    public void persist(T obj) {
        if (obj.getId() == null) {
            obj.setId(DataAccessRegistry.instance().getTickets().nextId());
        }
        String filePath = fullFilePathForObj(obj);
        File file = new File(filePath);
        File directory = new File(file.getParent());
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }





}
