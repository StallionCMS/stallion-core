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

import io.stallion.contentPublishing.forms.SimpleFormEndpoints;
import io.stallion.contentPublishing.forms.SimpleFormTag;
import io.stallion.monitoring.InternalEndpoints;
import io.stallion.settings.Settings;
import io.stallion.templating.TemplateRenderer;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;

import static io.stallion.utils.Literals.empty;


public class ContentPublishingBooter {

    public static void boot(ResourceConfig rc) {
        {
            rc.register(InternalEndpoints.class);
        }

        if (Settings.instance().getUserUploads() != null && Settings.instance().getUserUploads().getEnabled()) {
            UploadedFileController.register();

            rc.register(UploadedFileEndpoints.class);

        }

        if (!empty(Settings.instance().getFolders()) || new File(Settings.instance().getTargetFolder() + "/pages").exists()) {
            rc.register(ContentSlugCatchallResource.class);

            rc.register(SiteMapEndpoints.class);
            ListingEndpoints.register(rc);
        }

        {
            rc.register(SimpleFormEndpoints.class);
            TemplateRenderer.instance().getJinjaTemplating().registerTag(new SimpleFormTag());
        }



    }
}
