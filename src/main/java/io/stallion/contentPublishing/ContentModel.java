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

import java.util.List;


public interface ContentModel {

    String getContent();

    <Y extends ContentModel> Y  setContent(String content);

    String getOriginalContent();

    <Y extends ContentModel> Y  setOriginalContent(String originalContent);

    List<ContentWidget> getWidgets();

    <Y extends ContentModel> Y  setWidgets(List<ContentWidget> widgets);

    List<ContentWidget> getElements();

    <Y extends ContentModel> Y  setElements(List<ContentWidget> elements);


}
