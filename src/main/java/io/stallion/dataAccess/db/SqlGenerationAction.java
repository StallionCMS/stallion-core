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

import io.stallion.StallionApplication;
import io.stallion.boot.ActionModeFlags;
import io.stallion.boot.ModeFlags;
import io.stallion.boot.StallionRunAction;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.Prompter;
import org.apache.commons.lang3.StringUtils;
import org.parboiled.common.FileUtils;

import javax.script.ScriptEngine;
import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;


public class SqlGenerationAction  implements StallionRunAction<SqlGenerateCommandOptions> {
    private ScriptEngine scriptEngine;

    @Override
    public String getActionName() {
        return "sql-generate";
    }

    @Override
    public String getHelp() {
        return "Generates new SQL scripts from the models in the project.";
    }


    @Override
    public SqlGenerateCommandOptions newCommandOptions() {
        return new SqlGenerateCommandOptions();
    }

    @Override
    public ActionModeFlags[] getActionModeFlags() {
        return array(ActionModeFlags.NO_JOB_AND_TASK_EXECUTION, ActionModeFlags.NO_JERSEY, ActionModeFlags.NO_PRELOADED_DATA);
    }

    @Override
    public void initializeRegistriesAndServices(StallionApplication app, ModeFlags flags) {

        DB.setUseDummyPersisterForSqlGenerationMode(true);

        app.initializeRegistriesAndServices(flags.isTest());

        if (!DB.available()) {
            if (Settings.instance().getDatabase() == null || empty(Settings.instance().getDatabase().getUrl())) {
                throw new ConfigException("No database url defined in your settings");
            } else {
                throw new ConfigException("Database is not available.");
            }
        }
    }



    @Override
    public void execute(SqlGenerateCommandOptions options) throws Exception {
        //List<ClassLoader> loaders = list();
        //ConfigurationBuilder builder = new ConfigurationBuilder();
        //if (!empty(options.getPackageName())) {
        //    builder = builder.forPackages("io.stallion");
        //} else {
        //    builder = builder.forPackages(options.getPackageName());
        //}
        //for(StallionJavaPlugin plugin: PluginRegistry.instance().getJavaPluginByName().values()) {
        //    builder = builder.addClassLoader(plugin.getClass().getClassLoader());
        //}

        //Reflections reflections = new Reflections(builder);

        //Reflections reflections = new Reflections("org.mycabal");

        //Set<Class<? extends ModelBase>> classes = reflections.getSubTypesOf(ModelBase.class);
        //Log.info("Model Count {0} {1}", classes.size(), classes);
        //classes = set(User.class);
        List<SqlMigration> unRunMigrations = new SqlMigrationAction().findNotRunMigrations();
        if (unRunMigrations.size() > 0) {
            String unRunStr = StringUtils.join(apply(unRunMigrations, mig->mig.getFilename()), "\n");
            throw new UsageException("\n\nYou have migrations that have not yet been executed: \n\n" + unRunStr + "\n\n Before generating new migrations based " +
                    "on your models, you should run existing migrations to bring the database up to date. Run the 'sql-migrate' action in " +
                    "order to execute all outstanding migrations.\n\n");
        }
        boolean hasNewMigrations = false;
       // for (Class cls: classes) {
        List<Schema> schemas = DB.instance().getSchemas();
        Log.info("Schema count {0}", DB.instance().getSchemas().size());
        Integer lastMigrationNumber = getLastMigrationNumber();
        for (Schema schema: schemas)   {
            Log.info("Try generate for schema ", schema.getName());
            //GenerateResult result = generateForModel(cls);
            GenerateResult result = generateForSchema(schema);
            if (result == null) {
                Log.info("No change needed for {0}", schema.getName());
                continue;
            }
            Log.info("SQL GENERATED\n{0}", result.getSql());
            boolean shouldWrite = new Prompter("Write this script to file? ").yesNo();
            if (shouldWrite) {
                lastMigrationNumber += 10;
                writeMigrationToFile(lastMigrationNumber, result);
                hasNewMigrations = true;
            }
        }

    }

