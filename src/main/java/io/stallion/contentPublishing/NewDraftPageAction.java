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

import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.StallionRunAction;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.file.TextFilePersister;
import io.stallion.exceptions.CommandException;
import io.stallion.exceptions.UsageException;
import io.stallion.settings.ContentFolder;
import io.stallion.settings.Settings;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.Prompter;
import io.stallion.utils.SimpleTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

import static io.stallion.utils.Literals.empty;


public class NewDraftPageAction implements StallionRunAction<CommandOptionsBase> {
    @Override
    public String getActionName() {
        return "new-draft-page";
    }

    @Override
    public String getHelp() {
        return "Create a new draft, markdown page or blog post.";
    }

    public void execute(CommandOptionsBase options) throws Exception {
        int x = 1;
        ContentFolder cf = null;
        if (Settings.instance().getFolders().size() == 0) {
            throw new UsageException("Your site does not have any content folders defined.");
        } else if (Settings.instance().getFolders().size() == 1) {
            cf = Settings.instance().getFolders().get(0);
        }
        if (cf == null) {
            for (ContentFolder folder : Settings.instance().getFolders()) {
                System.out.println(x + ") " + folder.getBucket());
                x++;
            }
            String folder = Prompter.prompt("Choose a folder from the list above: ").trim();

            if (StringUtils.isNumeric(folder)) {
                Integer num = Integer.parseInt(folder) - 1;
                if (num >= 0 && num < Settings.instance().getFolders().size()) {
                    cf = Settings.instance().getFolders().get(num);
                }
            }
            if (cf == null) {
                for (ContentFolder afolder : Settings.instance().getFolders()) {
                    if (folder.equals(afolder.getBucket())) {
                        cf = afolder;
                    }
                }
            }
            if (cf == null) {
                throw new CommandException("Could not find folder: " + folder);
            }
        }


        String title = Prompter.prompt("Choose a post title: ");
        String slug = "";
        if (!empty(cf.getListingRootUrl())) {
            slug = cf.getListingRootUrl();
        }
        if (!slug.endsWith("/")) {
            slug += "/";
        }
        slug += GeneralUtils.slugify(title);

        String postContent = new SimpleTemplate(postTemplate)
                .put("publishDate", "2099-01-01 11:15:00 America/New_York")
                .put("id", DataAccessRegistry.instance().getTickets().nextId())
                .put("slug", slug)
                .put("title", title)
                .put("siteUrl", Settings.instance().getSiteUrl())
                .put("previewKey", GeneralUtils.randomToken(8))
                .render();
        String blogFolderPath = ((TextFilePersister)DataAccessRegistry.instance().get(cf.getBucket()).getPersister()).getBucketFolderPath();
        String fileName =  blogFolderPath + "/" + DateUtils.formatNow("yyyy-MM-dd") + "-" + GeneralUtils.slugify(title) + ".txt";
        File file = new File(fileName);
        FileUtils.write(file, postContent, "UTF-8");
        System.out.println("Successfully wrote new blog post to file: " + file.getAbsolutePath());
    }

    private static final String postTemplate = "{ title }\n=====================================\n" +
            "publishDate: { publishDate }\n" +
            "slug: { slug }\n" +
            "id: { id }\n" +
            "author: \n" +
            "previewKey: {previewKey}\n" +
            "metaDescription: \n" +
            "\n" +
            "Hello, I am a brand new blog post. I can be previewed at [{siteUrl}{slug}?stPreview={previewKey}]({siteUrl}{slug}?stPreview={previewKey})\n\n" +
            "When you are ready to publish me, change the publishDate to a near future or past date.";


}
