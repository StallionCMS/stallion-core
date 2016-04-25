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

package io.stallion.dal.base;

import io.stallion.Context;
import io.stallion.dal.db.DB;
import io.stallion.dal.file.JsonFilePersister;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import org.apache.commons.lang3.StringUtils;

import static io.stallion.utils.Literals.empty;

public class DalRegistration {
    private Class<? extends ModelController> controllerClass;
    private Class<? extends Model> modelClass;
    private Class<? extends Persister> persisterClass;
    private Class<? extends Stash> stashClass;
    private boolean syncAllToMemory = true;
    private DynamicModelDefinition dynamicModelDefinition;
    private String path;
    private String relativePath;
    private String absolutePath;
    private String tableName;

    private boolean writable = false;
    private boolean shouldWatch = true;
    private String nameSpace;
    private boolean useDataFolder = true;
    private String templatePath = "";
    private String bucket = null;
    private boolean multiplePerFile = false;
    private String itemArrayName = "";
    private boolean databaseBacked = false;

    /**
     * Ensure all required fields are set and valid, hydrate any other fields with
     * defaults.
     *
     * @param appTargetPath
     * @return
     */
    public DalRegistration build(String appTargetPath) {
        if (controllerClass == null) {
            throw new ConfigException("You must choose a controller class for every DalRegistration");
        }
        ModelController controller;
        try {
            controller = controllerClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (modelClass == null) {
            modelClass = controller.getModelClass();
        }
        if (modelClass == null) {
            throw new ConfigException("You must either set the DalRegistration modelClass property, or the controller must return the model class");
        }
        if (persisterClass == null) {
            if (DB.available()) {
                persisterClass = DB.instance().getDefaultPersisterClass();
            } else {
                persisterClass = JsonFilePersister.class;
            }
        }
        if (stashClass == null) {
            if (syncAllToMemory) {
                stashClass = LocalMemoryStash.class;
            } else {
                stashClass = NoStash.class;
            }
        }

        if (empty(tableName)) {
            tableName = getBucket();
        }
        if (empty(getPath())) {
            setPath(getBucket());
        }

        hydratePaths(appTargetPath);


        return this;
    }

    /**
     * Parse the target path and hydrate the absolute and relative path fields.
     * @param appTargetPath
     */
    public void hydratePaths(String appTargetPath) {
        if (empty(getPath()) && empty(getTableName())) {
            throw new UsageException("DalRegistration must have a non-empty path or tableName");
        } else if (empty(getPath())) {
            return;
        }
        if (getPath().contains("|")) {
            String[] parts = getPath().split("\\|", 2);
            setPath(parts[0]);
            setItemArrayName(parts[1]);
            setMultiplePerFile(true);
        }

        if (getPath().startsWith("/")) {
            setAbsolutePath(getPath());
            setRelativePath(getAbsolutePath().replace(appTargetPath, ""));
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
        } else {
            if (!StringUtils.isEmpty(nameSpace)) {
                path = nameSpace + "-" + path;
            }
            if (isUseDataFolder()) {
                setAbsolutePath(Context.getSettings().getDataDirectory() + "/" + getPath());
            } else {
                setAbsolutePath(appTargetPath + "/" + getPath());
            }
            setRelativePath(getPath());
        }
    }

    public Class<? extends ModelController> getControllerClass() {
        return controllerClass;
    }

    /**
     * The ModelController subclass to be associated with this registration
     * @return
     */
    public DalRegistration setControllerClass(Class<? extends ModelController> controllerClass) {
        this.controllerClass = controllerClass;
        return this;
    }

    public Class<? extends Model> getModelClass() {
        return modelClass;
    }

    /**
     * The data model to be used with this registration
     * @return
     */
    public DalRegistration setModelClass(Class<? extends Model> modelClass) {
        this.modelClass = modelClass;
        return this;
    }

    public String getPath() {
        return path;
    }

    /**
     * For file based registrations, the path to the folder with the data. Should be relative.
     *
     * @return
     */
    public DalRegistration setPath(String path) {
        this.path = path;
        return this;
    }

    public String getRelativePath() {
        return relativePath;
    }

    /**
     * The path to the data, relative to the Settings.targetPath or relative to the
     * app-data path, if isUseDataFolder() is true.
     * @return
     */
    DalRegistration setRelativePath(String relativePath) {
        this.relativePath = relativePath;
        return this;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * The absolute file system path to the data
     * @return
     */
    DalRegistration setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * The database table for the data, for DB backed registrations
     * @return
     */
    public DalRegistration setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public Class<? extends Persister> getPersisterClass() {
        return persisterClass;
    }

    /**
     * The Persister subclass to use for this registration
     * @return
     */
    public DalRegistration setPersisterClass(Class<? extends Persister> persisterClass) {
        this.persisterClass = persisterClass;
        return this;
    }

    public boolean isWritable() {
        return writable;
    }

    /**
     * Is Stallion allowed to write to the datastore, or is it read only?
     * @param writable
     * @return
     */
    public DalRegistration setWritable(boolean writable) {
        this.writable = writable;
        return this;
    }

    public boolean isShouldWatch() {
        return shouldWatch;
    }

    /**
     * Should the file system be watched for changes, and reload changes automatically?
     *
     * @return
     */
    public DalRegistration setShouldWatch(boolean shouldWatch) {
        this.shouldWatch = shouldWatch;
        return this;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public DalRegistration setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    /**
     * The bucket is the shorthand by which the controller will be accessed from templates,
     * from DalRegistry.get(), etc. Usually this should be the table name or the folder name,
     * but you can set it to a custom value. It should be unique among all registrations.
     *
      * @return
     */
    public String getBucket() {
        if (!empty(bucket)) {
            return bucket;
        } else if (!StringUtils.isEmpty(getTableName())) {
            return getTableName();
        } else {
            return getRelativePath();
        }
    }

    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * For displayable registrations, the default template to use for rendering the object.
     *
     * @return
     */
    public DalRegistration setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
        return this;
    }

    public boolean isUseDataFolder() {
        return useDataFolder;
    }

    /**
     * If true, relative path will be relative to the app-data folder, not the project folder.
     * @param useDataFolder
     * @return
     */
    public DalRegistration setUseDataFolder(boolean useDataFolder) {
        this.useDataFolder = useDataFolder;
        return this;
    }

    /**
     * The bucket is the shorthand by which the controller will be accessed from templates,
     * from DalRegistry.get(), etc. Usually this should be the table name or the folder name,
     * but you can set it to a custom value. It should be unique among all registrations.
     *
     * @return
     */
    public DalRegistration setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public DynamicModelDefinition getDynamicModelDefinition() {
        return dynamicModelDefinition;
    }


    /**
     * Used for defining a model via Javascript
     *
     * @param dynamicModelDefinition
     * @return
     */
    public DalRegistration setDynamicModelDefinition(DynamicModelDefinition dynamicModelDefinition) {
        this.dynamicModelDefinition = dynamicModelDefinition;
        return this;
    }

    public boolean isMultiplePerFile() {
        return multiplePerFile;
    }

    /**
     * True for file-based data stores if there are multiple objects per file, instead of one file per object
     * @return
     */
    public DalRegistration setMultiplePerFile(boolean multiplePerFile) {
        this.multiplePerFile = multiplePerFile;
        return this;
    }

    public String getItemArrayName() {
        return itemArrayName;
    }

    /**
     * If isMultiplePerFile() is true, this is the name of the array of the objects.
     * @param itemArrayName
     * @return
     */
    public DalRegistration setItemArrayName(String itemArrayName) {
        this.itemArrayName = itemArrayName;
        return this;
    }

    /**
     * The class to use for the data stash. Default is LocalMemoryStash, which syncs
     * everything into local memory. Use NoStash to avoid syncing into memory.
     * @return
     */
    public Class<? extends Stash> getStashClass() {
        return stashClass;
    }

    public DalRegistration setStashClass(Class<? extends Stash> stashClass) {
        this.stashClass = stashClass;
        return this;
    }

    public boolean isSyncAllToMemory() {
        return syncAllToMemory;
    }

    /**
     * Should all objects from the data store be synced to local memory? Defaults to true.
     * @return
     */
    public DalRegistration setSyncAllToMemory(boolean syncAllToMemory) {
        this.syncAllToMemory = syncAllToMemory;
        return this;
    }


    public boolean isDatabaseBacked() {
        return databaseBacked;
    }

    /**
     * True if the registration should use the database, false if it should use the
     * file system.
     *
     * @param databaseBacked
     * @return
     */
    public DalRegistration setDatabaseBacked(boolean databaseBacked) {
        this.databaseBacked = databaseBacked;
        return this;
    }

}