    public void writeMigrationToFile(Integer migrationNumber, GenerateResult result) {
        String prefix = StringUtils.leftPad(migrationNumber.toString(), 5, '0');

        File dir = new File(System.getProperty("user.dir") + "/src/main/resources/sql");
        File migrationsFile = new File(System.getProperty("user.dir") + "/src/main/resources/sql/migrations.txt");;
        if (dir.exists() && migrationsFile.exists()) {
            String name = prefix + "-" + result.getChangePrefix()
                    + ".mysql.js";
            File newMigrationFile = new File(dir.getAbsolutePath() + "/" + name);

            FileUtils.writeAllText(result.getSqlJs(), newMigrationFile, Charset.forName("UTF-8"));

            // Append the new migration to the text file with a list of migrations
            String s = FileUtils.readAllText(migrationsFile);
            if (!s.endsWith("\n")) {
                s += "\n";
            }
            s += prefix + "-" + result.getChangePrefix();
            FileUtils.writeAllText(s, migrationsFile, Charset.forName("UTF-8"));

        } else {
            String file = Settings.instance().getTargetFolder() + "/sql/" + prefix + "-" + result.getChangePrefix()
                    + ".mysql.js";
            FileUtils.writeAllText(result.getSqlJs(), new File(file), Charset.forName("UTF-8"));
        }

    }


    public int getLastMigrationNumber() {
        Integer max = 0;
        File migrationsFile = new File(System.getProperty("user.dir") + "/src/main/resources/sql/migrations.txt");
        if (migrationsFile.exists()) {
            for(String line: FileUtils.readAllText(migrationsFile, UTF8).split("\\n")) {
                line = StringUtils.strip(line);
                if (empty(line)) {
                    continue;
                }
                if (line.startsWith("//") || line.startsWith("#")) {
                    continue;
                }
                int dash = line.indexOf("-");
                if (dash == -1) {
                    continue;
                }
                Integer version = Integer.valueOf(StringUtils.stripStart(line.substring(0, dash), "0"));
                if (version >= max) {
                    max = version;
                }
            }

        } else {
            for (SqlMigration migration : new SqlMigrationAction().getUserMigrations()) {
                if (migration.getVersionNumber() > max) {
                    max = migration.getVersionNumber();
                }
            }
        }
        return max;
    }

    public GenerateResult generateForModel(Class cls) {
        Schema schema = DB.instance().modelToSchema(cls);
        return generateForSchema(schema);
    }

    public GenerateResult generateForSchema(Schema schema) {
        if (!DB.instance().tableExists(schema.getName())) {
            return createTableSqlForSchema(schema);
        } else {
            List<Col> missingColumns = list();
            for(Col col: schema.getColumns()) {
                if (col.getVirtual()) {
                    continue;
                }
                if (!DB.instance().columnExists(schema.getName(), col.getName())) {
                    missingColumns.add(col);
                }
            }
            if (missingColumns.size() > 0) {
                return createAlterTableForNewColumns(schema, missingColumns);
            }
        }
        return null;
    }

    public GenerateResult createTableSqlForSchema(Schema schema) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `" + schema.getName() + "` (\n");
        sql.append("`id` bigint(20) unsigned NOT NULL,\n");
        for (Col col: schema.getColumns()) {
            sql.append("    `" + col.getName() + "` ");
            sql.append(" " + dbTypeForColumn(col) + " ");
            if (col.getNullable()) {
                sql.append(" NULL ");
            } else {
                sql.append(" NOT NULL ");
            }
            if (col.getDefaultValue() != null) {
                Object defaultVal = col.getDefaultValue();
                if (defaultVal instanceof Boolean) {
                    if ((Boolean)defaultVal == true){
                        defaultVal = 1;
                    } else {
                        defaultVal = 0;
                    }
                } else {
                    defaultVal = "'" + defaultVal.toString().replace("'", "\'") + "'";
                }
                sql.append(" DEFAULT " + defaultVal + " ");
            }
            sql.append(",\n");
        }
        sql.append("  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n");
        sql.append("  PRIMARY KEY (`id`),\n");
        sql.append("  KEY `row_updated_at_key` (`row_updated_at`),\n");

        for(Col col: schema.getColumns()) {
            if (col.getUniqueKey()) {
                sql.append("  UNIQUE KEY `" + col.getName() + "_key` (`" + col.getName() + "`),\n");

            }
            if (col.getAlternativeKey()) {
                sql.append("  KEY `" + col.getName() + "_key` (`" + col.getName() + "`),\n");
            }
        }
        for(String def: schema.getExtraKeyDefinitions()) {
            sql.append("    " + def + ",\n");
        }
        String sqlString = sql.toString();
        sqlString = StringUtils.strip(StringUtils.strip(sqlString, "\n"), ",") + "\n";
        sqlString += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        return new GenerateResult()
                .setChangePrefix("create-table-" + schema.getName())
                .setSql(sqlString)
                .setSqlJs("db.execute('''\n" + sqlString + " \n''');")
                .setTableName(schema.getName())
                ;
    }

