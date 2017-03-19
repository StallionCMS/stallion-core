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

package io.stallion.assets;

import io.stallion.exceptions.UsageException;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


/**
 * A wrapper around AssetsController that only allows access to particular methods.
 *
 */
public class AssetsControllerSafeWrapper {
    private AssetsController assets;
    public AssetsControllerSafeWrapper(AssetsController assets) {
        this.assets = assets;
    }

    public String resource(String path) {
        return this.assets.resource(path);
    }

    public String resource(String path, String plugin) {
        return this.assets.resource(path, plugin);
    }
    public String resource(String path, String plugin, String developerUrl) {
        return assets.resource(path, plugin, developerUrl);
    }



    public String pageFooterLiterals() {
        return assets.pageHeadLiterals();
    }

    public String pageHeadLiterals() {
        return assets.pageHeadLiterals();
    }

    public String bundle(String fileName) {
        return assets.bundle(fileName);
    }

    public String bundle(String plugin, String fileName) {
        return assets.bundle(plugin, fileName);
    }

    public String url(String path) {
        return assets.url(path);
    }
}
