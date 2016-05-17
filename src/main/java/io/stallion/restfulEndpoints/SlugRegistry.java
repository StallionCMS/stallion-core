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

package io.stallion.restfulEndpoints;

import io.stallion.dataAccess.Displayable;
import io.stallion.exceptions.UsageException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.stallion.utils.Literals.empty;


public class SlugRegistry {
    private static SlugRegistry _instance;

    private Map<String, Displayable> slugMap = new HashMap<>();
    private Map<String, String> redirectMap = new HashMap<>();

    public SlugRegistry() {

    }

    public static SlugRegistry instance() {
        if (_instance == null) {
            throw new UsageException("Must call SlugRegistry.load() before calling instance();");
        }
        return _instance;
    }

    public static SlugRegistry load() {
        _instance = new SlugRegistry();
        return _instance;
    }

    public void shutdown() {
        _instance = null;
    }


    public Collection<Displayable> listAll() {
        return slugMap.values();
    }

    public SlugRegistry addDisplayable(Displayable item) {
        getSlugMap().put(item.getSlug(), item);
        return this;
    }

    public boolean hasUrl(String url) {
        return getSlugMap().containsKey(url);
    }

    public Displayable lookup(String url) {
        return getSlugMap().get(url);
    }

    public Displayable lookup(String url, Displayable defaultObj) {
        return getSlugMap().getOrDefault(url, defaultObj);
    }

    public Map<String, Displayable> getSlugMap() {
        return slugMap;
    }

    public void registerRedirect(String fromUrl, String toUrl) {
        if (fromUrl == null || toUrl == null) {
            return;
        }
        fromUrl = fromUrl.trim();
        if (empty(fromUrl)) {
            return;
        }
        redirectMap.put(fromUrl, toUrl);
    }

    public String returnRedirectIfExists(String fromUrl) {
        if (redirectMap.containsKey(fromUrl)) {
            return redirectMap.get(fromUrl);
        }
        return null;
    }
}
