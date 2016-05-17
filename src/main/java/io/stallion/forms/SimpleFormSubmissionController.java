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

package io.stallion.forms;

import io.stallion.Context;
import io.stallion.dataAccess.DataAccessRegistration;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.DbPersister;
import io.stallion.dataAccess.file.JsonFilePersister;
import io.stallion.settings.Settings;

import java.io.File;
import java.util.UUID;


public class SimpleFormSubmissionController extends StandardModelController<SimpleFormSubmission> {

    public static SimpleFormSubmissionController instance() {
        return (SimpleFormSubmissionController) Context.dal().get("simple_form_submissions");
    }

    public static void register() {
        DataAccessRegistration registration = new DataAccessRegistration()
                .setControllerClass(SimpleFormSubmissionController.class)
                .setModelClass(SimpleFormSubmission.class);
        if (DB.instance() == null) {
            registration
                    .setPersisterClass(JsonFilePersister.class)
                    .setPath("simple_form_submissions")
                    .setNameSpace("")
                    .setWritable(true)
                    .setUseDataFolder(true)
                    .setShouldWatch(true);
            registration.hydratePaths(Settings.instance().getTargetFolder());
            File dir = new File(registration.getAbsolutePath());
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
        } else {
            registration
                    .setTableName("simple_form_submissions")
                    .setBucket("simple_form_submissions")
                    .setPersisterClass(DbPersister.class);
        }
        Context.dal().register(registration);
        Context.dal().register(registration);
    }


    public Object generateId(SimpleFormSubmission obj) {
        return UUID.randomUUID().toString();
    }
}
