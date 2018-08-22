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

package io.stallion.contentPublishing;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.users.IUser;

import java.util.Map;

import static io.stallion.utils.Literals.*;


public class UploadedFileController<T extends UploadedFile> extends StandardModelController<T> {
    public static UploadedFileController instance() {
        return (UploadedFileController) DataAccessRegistry.instance().get("uploaded_files");
    }
    public static void register() {
        DataAccessRegistry.instance().registerDbOrFileModel(UploadedFile.class, UploadedFileController.class, "uploaded_files");
    }

    public static String getTypeForExtension(String ext) {
        return extensionToFileType.getOrDefault(ext.toLowerCase(), "other");
    }

    public static Map<String, String> extensionToFileType = map(
            val("png", "image"),
            val("svg", "image"),
            val("jpg", "image"),
            val("gif", "image"),
            val("doc", "document"),
            val("pdf", "document"),
            val("docx", "document"),
            val("xls", "document"),
            val("xlsx", "document"),
            val("numbers", "document")
    );

    public boolean fileViewable(IUser user, T file) {
        if (file.isPubliclyViewable()) {
            return true;
        }
        if (user != null && !empty(user.getId()) && user.getId().equals(file.getOwnerId())) {
            return true;
        }
        return false;
    }
}