    public GenerateResult createAlterTableForNewColumns(Schema schema, List<Col> columns) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE `" + schema.getName() + "` ");

        int i = 0;
        for (Col col: columns) {
            sql.append("\n    ADD COLUMN `" + col.getName() + "` ");
            String typeForColumn = dbTypeForColumn(col);
            sql.append(" " + typeForColumn + " ");
            if (typeForColumn.contains("text")) {

            } else if (col.getNullable()) {
                sql.append(" NULL");
            } else {
                sql.append(" NOT NULL");
            }
            if (col.getDefaultValue() != null) {
                Object defaultValue = col.getDefaultValue();
                if (typeForColumn.contains("text")) {

                } else if (col.getDefaultValue().equals(true)) {
                    sql.append(" DEFAULT 1 ");
                } else if (defaultValue.equals(false)) {
                    sql.append(" DEFAULT 0 ");
                } else {
                    sql.append(" DEFAULT '" + defaultValue.toString().replace("'", "\'") + "'");
                }
            }
            i++;
            if (i != columns.size()) {
                sql.append(",");
            }
        }
        sql.append(";");
        String sqlString = sql.toString();

        GenerateResult generateResult = new GenerateResult()
                .setChangePrefix(GeneralUtils.slugify("alter-" + schema.getName() + "-add-" + columns.size()))
                .setSql(sqlString)
                .setSqlJs("db.execute('''\n" + sqlString + "\n''');")
                .setTableName(schema.getName())
                ;
        return generateResult;
    }

    protected String dbTypeForColumn(Col column) {
        if (!empty(column.getDbType())) {
            String dbType = column.getDbType();
            if (column.getLength() > 0 && !dbType.contains("(") && !dbType.contains("text") && !dbType.contains("(")) {
                dbType = dbType + "(" + column.getLength() + ")";
            }
            return dbType;
        }
        if (column.getjType() == String.class) {
            int length = column.getLength();
            if (length < 1) {
                length = 250;
            }
            return "varchar(" + length + ")";
        } else if (column.getjType() == Boolean.class) {
            return "bit(1)";
        } else if (column.getjType() == boolean.class) {
            return "bit(1)";
        } else if (column.getjType() == Long.class) {
            return "bigint(20)";
        } else if (column.getjType() == long.class) {
            return "bigint(20)";
        } else if (column.getjType() == Integer.class) {
            return "int";
        } else if (column.getjType() == int.class) {
            return "int";
        } else if (column.getjType() == float.class) {
            return "float";
        } else if (column.getjType() == Float.class) {
            return "float";
        } else if (column.getjType() == double.class) {
            return "double";
        } else if (column.getjType() == Double.class) {
            return "double";
        } else if (column.getjType() == ZonedDateTime.class) {
            return "datetime";
        } else if (column.getjType() == LocalDateTime.class) {
            return "datetime";
        } else if (column.getjType() == LocalDate.class) {
            return "date";
        } else if (List.class.isAssignableFrom(column.getjType())) {
            return "longtext";
        } else if (Map.class.isAssignableFrom(column.getjType())) {
            return "longtext";
        } else if (Enum.class.isAssignableFrom(column.getjType())) {
            return "varchar(30)";
        } else {
            throw new UsageException("Could not guess database column type for column " + column.getName());
        }
    }

    public static class GenerateResult {
        private String sql = "";
        private String sqlJs = "";
        private String tableName = "";
        private String changePrefix = "";

        public String getSql() {
            return sql;
        }

        public GenerateResult setSql(String sql) {
            this.sql = sql;
            return this;
        }

        public String getSqlJs() {
            return sqlJs;
        }

        public GenerateResult setSqlJs(String sqlJs) {
            this.sqlJs = sqlJs;
            return this;
        }

        public String getTableName() {
            return tableName;
        }

        public GenerateResult setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public String getChangePrefix() {
            return changePrefix;
        }

        public GenerateResult setChangePrefix(String changePrefix) {
            this.changePrefix = changePrefix;
            return this;
        }
    }

}
