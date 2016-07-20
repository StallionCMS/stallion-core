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

package io.stallion.tests.integration.filtering;

import io.stallion.dataAccess.file.TextItem;
import io.stallion.dataAccess.file.TextItemController;
import io.stallion.dataAccess.filtering.FilterGroup;
import io.stallion.dataAccess.filtering.FilterOperator;
import io.stallion.dataAccess.filtering.Or;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.utils.DateUtils;
import io.stallion.utils.json.JSON;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;

import static io.stallion.Context.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class FilteringTests extends AppIntegrationCaseBase {


    @BeforeClass
    public static void setUpClass() throws Exception {
        Log.setLogLevel(Level.FINEST);
        startApp("/text_site");
        BooksController.register();
        BooksController.instance().populate();
    }

    @Test
    public void testBasicFilter() {

        assertEquals(91, booksController().filter("published", false).count());
        assertEquals(909, booksController().filter("published", true).count());
        assertEquals(91, booksController().filter("published", 0).count());
        assertEquals(909, booksController().filter("published", 1).count());

        assertEquals(10, booksController().filter("publishDate.year", 1855).count());
        assertEquals(167, booksController().filter("author", "Mark Twain").count());
        assertEquals(33, booksController().filter("author", "Mark Twain").filter("publisherId", 2).count());


    }

    @Test
    public void testAllOperations() {

        ZonedDateTime year1884 = ZonedDateTime.of(1884, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
        assertEquals(900, booksController().filterBy("id", 100L, FilterOperator.GREATER_THAN).count());
        assertEquals(901, booksController().filterBy("id", 100L, FilterOperator.GREATER_THAN_OR_EQUAL).count());
        assertEquals(660, booksController().filterBy("publishDate", year1884, FilterOperator.GREATER_THAN).count());
        assertEquals(660, booksController().filterBy("publishDate", year1884, FilterOperator.GREATER_THAN).count());
        assertEquals(300, booksController().filterBy("categories", "scifi", FilterOperator.IN).count());
        assertEquals(700, booksController().filterChain().excludeBy("categories", "scifi", FilterOperator.IN).count());
        assertEquals(100, booksController().filterBy("categories", "romance", FilterOperator.IN).count());
    }

    @Test
    public void testOrOperations() {
        assertEquals(167 * 2, booksController().anyOf(new Or("author", "Mark Twain"), new Or("author", "Jane Austen")).count());
        assertEquals(666, booksController().filterChain().excludeAnyOf(new Or("author", "Mark Twain"), new Or("author", "Jane Austen")).count());
        assertEquals(67, booksController().anyOf(new Or("author", "Mark Twain"), new Or("author", "Jane Austen")).filter("publisherId", 2).count());
    }

    @Test
    public void testSearchOperations() {
        assertEquals(167, booksController().search("twain", "author", "title").count());
        // Charles Dickens and Emily Dickinson
        assertEquals(333, booksController().search("dick", "author", "title").count());
        // Author Shakespeare or title of Square
        assertEquals(333, booksController().search("are", "author", "title").count());
    }

    @Test
    public void testGrouping() {



        assertEquals(100, booksController().filterChain().groupBy("publishDate.year").size());


        List<FilterGroup<Book>> groups = booksController()
                .filterChain()
                .filter("published", true)
                .sort("publishDate", "desc")
                .groupBy("publishDate.year", "publishDate.month");


        for(FilterGroup<Book> group: groups) {
            Log.info("Group: {0} {1} count={2}",
                    group.getFirst().getTitle(),
                    DateUtils.formatLocalDate(group.getFirst().getPublishDate(), "YYYY-MM"),
                    group.getItems().size());

        }

        Log.info("firstOfs {0}", JSON.stringify(groups.get(4).getFirstOfs()));




        assertEquals(300, groups.size());
        assertTrue(groups.get(0).getFirst().getPublishDate().isAfter(groups.get(1).getFirst().getPublishDate()));
        assertTrue(groups.get(2).getItems().size() == 3);

        /* */




    }

    public BooksController booksController() {
        return BooksController.instance();
    }

    public TextItemController<TextItem> controller() {
        return (TextItemController)dal().get("posts");
    }
}
