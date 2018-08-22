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

package io.stallion.templating;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import io.stallion.utils.Markdown;


public class MarkdownFilter implements Filter {
    @Override
    public Object filter(Object o, JinjavaInterpreter jinjavaInterpreter, String... strings) {
        if (o == null || !(o instanceof String) || ((String) o).length() == 0) {
            return o;
        }
        return Markdown.instance().process(o.toString());
    }

    @Override
    public String getName() {
        return "markdown";
    }
}
