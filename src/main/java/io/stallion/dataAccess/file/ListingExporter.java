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

package io.stallion.dataAccess.file;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.Displayable;
import io.stallion.dataAccess.DisplayableModelController;
import io.stallion.services.Log;
import io.stallion.settings.ContentFolder;
import io.stallion.settings.Settings;
import io.stallion.tools.Exporter;
import io.stallion.tools.ExporterRegistry;
import io.stallion.utils.DateUtils;


public class ListingExporter implements Exporter {
    public static void register() {
        ExporterRegistry.instance().register(new ListingExporter());
    }
    @Override
    public List<String> listUrls() {
        List<String> urlPaths = list();
        for (ContentFolder conf: Settings.instance().getFolders()) {
            if (!conf.isListingEnabled()) {
                continue;
            }
            String root = conf.getListingRootUrl();
            if (root.equals("/")) {
                root = "";
            }
            urlPaths.add(or(root, "/"));
            urlPaths.add(root + "/rss.xml");
            urlPaths.add(root + "/feed");
            DisplayableModelController<TextItem> controller = (DisplayableModelController)DataAccessRegistry.instance().get(conf.getPath());
            int count = controller.filter("published", true).count();
            for (TextItem post: controller.filter("published", true).all()) {
                urlPaths.add(root + "/archives/" + DateUtils.formatLocalDateFromZonedDate(post.getPublishDate(), "yyyy/MM/"));
                for (String tag: post.getTags()) {
                    urlPaths.add("/by-tag/" + tag + "/");
                }
            }
            int pageCount = (count / conf.getItemsPerPage()) + 1;
            for (int i = 2; i <= pageCount; i++) {
                urlPaths.add("/page/" + i + "/");
            }
        }
        return urlPaths;
    }
}




