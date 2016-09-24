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

package io.stallion.dataAccess;


import io.stallion.dataAccess.db.*;
import io.stallion.dataAccess.file.*;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.settings.ContentFolder;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Table;
import java.io.File;
import java.util.*;


import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * The DalRegistry, or Data Access Layer Registry, allows for ModelControllers to be
 * registered, booted up, and then accessed.
 *
 */
public class DataAccessRegistry implements Map<String, ModelController>  {
    private Map<String, ModelController> internalMap = new HashMap<String, ModelController>();
    private Map<String, String> modelClassToBucketName = new HashMap<>();
    private DB db;
    private Tickets tickets;

    private TextItemController<TextItem> pages;
    private TextItemController<TextItem> posts;

    private Set<String> deduping = new HashSet<String>();

    private static DataAccessRegistry _instance;

    public static DataAccessRegistry instance() {
        if (_instance == null) {
            throw new UsageException("You must call load() before accessing the DalRegistry.");
        }
        return _instance;
    }

    public static DataAccessRegistry load() {
        _instance = new DataAccessRegistry();
        try {
            _instance.loadAndHydrate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return _instance;
    }

    public void deregister(String bucket) {
        internalMap.remove(bucket);
        deduping.remove(bucket);
    }

    public static void shutdown() {
        _instance = null;
    }

    public DataAccessRegistry() {
        if (DB.instance() != null && DB.instance().getTickets() != null) {
            tickets = DB.instance().getTickets();
        } else {
            tickets = new TimebasedTickets();
        }
    }

    public void loadAndHydrate() throws Exception {
        loadAndHydrate(Settings.instance().getTargetFolder(), Settings.instance());
    }

    public void loadAndHydrate(String targetFolder, Settings settings) throws Exception {
        loadFileBasedDataAccess(targetFolder, settings);
        //loadDbBasedDal(settings);
    }

    /**
     * Loads data access controllers for the pages folder, posts folder, and any folders defined
     * in the settings [TargetFolder] block
     * @param targetFolder
     * @param settings
     * @throws Exception
     */
    private void loadFileBasedDataAccess(String targetFolder, Settings settings) throws Exception {
        List<DataAccessRegistration> registrations = new ArrayList<>();
        List<ContentFolder> folders = new ArrayList<>();
        if (settings.getFolders() != null) {
            folders.addAll(settings.getFolders());
        }

        List<String> names = new ArrayList<>();
        if (new File(targetFolder + "/pages").isDirectory()) {
            folders.add(new ContentFolder().setPath(targetFolder + "/pages").setType("markdown").setItemTemplate(settings.getPageTemplate()));
        }

        for (ContentFolder folder: folders) {
            DataAccessRegistration registration = new DataAccessRegistration();
            registration.setUseDataFolder(false);
            registration.setPath(folder.getPath());
            registration.setWritable(folder.getWritable());
            if (!StringUtils.isBlank(folder.getType()) && folder.getType().equals("json")) {
                registration.setControllerClass(StandardModelController.class);
                registration.setModelClass(MappedModelBase.class);
                registration.setPersisterClass(JsonFilePersister.class);
            } else if (!StringUtils.isBlank(folder.getType()) && folder.getType().equals("toml")) {
                registration.setControllerClass(TomlItemController.class);
                registration.setModelClass(TomlItem.class);
                registration.setPersisterClass(TomlPersister.class);
                registration.setWritable(false);
            } else {
                registration.setControllerClass(TextItemController.class);
                registration.setModelClass(TextItem.class);
                registration.setPersisterClass(TextFilePersister.class);

            }
            registration.setTemplatePath(folder.getItemTemplate());
            registration.setShouldWatch(true);
            if (!StringUtils.isEmpty(folder.getClassName())) {
                Class clazz = this.getClass().getClassLoader().loadClass(folder.getClassName());
                registration.setModelClass(clazz);
            }
            register(registration);
        }

        if (containsKey("pages")) {
            setPages((TextItemController) get("pages"));
        }
    }

    public ModelController registerDbModel(Class<? extends Model> model, Class<? extends ModelController> controller) {
        return registerDbModel(model, controller, LocalMemoryStash.class);
    }

    public ModelController registerDbModel(Class<? extends Model> model, Class<? extends ModelController> controller, boolean syncToMemory) {
        Class<? extends Stash> cls = LocalMemoryStash.class;
        if (!syncToMemory) {
            cls = NoStash.class;
        }
        return registerDbModel(model, controller, cls);
    }
    public ModelController registerDbModel(Class<? extends Model> model, Class<? extends ModelController> controller, Class<? extends Stash> stash) {
        return registerDbModel(model, controller, stash, null);
    }
    /**
     * Registers the given model and controller with a database persister, getting the bucket name
     * from the @Table annotation on the model.
     *
     * @param model
     * @param controller
     * @param stash
     * @return
     */
    public ModelController registerDbModel(Class<? extends Model> model, Class<? extends ModelController> controller, Class<? extends Stash> stash, String bucket) {
        Table anno = model.getAnnotation(Table.class);
        if (anno == null) {
            throw new UsageException("A @Table annotation is required on the model " + model.getCanonicalName() + " in order to register it.");
        }
        bucket = or(bucket, anno.name());
        String table = anno.name();
        DataAccessRegistration registration = new DataAccessRegistration()
                .setDatabaseBacked(true)
                .setPersisterClass(DbPersister.class)
                .setBucket(bucket)
                .setTableName(table)
                .setControllerClass(controller)
                .setStashClass(stash)
                .setModelClass(model);
        return register(registration);
    }

    /**
     * Registers the data store defined by the passed in DalRegistration.  Does everything
     * including instantiating the persister, controller, and stash; syncing all data
     * into the stash if applicable; setting up file system watchers if applicable; and
     * adding the controller to the internal registry so that it be accessed via
     * DalRegistry.instance().get(bucketName) and so that it will be available in
     * templates.
     *
     *
     *
     * @param registration
     */
    public ModelController register(DataAccessRegistration registration) {

        // Validation, de-duping, and normalization
        if (StringUtils.isEmpty(registration.getPath()) && StringUtils.isEmpty(registration.getTableName()) && empty(registration.getBucket())) {
            throw new ConfigException(String.format("You tried to load a model/controller. But both the folder path and the table name were empty. One the two must be set. model=%s, controller=%s", registration.getModelClass(), registration.getControllerClass()));
        }
        registration.build(settings().getTargetFolder());
        if (deduping.contains(registration.getBucket())) {
            throw new ConfigException(String.format("Bucket was registered twice: %s", registration.getBucket()));
        }
        if (!StringUtils.isEmpty(registration.getAbsolutePath())) {
            if (deduping.contains(registration.getAbsolutePath())) {
                throw new ConfigException(String.format("registered same path twice: %s", registration.getAbsolutePath()));
            }
        }
        if (internalMap.containsKey(registration.getBucket())) {
            throw new ConfigException(String.format("Bucket controller was registered twice: %s", registration.getBucket()));
        }
        deduping.add(registration.getAbsolutePath());
        deduping.add(registration.getBucket());




        // Load the persister

        Persister persister;
        try {
            persister = registration.getPersisterClass().newInstance();
        } catch (Exception e) {
            Log.warn("Could not instaniate persister instance for class {0} bucket {1}", registration.getPersisterClass(), registration.getRelativePath());
            throw new RuntimeException(e);
        }


        /*
        */

        // Register the model with the database, for schema purposes
        if (persister.isDbBacked()) {
            if (DB.isUseDummyPersisterForSqlGenerationMode()) {
                persister = new DummyPersister<>();
            }
            if (DB.instance() == null) {
                    throw new ConfigException("You are using a model that requires a database, but you database configuration is empty.");
            }
            if (registration.getDynamicModelDefinition() != null) {
                DB.instance().addDynamicModelDefinition(registration.getDynamicModelDefinition());
            } else {
                DB.instance().addModel(registration.getModelClass());
            }
        }  else if (registration.isWritable()) {
            File folder = new File(registration.getAbsolutePath());
            if (!folder.isDirectory()) {
                folder.mkdirs();
            }
        }


        // Register the stash
        Stash stash;
        try {
            stash = registration.getStashClass().newInstance();
        } catch (InstantiationException e) {
            Log.warn("Could not instaniate stash instance for class {0} bucket {1}", registration.getStashClass(), registration.getBucket());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            Log.warn("Could not instaniate stash instance for class {0} bucket {1}", registration.getStashClass(), registration.getBucket());
            throw new RuntimeException(e);

        }

        // Register the item controller
        ModelController controller;
        try {
            controller = registration.getControllerClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Log.warn("Could not instaniate controller instance for class {0} bucket {1}", registration.getControllerClass(), registration.getRelativePath());
            throw new RuntimeException(e);
        }



        // Initialize all the things
        controller.init(registration, persister, stash);
        persister.init(registration, controller, stash);
        stash.init(registration, controller, persister);


        // Add the controller to the DalRegistry lookup table
        internalMap.put(controller.getBucket(), controller);
        // Add the model to the DalRegistry lookup table
        Log.info("Register model {0}", registration.getModelClass().getCanonicalName());
        modelClassToBucketName.put(registration.getModelClass().getCanonicalName(), registration.getBucket());

        // Load all the items into the local stash
        stash.loadAll();


        // Attach any file system watchers
        if (registration.isShouldWatch()) {
            try {
                persister.attachWatcher();
            } catch (Exception e) {
                Log.warn("Could not attach watcher for persister {0} bucket {1} absolute path {2}", registration.getPersisterClass(), registration.getRelativePath(), registration.getAbsolutePath());
                throw new RuntimeException(e);
            }
        }
        return controller;
    }

    @Deprecated
    public ModelController getControllerForModelName(String modelName) {
        if (containsKey(modelName)) {
            return get(modelName);
        }
        String bucket = modelClassToBucketName.get(modelName);
        return get(bucket);
    }

    @Deprecated
    public ModelController getControllerForModel(Class<? extends Model> model) {
        return getControllerForModelName(model.getCanonicalName());
    }

    public ModelController<? extends Model> get(String key) {
        return internalMap.get(key);
    }

    public TextItemController<TextItem> getPages() {
        return pages;
    }

    public void setPages(TextItemController<TextItem> pages) {
        this.pages = pages;
    }

    public TextItemController<TextItem> getPosts() {
        return posts;
    }

    public void setPosts(TextItemController<TextItem> posts) {
        this.posts = posts;
    }

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    @Override
    public ModelController get(Object key) {
        try {
            return this.internalMap.get(key);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ModelController getNamespaced(String nameSpace, Object key) {
        try {
            return this.internalMap.get(nameSpace + "-" + key);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ModelController put(String key, ModelController value) {
        throw new RuntimeException("You must use registerController() method to add an item controller");
    }

    @Override
    public ModelController remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends ModelController> m) {

    }

    @Override
    public void clear() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<String> keySet() {
        return internalMap.keySet();
    }

    @Override
    public Collection<ModelController> values() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<Entry<String, ModelController>> entrySet() {
        return internalMap.entrySet();
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public Tickets getTickets() {
        return tickets;
    }

    public void setTickets(Tickets tickets) {
        this.tickets = tickets;
    }
}
