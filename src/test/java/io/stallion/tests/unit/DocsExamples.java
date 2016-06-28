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

package io.stallion.tests.unit;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import io.stallion.utils.json.JSON;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class DocsExamples {
    public void example() throws Exception {

        HttpResponse<String> httpResponse =  Unirest.get("http://httpbin.org/html")
                .asString();
        assert httpResponse.getStatus() == 200;
        assert httpResponse.getBody().contains("Moby Dick");


        // To JSON Object
        HttpResponse<String> bookResponse = Unirest.get("http://httpbin.org/books/1").asString();
        Book book = JSON.parse(bookResponse.getBody(), Book.class);

        // POST JSON
        HttpResponse<JsonNode> jsonResponse = Unirest.post("http://httpbin.org/post")
                .queryString("name", "Mark")
                .field("last", "Polo")
                .asJson();
        assert "Polo".equals(jsonResponse.getBody().getObject().get("last"));

        List<Book> books = list();
        Set<Long> authorIds = books.stream().map(b -> b.getAuthorId()).collect(Collectors.toSet());

    }

    public class Book {
        private long id;
        private long authorId;

        public long getId() {
            return id;
        }

        public Book setId(long id) {
            this.id = id;
            return this;
        }

        public long getAuthorId() {
            return authorId;
        }

        public Book setAuthorId(long authorId) {
            this.authorId = authorId;
            return this;
        }
    }

    public class Author {
        private long id;
        private long name;

        public long getId() {
            return id;
        }

        public Author setId(long id) {
            this.id = id;
            return this;
        }

        public long getName() {
            return name;
        }

        public Author setName(long name) {
            this.name = name;
            return this;
        }
    }
}
