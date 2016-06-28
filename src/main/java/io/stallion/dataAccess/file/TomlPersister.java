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

package io.stallion.dataAccess.file;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.stallion.dataAccess.MappedModel;
import io.stallion.dataAccess.Model;
import io.stallion.exceptions.ConfigException;
import io.stallion.fileSystem.TreeVisitor;
import io.stallion.reflection.PropertyUtils;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.stallion.utils.Literals.*;


public class TomlPersister<T extends Model> extends FilePersisterBase<T> {

    private static final Set<String> extensions = set("toml");
    @Override
    public Set<String> getFileExtensions() {
        return extensions;
    }

    @Override
    public List fetchAll()  {
        File target = new File(Settings.instance().getTargetFolder());
        if (!target.isDirectory()) {
            if (getItemController().isWritable()) {
                target.mkdirs();
            } else {
                throw new ConfigException(String.format("The TOML bucket %s (path %s) is read-only, but does not exist in the file system. Either create the folder, make it writable, or remove it from the configuration.", getItemController().getBucket(), getBucketFolderPath()));
            }
        }
        TreeVisitor visitor = new TreeVisitor();
        Path folderPath = FileSystems.getDefault().getPath(getBucketFolderPath());
        try {
            Files.walkFileTree(folderPath, visitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Object> objects = new ArrayList<>();
        for (Path path : visitor.getPaths()) {
            if (!path.toString().toLowerCase().endsWith(".toml")) {
                continue;
            }
            if (path.toString().contains(".#")) {
                continue;
            }
            Log.fine("Load from toml file " + path);
            if (isManyItemsPerFile()) {
                try {
                    Log.finer("Load toml path {0} and items {1}", path.toString(), getItemArrayName());
                    String toml = FileUtils.readFileToString(new File(path.toString()));
                    Toml t = new Toml().read(toml);
                    List<HashMap> models = t.getList(getItemArrayName());
                    long x = 0;
                    for (Map m: models) {
                        x++;
                        T o = getModelClass().newInstance();
                        for (Object key: m.keySet()) {
                            PropertyUtils.setProperty(o, key.toString(), m.get(key));
                        }
                        Log.info("add item {0}", ((MappedModel)o).get("title"));
                        if (empty(o.getId())) {
                            o.setId(x);
                        }
                        handleFetchOne(o);
                        objects.add(o);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }


            } else {
                objects.add(fetchOne(path.toString()));
            }


        }
        return objects;
    }

    public String makePathForObject(T obj) {
        return obj.getId() + ".toml";
    }


    public T doFetchOne(File file) {
        T o = null;
        try {
            String toml = FileUtils.readFileToString(file);
            o = new Toml().read(toml).to(this.getModelClass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return o;
    }


    @Override
    public void persist(T obj) {
        TomlWriter writer = new TomlWriter();

        try {
            writer.write(obj, new File(fullFilePathForObj(obj)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}


