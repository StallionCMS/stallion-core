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

package io.stallion.requests;

import io.stallion.assets.AssetsController;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;


public class PerPageLiterals {
    private HashSet<String> literals = new LinkedHashSet<>();

    public PerPageLiterals addString(String s) {
        literals.add(s + "\n");
        return this;
    }
    public PerPageLiterals addAsset(String path) {
        if (path.endsWith(".js")) {
            addJs(AssetsController.instance().url(path));
        } else {
            addCss(AssetsController.instance().url(path));
        }
        return this;
    }

    public PerPageLiterals addBundle(String path) {
        addString(AssetsController.instance().bundle(path));
        return this;
    }

    public PerPageLiterals addJs(String url) {
        literals.add(MessageFormat.format("<script src=\"{0}\" type=\"text/javascript\"></script>\n", url));
        return this;
    }
    public PerPageLiterals addCss(String url) {
        literals.add(MessageFormat.format("<link rel=\"stylesheet\" src=\"{0}\">\n", url));
        return this;
    }

    public String stringify() {
        StringBuilder builder = new StringBuilder();
        for (String literal: literals) {
            builder.append(literal);
        }
        return builder.toString();
    }
}
