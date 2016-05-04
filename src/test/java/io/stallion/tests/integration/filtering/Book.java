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

import io.stallion.dal.base.AlternativeKey;
import io.stallion.dal.base.ModelBase;
import io.stallion.dal.base.UniqueKey;
import io.stallion.dal.db.Converter;
import io.stallion.dal.db.converters.JsonListConverter;

import javax.persistence.Column;
import java.time.ZonedDateTime;
import java.util.List;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class Book extends ModelBase {
    private String author = "";
    private ZonedDateTime publishDate;
    private String title = "";
    private List<String> categories = list();
    private boolean isPublished = false;
    private Long publisherId;
    private String isbn = "";
    private String description = "";

    @Column
    @AlternativeKey
    public String getAuthor() {
        return author;
    }

    public Book setAuthor(String author) {
        this.author = author;
        return this;
    }

    @Column
    public ZonedDateTime getPublishDate() {
        return publishDate;
    }

    public Book setPublishDate(ZonedDateTime publishDate) {
        this.publishDate = publishDate;
        return this;
    }

    @Column
    public String getTitle() {
        return title;
    }

    public Book setTitle(String title) {
        this.title = title;
        return this;
    }

    @Column
    @Converter(cls= JsonListConverter.class)
    public List<String> getCategories() {
        return categories;
    }

    public Book setCategories(List<String> categories) {
        this.categories = categories;
        return this;
    }

    @Column
    public boolean isPublished() {
        return isPublished;
    }

    public Book setPublished(boolean published) {
        isPublished = published;
        return this;
    }

    @Column
    public Long getPublisherId() {
        return publisherId;
    }

    public Book setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
        return this;
    }

    @UniqueKey
    @Column
    public String getIsbn() {
        return isbn;
    }

    public Book setIsbn(String isbn) {
        this.isbn = isbn;
        return this;
    }

    @Column(columnDefinition = "longtext")
    public String getDescription() {
        return description;
    }

    public Book setDescription(String description) {
        this.description = description;
        return this;
    }
}
