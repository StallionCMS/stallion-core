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

package io.stallion.utils;

import io.stallion.tests.integration.filtering.Book;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class TestLiterals {

    @Test
    public void testFilterApply() {
        List<Book> books = list(new Book().setTitle("alpha"), new Book().setTitle("beta"));

        books.stream().filter(b->{return !empty(b.getTitle()) && !empty(b.getAuthor());}).collect(Collectors.toList());
        books.stream().filter(b->!empty(b.getTitle())).map(b->b.getTitle()).collect(Collectors.toList());
        apply(filter(books, b->!empty(b.getTitle())), b->b.getTitle());
        List<String> titles = apply(
                filter(books, b->!empty(b.getTitle())),
                b->b.getTitle()
        );


    }
}
