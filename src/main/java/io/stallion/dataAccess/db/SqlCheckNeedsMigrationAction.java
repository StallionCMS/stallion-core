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

package io.stallion.dataAccess.db;

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.StallionRunAction;
import io.stallion.boot.SqlMigrateCommandOptions;
import io.stallion.services.Log;

import javax.script.ScriptEngine;
import java.util.List;

import static io.stallion.utils.Literals.*;

/**
 * A command-line action that checks to see if a database migration needs to be run, that is
 * if there is a SQL migration file in the sql folder that has not yet been marked as run in
 * the database.
 * This is used by the publish/deployment action, if a migration has not been run, deployment
 * should be aborted.
 */
public class SqlCheckNeedsMigrationAction implements StallionRunAction<SqlMigrateCommandOptions> {
    private ScriptEngine scriptEngine;

    @Override
    public String getActionName() {
        return "sql-check-migrations";
    }

    @Override
    public String getHelp() {
        return "Checks to see if a SQL migration needs to be run.";
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
        if (!DB.available()) {
            // No db available, no migrations to run
            Log.info("No DB available, no migrations to run.");
            System.out.println("result:success");
            return;
        }
        SqlMigrationAction migrationAction = new SqlMigrationAction();
        migrationAction.createMigrationTrackingTableIfNotExists();
        // Get a ticket to make sure the tickets table is operational
        List<SqlMigration> migrations = migrationAction.getDefaultMigrations();
        migrations.addAll(migrationAction.getUserMigrations());
        DB db = DB.instance();
        List<SqlMigration> unrunMigrations = list();
        for (SqlMigration migration: migrations) {
            Long currentVersion = or(db.queryScalar("SELECT MAX(versionNumber) FROM stallion_sql_migrations WHERE appName=?", migration.getAppName()), 0L);
            if (currentVersion >= migration.getVersionNumber()) {
                Log.finer("File {0} is below current version of {1} for app {2}", migration.getFilename(), currentVersion, migration.getAppName());
                continue;
            }
            Log.warn("Un-executed migration found: {0}", migration.getFilename());
            unrunMigrations.add(migration);
        }
        if (unrunMigrations.size() > 0) {

            Log.warn("Un-executed migrations found. Exiting with error code.");
            System.out.println("result:failure");
            System.exit(1);
        } else {
            Log.info("No un-executed migrations found.");
            System.out.println("result:success");
        }


    }

}
