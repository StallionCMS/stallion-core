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

import io.stallion.dataAccess.MappedModel;
import io.stallion.dataAccess.StandardDisplayableModel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.map;


public class TextItem extends StandardDisplayableModel implements MappedModel {

    private Map<String, Object> attributes = map();

    private List<String> tags = null;
    private List<StElement> elements = null;
    private Map<String, StElement> elementById;




    /***
     * Creates a new object with all fields non-null;
     * @return
     */
    public static TextItem build() {
        TextItem item1 = new TextItem()
                .setSlug("dfasf");
        TextItem item = new TextItem();
        item
                .setTags(new ArrayList<String>())
                .setElementById(new HashMap<>())
                .setElements(new ArrayList<>())
                .setPublishDate(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC")))
                .setTitle("")
                .setDraft(false)
                .setTemplate("")
                .setContent("")
                .setSlug("")
                ;
        return item;

    }


    public List<String> getTags() {
        return tags;
    }

    public TextItem setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public List<StElement> getElements() {
        return elements;
    }

    public TextItem setElements(List<StElement> elements) {
        this.elements = elements;
        return this;
    }

    public Map<String, StElement> getElementById() {
        return elementById;
    }

    public TextItem setElementById(Map<String, StElement> elementById) {
        this.elementById = elementById;
        return this;
    }

    public Integer getYear() {
        return getPublishDate().getYear();
    }

    public Integer getMonth() {
        return getPublishDate().getMonth().getValue();
    }




    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
