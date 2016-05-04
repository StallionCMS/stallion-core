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

package io.stallion.tests.integration.filtering;

import io.stallion.dal.DalRegistry;
import io.stallion.dal.base.DalRegistration;
import io.stallion.dal.base.DummyPersister;
import io.stallion.dal.base.LocalMemoryStash;
import io.stallion.dal.base.StandardModelController;
import io.stallion.services.Log;
import io.stallion.settings.Settings;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class BooksController extends StandardModelController<Book> {
    public static BooksController instance() {
        return (BooksController)DalRegistry.instance().get("books");
    }
    public static void register() {
        DalRegistry.instance().registerDal(
                new DalRegistration()
                        .setBucket("books")
                        .setModelClass(Book.class)
                        .setControllerClass(BooksController.class)
                        .setStashClass(LocalMemoryStash.class)
                        .setPersisterClass(DummyPersister.class)
                        .build(Settings.instance().getTargetFolder())
        );
    }
    static final String[] allCategories = {"mystery", "history", "scifi", "fantasy", "thriller", "literary", "young adult", "romance", "comedy", "drama"};
    static final String[] titleParts = {"Time", "Mystery", "Bitter", "Fair", "Jolly", "Light", "Broadside", "Square"};
    static final String[] authors = {"Mark Twain", "Charles Dickens", "Jane Austen", "William Shakespeare", "James Joyce", "Emily Dickinson"};

    public void populate() {
        for(int x = 0; x<1000; x++) {
            String author = authors[x % 6];
            String title = titleParts[x % 8] + " " + titleParts[(x * x + x + x) % 8 ] + " " + titleParts[(x * x * x + x) % 8 ];
            List<String> categories = list(allCategories[x % 10], allCategories[((x+1) % 5)] );
            Book book = new Book()
                    .setPublishDate(ZonedDateTime.of(1850 + (x % 100), 1 + (x % 12), 1 + (x % 28), 12, 0, 0, 0, ZoneId.of("UTC")))
                    .setPublisherId(x % 10L)
                    .setTitle(title)
                    .setCategories(categories)
                    .setPublished(x % 11 != 0)
                    .setAuthor(author)
                    .setId(new Long(x));

            save(book);

        }

    }
}
