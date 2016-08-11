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

package io.stallion.dataAccess.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import io.stallion.dataAccess.AlternativeKey;
import io.stallion.dataAccess.DynamicModelDefinition;
import io.stallion.dataAccess.Tickets;
import io.stallion.dataAccess.UniqueKey;
import io.stallion.dataAccess.Model;
import io.stallion.dataAccess.db.converters.*;
import io.stallion.dataAccess.db.mysql.MySqlDbImplementation;
import io.stallion.dataAccess.db.postgres.PostgresDbImplementation;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import io.stallion.plugins.javascript.BaseJavascriptModel;
import io.stallion.services.Log;
import io.stallion.reflection.PropertyUtils;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.DbConfig;
import io.stallion.utils.Literals;
import io.stallion.utils.StallionClassLoader;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;


import javax.persistence.Column;
import javax.persistence.Table;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.sql.*;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * A singleton that stores the database connection pool, registered model schemas,
 * and provides a bunch of helper methods for actually talking to the database.
 */
public class DB {
    //@Converter(converterName = "")
    private List<Schema> tables = new ArrayList<Schema>();
    private Map<String, Schema> classToSchema = new HashMap<String, Schema>();
    private DataSource dataSource;
    private Map<String, AttributeConverter> converters = new HashMap<String, AttributeConverter>();
    private Tickets tickets;

    private DbImplementation dbImplementation;
    private static boolean useDummyPersisterForSqlGenerationMode = false;

    private static DB _instance;

    public static DB instance() {
        return _instance;
    }

    public static boolean available() {
        if (_instance != null) {
            return true;
        }
        return false;
    }



    /**
     * Load the database based on the configuration defined in the stallion.toml settings.
     * @return
     */
    public static DB load() {
        if (emptyInstance(Settings.instance().getDatabase()) || empty(Settings.instance().getDatabase().getUrl())) {
            return null;
        }
        Properties p = new Properties(System.getProperties());
        p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); // or any other
        System.setProperties(p);

