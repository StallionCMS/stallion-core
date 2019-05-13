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

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.stallion.utils.Literals.*;

import com.amazonaws.util.CollectionUtils;
import io.stallion.StallionApplication;
import io.stallion.boot.ActionModeFlags;
import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.ModeFlags;
import io.stallion.boot.StallionRunAction;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.exceptions.ConfigException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;


public class SqlObsoleteAction implements StallionRunAction<CommandOptionsBase> {
    @Override
    public String getActionName() {
        return "sql-obsolete";
    }

    @Override
    public String getHelp() {
        return "Returns sql tables and columns that exist in the database but not in the Java data models";
    }

    @Override
    public ActionModeFlags[] getActionModeFlags() {
        return array(ActionModeFlags.NO_JOB_AND_TASK_EXECUTION, ActionModeFlags.NO_JERSEY, ActionModeFlags.NO_PRELOADED_DATA);
    }

    /*
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
    */

    @Override
    public void execute(CommandOptionsBase options) throws Exception {
        List<String> tableNames = DB.instance().queryColumn("SHOW tables");
        List<String> notFound = list();
        for(String tableName: tableNames) {
            if (DB.instance().tableNameToSchema(tableName) == null) {
                notFound.add(tableName);
            }
        }
        Log.info("Tables without matching schema: \n\n" + CollectionUtils.join(notFound, "\n") + "\n\n");
        for(Schema schema: DB.instance().getSchemas()) {
            Log.fine("Schema: " + schema.getName());
            Set<String> modelColumnNames = set();
            if (schema.getName().startsWith("stallion_")) {
                continue;
            }
            for (Col col: schema.getColumns()) {
                modelColumnNames.add(col.getName().toLowerCase());
            }
            List<Map<String, Object>> columnData = DB.instance().findRecords("SHOW COLUMNS FROM " + schema.getName());
            List<String> extraColumns = list();
            for(Map<String, Object> sqlCol: columnData) {
                if ("row_updated_at".equals(sqlCol.get("field"))) {
                    continue;
                }
                if ("id".equals(sqlCol.get("field"))) {
                    continue;
                }
                if (!modelColumnNames.contains(sqlCol.get("field").toString().toLowerCase())) {
                    extraColumns.add(sqlCol.get("field").toString());
                }
            }
            Log.info("Columns without matching bean property for table: " + schema.getName() + "  \n\n" + CollectionUtils.join(extraColumns, ",\nDROP ") + "\n\n");
        }
    }
}
