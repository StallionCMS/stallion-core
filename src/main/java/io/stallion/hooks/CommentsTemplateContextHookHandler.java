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

package io.stallion.hooks;

import io.stallion.templating.TemplateContextHookHandler;

import java.util.Map;

import static io.stallion.utils.Literals.*;


public class CommentsTemplateContextHookHandler extends TemplateContextHookHandler {
    @Override
    public void handle(Map<String, Object> context) {
        context.put("comments", list());
    }

    public static void register() {

    }

}