        DbConfig config = Settings.instance().getDatabase();
        DB db;
        if (empty(config.getDbAccessorClass()) || "io.stallion.dataAccess.db.DB".equals(config.getDbAccessorClass())) {
            db = new DB();
        } else {
            Class cls = StallionClassLoader.loadClass(config.getDbAccessorClass());
            if (cls == null) {
                throw new ConfigException("The dbAccessorClass you chose in the database settings could not be found: " + config.getDbAccessorClass());
            }
            try {
                db = (DB)cls.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        db.initialize(config);


        _instance = db;
        return _instance;
    }

    /**
     * Close the datasource pool, null out the singleton instance.
     */
    public static void shutdown() {
        if (_instance != null) {
            if (_instance.getDataSource() instanceof  ComboPooledDataSource) {
                ((ComboPooledDataSource)_instance.getDataSource()).close();
            }
        }
        _instance = null;
    }

    /**
     * Intialize the database based on the passed in configuration object.
     * @param config
     */
    public void initialize(DbConfig config) {
        try {
            dbImplementation = (DbImplementation)StallionClassLoader.loadClass(config.getImplementationClass()).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Test out the connection. We do this directly, because if we test via the ComboPooledDataSource
        // exceptions will make the driver hang while retrying, and will also bury the underlying cause

        try {
            Driver driver = (Driver)StallionClassLoader.loadClass(config.getDriverClass()).newInstance();
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            props.setProperty("password", config.getPassword());
            try (Connection conn = driver.connect(config.getUrl(), props)) {
                Statement st = conn.createStatement();
                ResultSet results = st.executeQuery("SELECT 1 AS oneCol");
                results.next();
                Long i = results.getLong("oneCol");
                assert i == 1L;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }



        ComboPooledDataSource cpds = new ComboPooledDataSource();
        /*
        try {
            try (Connection conn = cpds.getConnection()) {
                Statement st = conn.createStatement();
                ResultSet results = st.executeQuery("SELECT 1");
                Long i = results.getLong(0);
                assert i == 1L;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        */
        try {
            cpds.setDriverClass(config.getDriverClass()); //loads the jdbc driver
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }

        String url = config.getUrl();
        if (!url.contains("?")) {
            url += "?";
        }
        // Assume the database server is in UTC
        if (!url.contains("&useLegacyDatetimeCode=")) {
            url += "&useLegacyDatetimeCode=false";
        }
        if (!url.contains("&serverTimezone=")) {
            url += "&serverTimezone=UTC";
        }
        //&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC

        cpds.setJdbcUrl(url);
        cpds.setUser(config.getUsername());
        cpds.setPassword(config.getPassword());


        cpds.setAcquireRetryAttempts(10);
        cpds.setAcquireRetryDelay(200);
        //cpds.setCheckoutTimeout(1);
        // the settings below are optional -- c3p0 can work with defaults
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        cpds.setIdleConnectionTestPeriod(5000);
        cpds.setTestConnectionOnCheckin(true);

        this.dataSource = cpds;


        // Make sure the database server time is UTC and in sync with the local server time
        // or else stop execution to prevent nasty and insiduious errors.
        //Timestamp date = this.queryScalar(dbImplementation.getCurrentTimeStampQuery());
        Timestamp date = this.queryScalar(dbImplementation.getCurrentTimeStampQuery());
        ZonedDateTime now = utcNow();
        ZonedDateTime dbTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.of("UTC"));

        //LocalDateTime now = utcNow().toLocalDateTime();
        ZonedDateTime max = now.plusMinutes(2);
        ZonedDateTime min = now.minusMinutes(2);

        //LocalDateTime dbTime = date.toLocalDateTime();
        if (dbTime.isAfter(max) || dbTime.isBefore(min)) {
            throw new ConfigException("The database CURRENT_TIMESTAMP() is mismatched with the server time. Db time is " + dbTime + ". Server time is "  + now + ". Make sure the database server is in UTC and that all your servers clocks are matched. ");
        }


        // Todo: why not lazy load converters???
        registerConverter(new JsonMapConverter());
        registerConverter(new JsonSetConverter());
        registerConverter(new JsonObjectConverter());
        registerConverter(new JsonListConverter());

        this.tickets = dbImplementation.initTicketsService(this);

    }

    /**
     * Instantiate an instance of a Tickets subclass, based on the database type
     * @return
     */
    public Tickets newTicketsGeneratorInstance() {
        return dbImplementation.initTicketsService(this);
    }

    public Class getDefaultPersisterClass() {
        return DbPersister.class;
    }

    public DB registerConverter(AttributeConverter converter) {
        converters.put(converter.getClass().getName(), converter);
        return this;
    }

    /**
     * Returns a QueryRunner instance associated with the underlying dataSource
     * @return
     */
    public QueryRunner newQuery() {
        return new QueryRunner(dataSource);
    }

    /**
     * Update the database with this object, or insert it if it does not exist.
     *
     * @param obj
     * @return
     */
    public Model save(Model obj) {

        if (obj.getId() == null) {

            insert(obj);
            return obj;
        } else if (obj.getIsNewInsert()) {
            insert(obj);
            obj.setIsNewInsert(false);
            return obj;
        } else {
            int affected = update(obj);
            if (affected == 0) {
                insert(obj);
            }
            return obj;
        }
    }

    /**
     * Query the database with arbitrary SQL and return a scalar object (a string, number, boolean, etc).
     *
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public <T> T queryScalar(String sql, Object...params) {

        QueryRunner runner = new QueryRunner(dataSource);
        try {
            return runner.query(sql, new ScalarHandler<T>(), params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Fetch all objects of the given model.
     *
     * @param model
     * @param <T>
     * @return
     */
    public <T extends Model> List<T> fetchAll(Class<T> model) {
        return fetchAll(model, null, null);
    }

    /**
     * Fetch the object with the given id.
     *
     * @param model
     * @param id
     * @param <T>
     * @return
     */
    public <T> T fetchOne(Class<? extends T> model, Object id) {
        return fetchOne(model, "id", id);
    }

    /**
     * Fetch an object where field==value
     *
     * @param model
     * @param field
     * @param value
     * @param <T>
     * @return
     */
    public <T> T fetchOne(Class<? extends T> model, String field, Object value) {
        Schema schema = getSchemaForModelClass(model);
        QueryRunner runner = new QueryRunner(dataSource);
        //ResultSetHandler handler = new BeanHandler(model);
        ResultSetHandler handler = new ModelResultHandler(schema);
        String sql = "SELECT * FROM " + schema.getName() + " WHERE " + field + "=? LIMIT 1";
        T record = null;
        try {
            record = (T)runner.query(sql, handler, value);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return record;
    }

    /**
     * Fetch all objects where column field is of value
     *
     * @param model
     * @param field
     * @param value
     * @param <T>
     * @return
     */
    public <T extends Model> List<T> fetchAll(Class<? extends T> model, String field, Object value) {
        Schema schema = getSchemaForModelClass(model);
        QueryRunner runner = new QueryRunner(dataSource);
        ModelListHandler<T> handler = new ModelListHandler<T>(schema);

        String sql = "SELECT * FROM " + schema.getName();
        if (!empty(field)) {
            sql += " WHERE " + field + "=?";
        }
        List records = null;
        try {
            if (!empty(field)) {
                records = runner.query(sql, handler, value);
            } else {
                records = runner.query(sql, handler);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    /**
     * Fetch all objects, sorted
     *
     * @param model
     * @param <T>
     * @return
     */
    public <T extends Model> List<T> fetchAllSorted(Class<? extends T> model, String sortField, String sortDirection) {
        Schema schema = getSchemaForModelClass(model);
        QueryRunner runner = new QueryRunner(dataSource);
        ModelListHandler<T> handler = new ModelListHandler<T>(schema);
        sortDirection = sortDirection.toUpperCase();
        if (!"ASC".equals(sortDirection) && !"DESC".equals(sortDirection)) {
            throw new UsageException("Invalid sort direction: " + sortDirection);
        }

        if (!"id".equals(sortField)  && !"row_updated_at".equals(sortField) && !schema.getKeyNames().contains(sortField)) {
            throw new UsageException("Sort field must be a database key. Sort field was: " + sortField + " on model " + model.getCanonicalName());
        }

        String sql = "SELECT * FROM " + schema.getName() + " ORDER BY " + sortField + " " + sortDirection;
        List records = null;
        try {
            records = runner.query(sql, handler);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    /**
     * Find a list of rows via arbitrary SQL, and return those rows as hashmaps
     *
     * @param sql
     * @param args
     * @return
     */
    public List<Map<String, Object>> findRecords(String sql, Object... args) {
        QueryRunner runner = new QueryRunner(dataSource);
        MapListHandler handler = new MapListHandler();
        try {
            return runner.query(sql, handler, args);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find one record via arbitrary SQL and return it as a hashmap
     *
     * @param sql
     * @param args
     * @return
     */
    public Map<String, Object> findRecord(String sql, Object... args) {
        QueryRunner runner = new QueryRunner(dataSource);
        MapHandler handler = new MapHandler();
        try {
            return runner.query(sql, handler, args);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find one object of the given model via arbitrary SQL
     *
     * @param model
     * @param sql
     * @param args
     * @param <T>
     * @return
     */
    public <T extends Model> T queryForOne(Class<T> model, String sql, Object ...args) {
        List<T> models = query(model, sql, args);
        if (models.size() < 1) {
            return null;
        } else {
            return models.get(0);
        }
    }

    /**
     * Find a list of objects of the given model via arbitrary SQL
     *
     * @param model
     * @param sql
     * @param args
     * @param <T>
     * @return
     */
    public <T extends Model> List<T> query(Class<T> model, String sql, Object ...args) {
        QueryRunner runner = new QueryRunner(dataSource);
        Schema schema = null;
        if (Model.class.isAssignableFrom(model)) {
            schema = getSchemaForModelClass(model);
        }
        if (schema != null) {
            ModelListHandler<T> handler = new ModelListHandler<T>(schema);
            try {
                return runner.query(sql, handler, args);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            BeanListHandler<T> handler = new BeanListHandler(model);
            try {
                return runner.query(sql, handler, args);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Find a list of objects of the given model via arbitrary SQL.
     * Accepts any java bean, does not require a Stallion Model
     *
     * @param model
     * @param sql
     * @param args
     * @param <T>
     * @return
     */
    public <T> List<T> queryBean(Class<T> model, String sql, Object ...args) {
        QueryRunner runner = new QueryRunner(dataSource);
        BeanListHandler<T> handler = new BeanListHandler(model);
        try {
            return runner.query(sql, handler, args);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find a list of objects via arbitrary SQL, checking the cache first, and storing to the
     * cache if retrieved fromt he database.
     *
     * @param model
     * @param sql
     * @param args
     * @param <T>
     * @return
     */
    public <T extends Model> List<T> cachedQuery(Class<T> model, String sql, Object ...args) {
        String cacheKey = buildCacheKey(model, sql, args);
        Object result = SmartQueryCache.getSmart(model.getCanonicalName(), cacheKey);
        if (result != null) {
            return (List<T>)result;
        }
        QueryRunner runner = new QueryRunner(dataSource);
        List<T> items = list();
        try {
            Schema schema = getSchemaForModelClass(model);
            if (!emptyInstance(schema)) {
                ModelListHandler<T> handler = new ModelListHandler<T>(schema);
                items = runner.query(sql, handler, args);
            } else {
                BeanListHandler<T> handler = new BeanListHandler(model);
                items = runner.query(sql, handler, args);
            }
            SmartQueryCache.set(model.getCanonicalName(), cacheKey, items);
            return items;
        } catch (SQLException e) {
            SmartQueryCache.set(model.getCanonicalName(), cacheKey, items);
            throw new RuntimeException(e);
        }
    }

    /**
     * Find a single value based on the given model, from arbitrary SQL, and
     * use the cache.
     *
     * @param model
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public <T> T cachedScalar(Class model, String sql, Object ...params) {
        String cacheKey = buildCacheKey(model, sql, params);
        T result = (T) SmartQueryCache.getSmart(model.getCanonicalName(), cacheKey);
        if (result != null) {
            return result;
        }
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            result = runner.query(sql, new ScalarHandler<T>(), params);
            SmartQueryCache.set(model.getCanonicalName(), cacheKey, result);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Select from the given model, using just an arbitrary WHERE ... clause, and use the cache.
     *
     * @param model
     * @param where
     * @param args
     * @param <T>
     * @return
     */
    public <T extends Model> List<T> cachedWhere(Class<T> model, String where, Object ...args) {
        String cacheKey = buildCacheKey(model, where, args);
        Object result = SmartQueryCache.getSmart(model.getCanonicalName(), cacheKey);
        if (result != null) {
            return (List<T>)result;
        }
        Schema schema = getSchemaForModelClass(model);

        QueryRunner runner = new QueryRunner(dataSource);
        //ResultSetHandler handler = new BeanHandler(model);
        String sql = "SELECT * FROM " + schema.getName() + " WHERE " + where;
        ModelListHandler<T> handler = new ModelListHandler<T>(schema);
        List<T> items = list();
        try {
            items = runner.query(sql, handler, args);

            SmartQueryCache.set(model.getCanonicalName(), cacheKey, items);
            return items;
        } catch (SQLException e) {
            SmartQueryCache.set(model.getCanonicalName(), cacheKey, items);
            throw new RuntimeException(e);
        }
    }

    /**
     * Turns a SQL query into a unique md5 hash to be used as a cache key.
     *
     * @param model
     * @param sql
     * @param args
     * @return
     */
    protected String buildCacheKey(Class model, String sql, Object ...args) {
        StringBuilder builder = new StringBuilder();
        builder.append("SQLQuery" + Literals.GSEP);
        builder.append(model.getCanonicalName() + Literals.GSEP);
        builder.append(sql + Literals.GSEP);
        for (Object arg: args) {
            builder.append(arg.toString() + Literals.GSEP);
        }
        String fullKey = builder.toString();
        return DigestUtils.md5Hex(fullKey);
    }


    /**
     * Find an object using an arbitrary SQL WHERE ... clause
     *
     * @param model
     * @param where
     * @param args
     * @return
     */
    public Object where(Class model, String where, Object ...args) {
        Schema schema = getSchemaForModelClass(model);

        QueryRunner runner = new QueryRunner(dataSource);
        //ResultSetHandler handler = new BeanHandler(model);
        String sql = "SELECT * FROM \"" + schema.getName() + "\" WHERE " + where;
        ModelListHandler handler = new ModelListHandler(schema);
        try {
            return runner.query(sql, handler, args);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete the object from the database.
     *
     * @param obj
     */
    public void delete(Model obj) {
        Schema schema = getSchemaForModelClass(obj.getClass());

        String sql = "DELETE FROM " + schema.getName() + " WHERE id=?";
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            runner.update(sql, obj.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        obj.setDeleted(true);
    }

    /**
     * Update the object
     *
     * @param obj
     * @return rows affected
     */
    public int update(Model obj) {
        Schema schema = getSchemaForModelClass(obj.getClass());

        String sql = "UPDATE `" + schema.getName() + "` SET ";
        List args = new ArrayList<>();
        for (Col col: schema.getColumns()) {
            if (col.getUpdateable()) {
                sql += "`" + col.getName() + "`" + "=?, ";
                Object arg = PropertyUtils.getPropertyOrMappedValue(obj, col.getPropertyName());
                arg = convertColumnArg(obj, col, arg);
                args.add(arg);
            }
        }
        sql = StringUtils.strip(sql.trim(), ",");
        sql += " WHERE id=?";
        args.add(obj.getId());
        QueryRunner run = new QueryRunner( dataSource );
        int affected = 0;
        try {
            affected = run.update(sql, args.toArray());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return affected;
    }

    /**
     * Insert the object into the database.
     *
     * @param obj
     * @return
     */
    public Model insert(Model obj) {
        Schema schema = getSchemaForModelClass(obj.getClass());

        if (obj.getId() == null) {
            obj.setId(dal().getTickets().nextId());
        }
        String sql = "INSERT INTO `" + schema.getName() + "`  (id ";
        List args = new ArrayList<>();
        args.add(obj.getId());
        for(Col col: schema.getColumns()) {
            if (col.getInsertable()) {
                sql += ", `" + col.getName() + "` ";
            }
        }
        sql += ") VALUES(?";
        for(Col col: schema.getColumns()) {
            if (col.getInsertable()) {
                sql += ", ?";
                Object arg = PropertyUtils.getPropertyOrMappedValue(obj, col.getPropertyName());
                if (arg == null && col.getDefaultValue() != null) {
                    arg = col.getDefaultValue();
                    PropertyUtils.setProperty(obj, col.getPropertyName(), arg);
                }
                arg = convertColumnArg(obj, col, arg);
                args.add(arg);
            }
        }
        sql += ") ";
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            runner.update(sql, args.toArray());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    /**
     * Trys to convert java type into what is needed by JDBC to store to the database.
     *
     * @param o
     * @param col
     * @param arg
     * @return
     */
    public Object convertColumnArg(Model o, Col col, Object arg) {

        if (arg == null && col.getDefaultValue() != null) {
            arg = col.getDefaultValue();
        }
        if (col.getConverter() != null) {
            arg = col.getConverter().toDb(o, arg, col.getPropertyName());
        } else if (arg != null && !StringUtils.isBlank(col.getConverterClassName())) {
            AttributeConverter converter = this.getConverter(col.getConverterClassName());
            arg = converter.convertToDatabaseColumn(arg);
        } else if (arg instanceof LocalDateTime) {
            arg = new Timestamp(((LocalDateTime) arg).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } else if (arg instanceof ZonedDateTime) {
            arg = new Timestamp(((ZonedDateTime) arg).toInstant().toEpochMilli());
        } else if (arg instanceof Enum) {
            return arg.toString();
        }
        return arg;
    }

    /**
     * Add a model to the internal registry of schemas.
     *
     * @param cls
     * @return
     */
    public Schema addModel(Class cls) {
        Schema schema = modelToSchema(cls);
        this.addTable(schema);
        classToSchema.put(schema.getClazz().getName(), schema);
        return schema;
    }

    /**
     * Add a dynamicModelDefinition to the internal registry of schemas
     *
     * @param def
     * @return
     */
    public Schema addDynamicModelDefinition(DynamicModelDefinition def) {
        Schema schema = new Schema(def.getTable(), def.getModelClass());
        schema.setColumns(def.getColumns());
        this.addTable(schema);
        classToSchema.put(def.getTable(), schema);
        return schema;
    }



    Schema getSchemaForModelClass(Class cls) {
        if (BaseJavascriptModel.class.isAssignableFrom(cls)) {
            String bucketName = null;
            try {
                bucketName = ((BaseJavascriptModel) cls.newInstance()).getBucketName();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return classToSchema.getOrDefault(bucketName, null);
        } else {
            return classToSchema.getOrDefault(cls.getCanonicalName(), null);
        }
    }

    public Schema modelToSchema(Class cls) {
        try {
            Field metaField = cls.getField("meta");
            if (metaField != null) {
                Map<String, Object> o = (Map<String, Object>)metaField.get(null);
                if (o != null && !empty(o.getOrDefault("tableName", "").toString())) {
                    return metaDataModelToSchema(cls, o);
                }
            }
        } catch(NoSuchFieldException e) {
            Log.exception(e, "Error finding meta field");
        } catch(IllegalAccessException e) {
            Log.exception(e, "Error accesing meta field");
        }
        return annotatedModelToSchema(cls);
    }

    private Schema annotatedModelToSchema(Class cls) {
        Table table = (Table)cls.getDeclaredAnnotation(Table.class);
        if (table == null) {
            throw new UsageException("Trying to register model with database, but it has no @Table annotation: " + cls.getCanonicalName());
        }
        if (empty(table.name() )) {
            throw new UsageException("Trying to register model with database, but it has no name for the @Table annotation: " + cls.getCanonicalName());
        }
        Schema schema = new Schema(table.name(), cls);
        Object inst = null;
        try {
            inst = cls.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        ExtraKeyDefinitions keyDefinitions = (ExtraKeyDefinitions)cls.getDeclaredAnnotation(ExtraKeyDefinitions.class);
        if (keyDefinitions != null) {
            for(String def: keyDefinitions.value()) {
                schema.getExtraKeyDefinitions().add(def);
            }
        }

        for(Method method: cls.getMethods()) {
            if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) {
                continue;
            }
            // If a property starts with "is", the type must be boolean, or else we continue;
            if (method.getName().startsWith("is") &&
                    (!method.getReturnType().isAssignableFrom(boolean.class)) && !method.getReturnType().isAssignableFrom(Boolean.class)) {
                continue;
            }

            String propertyName = "";
            if (method.getName().startsWith("is")) {
                propertyName = method.getName().substring(2);
            } else {
                propertyName = method.getName().substring(3);
            }
            if (empty(propertyName)) {
                continue;
            }
            propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
            Column columnAnno = method.getAnnotation(Column.class);
            if (columnAnno == null) {
                continue;
            }

            String columnName = propertyName.toLowerCase();
            if (!StringUtils.isBlank(columnAnno.name())) {
                columnName = columnAnno.name();
            }
            Col col = new Col();
            col.setPropertyName(propertyName);
            col.setName(columnName);
            col.setUpdateable(columnAnno.updatable());
            col.setjType(method.getReturnType());
            col.setInsertable(columnAnno.insertable());

            col.setDbType(columnAnno.columnDefinition());
            col.setNullable(columnAnno.nullable());
            col.setLength(columnAnno.length());
            // If the column cannot be null, we need a default value
            if (!columnAnno.nullable()) {
                col.setDefaultValue(PropertyUtils.getProperty(inst, propertyName));
            }

            Converter converterAnno = method.getDeclaredAnnotation(Converter.class);
            Log.finest("Adding schema Column {0}", columnName);
            if (converterAnno != null) {
                Log.finest("ConverterAnno {0} {1} {2} ", columnName, converterAnno, converterAnno.name());
                if (empty(converterAnno.name())) {
                    col.setConverterClassName(converterAnno.cls().getCanonicalName());
                } else {
                    col.setConverterClassName(converterAnno.name());
                }
            }
            if (method.getAnnotation(AlternativeKey.class) != null) {
                col.setAlternativeKey(true);
                schema.getKeyNames().add(col.getName());
            }
            if (method.getAnnotation(UniqueKey.class) != null) {
                col.setUniqueKey(true);
                schema.getKeyNames().add(col.getName());
            }
            if (columnAnno.unique()) {
                col.setUniqueKey(true);
                schema.getKeyNames().add(col.getName());
            }

            schema.getColumns().add(col);

        }
        return schema;
    }

    public List<Schema> getSchemas() {
        return new ArrayList<>(classToSchema.values());
    }

    private Schema metaDataModelToSchema(Class cls, Map<String, Object> meta) {
        Schema schema = new Schema(meta.get("tableName").toString(), cls);
        for(Map colData: (List<Map>)meta.getOrDefault("columns", list())) {
            Col col = new Col();
            col.setPropertyName(colData.getOrDefault("propertyName", "").toString());
            col.setName(colData.getOrDefault("name", "").toString());
            col.setUpdateable((boolean) colData.getOrDefault("updateable", true));
            col.setInsertable((boolean) colData.getOrDefault("insertable", true));

            schema.getColumns().add(col);
        }

        return schema;
    }

    /**
     * Execute an arbitrary SQL command, return the affected rows.
     *
     * @param sql
     * @param params
     * @return
     */
    public int execute(String sql, Object...params) {
        try {
            return newQuery().update(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    DB addTable(Schema schema) {
        tables.add(schema);
        return this;
    }

    /**
     * Get or instantiate an AttributeConverter instance based on the
     * converter className
     *
     * @param className
     * @return
     */
    public AttributeConverter getConverter(String className) {
        if (converters.containsKey(className)) {
            return converters.get(className);
        }
        Class cls = StallionClassLoader.loadClass(className);
        if (cls == null) {
            throw new UsageException("Could not find converter class: " + className);
        }
        try {
            AttributeConverter converter = (AttributeConverter)cls.newInstance();
            converters.put(className, converter);
            return converter;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean tableExists(String tableName) {
        if (tableName.contains("`") || tableName.contains("'")) {
            return false;
        }
        List<Map<String, Object>> tables = DB.instance().findRecords("SHOW TABLES LIKE '" + tableName + "'");
        return tables.size() > 0;
    }

    public boolean columnExists(String tableName, String column) {
        if (!tableExists(tableName)) {
            return false;
        }
        if (tableName.contains("`")) {
            return false;
        }
        List<Map<String, Object>> columns = DB.instance().findRecords("SHOW COLUMNS FROM `" + tableName + "` WHERE field=?", column);
        return columns.size() > 0;
    }

    /**
     * Get the table schema for a given model class
     *
     * @param cls
     * @return
     */
    public Schema getSchema(Class cls) {
        return classToSchema.get(cls.getName());
    }

    /**
     * Get the table schema for a model name
     *
     * @param name
     * @return
     */
    public Schema getSchema(String name) {
        return classToSchema.get(name);
    }


    /**
     * Get the underlying database connection pool
     *
     * @return
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * Get the Tickets service for generating new IDS
     * @return
     */
    public Tickets getTickets() {
        return tickets;
    }


    public DbImplementation getDbImplementation() {
        return dbImplementation;
    }


    /**
     * If true, internally, DalRegistry.register() will never use a DbPersister, instead
     * it will use a DummyPersister. This flag is set to true if we are running the command
     * sql-generate, in which case we need to register all models, but the models will fail
     * because the table does not actually exist. So by using a DummyPersister, we avoid
     * hard failures, allowing model registry to execute, then we can get a list of all registered
     * models and generate the correct SQL.
     *
     * @return
     */
    public static boolean isUseDummyPersisterForSqlGenerationMode() {
        return useDummyPersisterForSqlGenerationMode;
    }

    public static void setUseDummyPersisterForSqlGenerationMode(boolean useDummyPersisterForSqlGenerationMode) {
        DB.useDummyPersisterForSqlGenerationMode = useDummyPersisterForSqlGenerationMode;
    }
}
