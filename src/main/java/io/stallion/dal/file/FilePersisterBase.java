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

package io.stallion.dal.file;

import io.stallion.dal.base.*;
import io.stallion.dal.db.DefaultSort;
import io.stallion.dal.filtering.FilterChain;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import io.stallion.fileSystem.FileSystemWatcherService;
import io.stallion.fileSystem.TreeVisitor;
import io.stallion.reflection.PropertyComparator;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.stallion.utils.Literals.*;

/**
 * A base persister that handles retrieving and saving model objects to the file sytem.
 *
 * @param <T>
 */
public abstract class FilePersisterBase<T extends Model> extends BasePersister<T> {

    private String bucketFolderPath = "";
    private Map<String, Long> fileToIdMap = new HashMap<>();
    private Map<String, Long> fileToTimestampMap = new HashMap<>();
    private Map<Long, String> idToFileMap = new HashMap<>();
    private boolean manyItemsPerFile = false;
    private String itemArrayName = "";
    protected String sortField = "lastModifiedMillis";
    protected String sortDirection = "DESC";


    @Override
    public void init(DalRegistration registration, ModelController<T> controller, Stash<T> stash) {
        super.init(registration, controller, stash);
        bucketFolderPath = registration.getAbsolutePath();
        manyItemsPerFile = registration.isMultiplePerFile();
        itemArrayName = registration.getItemArrayName();
        idToFileMap = new HashMap<>();
        fileToIdMap = new HashMap<>();

        if (!StringUtils.isEmpty(registration.getAbsolutePath())) {
            Boolean exists = new File(registration.getAbsolutePath()).isDirectory();
            Log.fine("DAL target {0} exists? {1}", registration.getAbsolutePath(), exists);
            if (!exists) {
                new File(registration.getAbsolutePath()).mkdirs();
            }
        }

        DefaultSort defaultSort = getModelClass().getAnnotation(DefaultSort.class);
        if (defaultSort != null) {
            sortField = defaultSort.field();
            sortDirection = defaultSort.direction();
        }

    }

    public abstract Set<String> getFileExtensions();

    public boolean matchesExtension(String path) {
        String extension = FilenameUtils.getExtension(path).toLowerCase();
        return getFileExtensions().contains(extension);
    }

