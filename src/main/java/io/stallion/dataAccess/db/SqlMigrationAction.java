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

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.StallionRunAction;
import io.stallion.boot.SqlMigrateCommandOptions;
import io.stallion.exceptions.ConfigException;
import io.stallion.services.Log;
import io.stallion.utils.ResourceHelpers;
import io.stallion.utils.json.JSON;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.parboiled.common.FileUtils;

import javax.script.ScriptEngine;
import java.io.File;
import java.util.List;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * Runs all unexecuted database migrations.
 */
public class SqlMigrationAction  implements StallionRunAction<SqlMigrateCommandOptions> {
    private ScriptEngine scriptEngine;

    @Override
    public String getActionName() {
        return "sql-migrate";
    }

    @Override
    public String getHelp() {
        return "Executes all SQL scripts in the sql folder that are above the current migration number";
    }


    @Override
    public SqlMigrateCommandOptions newCommandOptions() {
        return new SqlMigrateCommandOptions();
    }

    @Override
    public void loadApp(SqlMigrateCommandOptions options) {
        AppContextLoader.loadWithSettingsOnly(options);
        DB.load();
    }

    @Override
    public void execute(SqlMigrateCommandOptions options) throws Exception {
        createMigrationTrackingTableIfNotExists();
        // Get a ticket to make sure the tickets table is operational
        Long nonce = DB.instance().getTickets().nextId();
        List<SqlMigration> migrations = getDefaultMigrations();
        migrations.addAll(getUserMigrations());
        DB db = DB.instance();
        for (SqlMigration migration: migrations) {
            Long currentVersion = or(db.queryScalar("SELECT MAX(versionNumber) FROM stallion_sql_migrations WHERE appName=?", migration.getAppName()), 0L);
            if (currentVersion >= migration.getVersionNumber()) {
                Log.finer("File {0} is below current version of {1} for app {2}", migration.getFilename(), currentVersion, migration.getAppName());
                continue;
            }
            Log.info("Run migration app:" + migration.getAppName() + " file:" + migration.getFilename());
            if (migration.isJavascript()) {
                ScriptEngine engine = getOrCreateScriptEngine();;
                engine.put("db", db);
                engine.eval("load(" + JSON.stringify(map(
                        val("script", transformJavascript(migration.getSource())),
                        val("name", migration.getFilename())
                )) + ");");
            } else {
                db.execute(migration.getSource());

            }
            db.execute("INSERT INTO stallion_sql_migrations (versionNumber, appName, fileName, executedAt) VALUES(?, ?, ?, UTC_TIMESTAMP())",
                    migration.getVersionNumber(), migration.getAppName(), migration.getFilename()
            );
        }


    }

    public String transformJavascript(String source) {
        StringBuilder builder = new StringBuilder();
        int current = 0;
        int end = source.length() - 1;
        for(int i: safeLoop(1000)) {
            int start = source.indexOf("'''", current);
            if (start == -1) {
                break;
            }
            int last = source.indexOf("'''", start + 3);
            if (last == -1) {
                break;
            }
            builder.append(source.substring(current, start));
            String inner = source.substring(start + 3, last);
            builder.append(JSON.stringify(inner));
            current = last + 3;
        }
        builder.append(source.substring(current, end));
        return builder.toString();
    }

    public ScriptEngine getOrCreateScriptEngine() {
        if (scriptEngine == null) {
            scriptEngine = new NashornScriptEngineFactory().getScriptEngine();
        }
        return scriptEngine;
    }

    public List<SqlMigration> getUserMigrations() {
        List<SqlMigration> migrations = list();
        File sqlDirectory = new File(settings().getTargetFolder() + "/sql");
        if (!sqlDirectory.isDirectory()) {
            Log.finer("Sql directory does not exist {0}", sqlDirectory.getAbsoluteFile());
            return migrations;
        }
        Log.finer("Find sql files in {0}", sqlDirectory.getAbsolutePath());
        for (File file: sqlDirectory.listFiles()) {
            Log.finer("Scan file" + file.getAbsolutePath());
            if (!set("js", "sql").contains(FilenameUtils.getExtension(file.getAbsolutePath()))) {
                Log.finer("Extension is not .js or .sql {0}", file.getAbsolutePath());
                continue;
            }
            if (file.getName().startsWith(".") || file.getName().startsWith("#")) {
                Log.finer("File name starts with invalid character {0}", file.getName());
                continue;
            }
            if (!file.getName().contains("." + DB.instance().getDbImplementation().getName().toLowerCase() + ".")) {
                Log.finer("File name does not contain the name of the current database engine: \".{0}.\"", DB.instance().getDbImplementation().getName().toLowerCase());
                continue;
            }
            if (!file.getName().contains("-")) {
                Log.finer("File name does not have version part {0}", file.getName());
                continue;
            }
            String versionString = StringUtils.stripStart(StringUtils.split(file.getName(), "-")[0], "0");
            if (!StringUtils.isNumeric(versionString)) {
                Log.finer("File name does not have numeric version part {0}", file.getName());
                continue;
            }
            Log.info("Load SQL file for migration: {0}", file.getName());

            migrations.add(
                    new SqlMigration()
                    .setVersionNumber(Integer.parseInt(StringUtils.stripStart(versionString, "0")))
                    .setAppName("")
                    .setFilename(file.getName())
                            .setSource(FileUtils.readAllText(file))
            );
        }
        return migrations;
    }

    public List<SqlMigration> getDefaultMigrations() {
        List<SqlMigration> migrations = list();
        List<String> files = list("00004-users", "00006-async_tasks", "00010-job_status", "00011-temp_tokens");
        for (String fileStub: files) {
            int version = Integer.parseInt(fileStub.split("-")[0]);
            String fileName = fileStub + "." + DB.instance().getDbImplementation().getName().toLowerCase() + ".sql";
            String source = ResourceHelpers.loadResource("stallion", "/sql/" + fileName);
            SqlMigration migration = new SqlMigration()
                    .setAppName("stallion")
                    .setFilename(fileName)
                    .setSource(source)
                    .setVersionNumber(version);
            migrations.add(migration);
        }
        return migrations;
    }

    void createMigrationTrackingTableIfNotExists() {
        String sql = ResourceHelpers.loadResource("stallion", "/sql/migrations-table." + DB.instance().getDbImplementation().getName() + ".sql");
        DB.instance().execute(sql);

    }
}