    @Override
    public List<T> fetchAll()  {
        File target = new File(Settings.instance().getTargetFolder());
        if (!target.isDirectory()) {
            if (getItemController().isWritable()) {
                target.mkdirs();
            } else {
                throw new ConfigException(String.format("The JSON bucket %s (path %s) is read-only, but does not exist in the file system. Either create the folder, make it writable, or remove it from the configuration.", getItemController().getBucket(), getBucketFolderPath()));
            }
        }
        TreeVisitor visitor = new TreeVisitor();
        Path folderPath = FileSystems.getDefault().getPath(getBucketFolderPath());
        try {
            Files.walkFileTree(folderPath, visitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<T> objects = new ArrayList<>();
        for (Path path : visitor.getPaths()) {
            if (!matchesExtension(path.toString())) {
                continue;
            }

            if (path.toString().contains(".#")) {
                continue;
            }
            if (path.getFileName().startsWith(".")) {
                continue;
            }
            T o = fetchOne(path.toString());
            if (o != null) {
                objects.add(o);
            }

        }
        objects.sort(new PropertyComparator<T>(sortField));
        if (sortDirection.toLowerCase().equals("desc")) {
            Collections.reverse(objects);
        }

        return objects;
    }

    @Override
    public T fetchOne(T obj) {
        return fetchOne(fullFilePathForObj(obj));
    }


    @Override
    public T fetchOne(Long id) {
        return fetchOne(fullFilePathForId(id));
    }

    public T fetchOne(String filePath) {
        if (filePath.startsWith(".") || filePath.startsWith("#") || filePath.contains("..")) {
            return null;
        }
        if (!matchesExtension(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        T o = doFetchOne(file);
        if (o == null) {
            return null;
        }
        if (empty(o.getId())) {
            o.setId(makeIdFromFilePath(filePath));
        }

        Long ts = file.lastModified();
        o.setLastModifiedMillis(ts);

        handleFetchOne(o);
        onPostLoadFromFile(o, filePath);
        return o;
    }

    public abstract T doFetchOne(File file);


    public FilterChain<T> filterChain() {
        throw new UsageException("File based persistence does not work with filter chains. You have to use a LocalStash, which will provide an in memory filter chain.");
    }

    @Override
    public void hardDelete(T obj)  {
        String filePath = fullFilePathForObj(obj);
        File file = new File(filePath);
        file.delete();
    }


    public void onPostLoadFromFile(T obj, String path) {
        if (path.startsWith(getBucketFolderPath())) {
            path = path.replace(getBucketFolderPath(), "");
        }
        if (path.startsWith("/")) {
            path = StringUtils.stripStart(path, "/");
        }
        getIdToFileMap().put(obj.getId(), path);
        getFileToIdMap().put(path, obj.getId());
        if (obj instanceof ModelWithFilePath) {
            ((ModelWithFilePath) obj).setFilePath(path);
        }

    }

    public boolean reloadIfNewer(T obj) {
        String path = fullFilePathForObj(obj);
        File file = new File(path.toString());
        Long currentTs = file.lastModified();
        Long fileLastModified = or(obj.getLastModifiedMillis(), 0L);
        if (currentTs >= fileLastModified) {
            getStash().loadForId(obj.getId());
            fileToTimestampMap.put(path.toString(), currentTs);
            return true;
        }
        return false;
    }

    public String relativeFilePathForObj(T obj) {
        if (obj instanceof ModelWithFilePath) {
            if (!empty(((ModelWithFilePath) obj).getFilePath())) {
                return ((ModelWithFilePath) obj).getFilePath();
            } else {
                String path = ((ModelWithFilePath) obj).generateFilePath();
                ((ModelWithFilePath) obj).setFilePath(path);
                return path;
            }
        } else if (getIdToFileMap().containsKey(obj.getId())) {
            return getIdToFileMap().get(obj.getId());
        } else {
            return makePathForObject(obj);
        }
    }

    public String makePathForObject(T obj) {
        return obj.getId().toString() + ".json";
    }

    public String fullFilePathForObj(T obj) {
        String path = getBucketFolderPath();
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path + relativeFilePathForObj(obj);
    }

    public String fullFilePathForId(Long id) {
        String path = getBucketFolderPath();
        if (!path.endsWith("/")) {
            path += "/";
        }
        if (!getIdToFileMap().containsKey(id)) {
            return null;
        }
        return path + getIdToFileMap().get(id);
    }

    public void watchEventCallback(String filePath) {
        if (getFileToIdMap().containsKey(filePath)) {
            getStash().loadForId(getFileToIdMap().get(filePath));
        }
    }

    @Override
    public void attachWatcher()  {
        FileSystemWatcherService.instance().registerWatcher(
                new ItemFileChangeEventHandler(this)
                        .setWatchedFolder(this.getBucketFolderPath())
                        .setWatchTree(true)
        );
    }

    /**
     * Derives a Long id by hashing the file path and then taking the first 8 bytes
     * of the path. If there are a hundred thousand files, the chance of a single
     * collision will be 1 in one hundred million.
     *
     * This is used if the model object doesn't have a defined id field.
     *
     * @param path
     * @return
     */
    public Long makeIdFromFilePath(String path) {
        path = path.toLowerCase();
        path = path.replace(getBucketFolderPath().toLowerCase(), "");
        path = StringUtils.stripStart(path, "/");
        path = getBucket() + "-----" + path;
        // Derive a long id by hashing the file path
        // If there are a hundred thousand files, the chance of a single collision will be 1 in one hundred million
        byte[] bs = Arrays.copyOfRange(DigestUtils.md5(path), 0, 8);
        ByteBuffer bb = ByteBuffer.wrap(bs);
        Long l = bb.getLong();
        if (l < 0) {
            l = -l;
        }
        Log.finest("calculated id is {0}", l);
        return l;
    }


    public String getBucketFolderPath() {
        return bucketFolderPath;
    }

    public FilePersisterBase setBucketFolderPath(String bucketFolderPath) {
        this.bucketFolderPath = bucketFolderPath;
        return this;
    }


    public Map<String, Long> getFileToIdMap() {
        return fileToIdMap;
    }

    public FilePersisterBase setFileToIdMap(Map<String, Long> fileToIdMap) {
        this.fileToIdMap = fileToIdMap;
        return this;
    }


    public Map<Long, String> getIdToFileMap() {
        return idToFileMap;
    }

    public FilePersisterBase setIdToFileMap(Map<Long, String> idToFileMap) {
        this.idToFileMap = idToFileMap;
        return this;
    }

    public boolean isManyItemsPerFile() {
        return manyItemsPerFile;
    }

    public FilePersisterBase setManyItemsPerFile(boolean manyItemsPerFile) {
        this.manyItemsPerFile = manyItemsPerFile;
        return this;
    }

    public String getItemArrayName() {
        return itemArrayName;
    }

    public FilePersisterBase setItemArrayName(String itemArrayName) {
        this.itemArrayName = itemArrayName;
        return this;
    }
}
