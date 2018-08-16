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

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.Displayable;
import io.stallion.dataAccess.ModelController;
import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.list;


public class SlugRegistry {
    private static SlugRegistry _instance;

    private Map<String, BucketThing> slugMap = new HashMap<>();
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
        List<Displayable> allThings = list();
        for(BucketThing thing: slugMap.values()) {
            allThings.add(
                    (Displayable)DataAccessRegistry.instance().get(thing.getBucket()).originalForId(thing.getId())
            );
        }
        return allThings;
    }

    public SlugRegistry addDisplayable(Displayable item) {
        if (item.getSlug() == null) {
            return this;
        }
        getSlugMap().put(item.getSlug(), new BucketThing().setBucket(item.getController().getBucket()).setId(item.getId()));
        return this;
    }

    public boolean hasUrl(String url) {
        return getSlugMap().containsKey(url);
    }

    public Displayable lookup(String url) {
        BucketThing thing = getSlugMap().get(url);
        Log.info("URL={0} thingId={1} thingBucket={2}", url, thing.getId(), thing.getBucket());
        ModelController controller = DataAccessRegistry.instance().get(thing.getBucket());
        return (Displayable)controller.originalForId(thing.getId());
    }



    public Displayable lookup(String url, Displayable defaultObj) {
        Displayable item = lookup(url);
        if (item == null) {
            return defaultObj;
        }
        return item;
    }

    public Map<String, BucketThing> getSlugMap() {
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

    public static class BucketThing {
        private String bucket;
        private Long id;

        public String getBucket() {
            return bucket;
        }

        public BucketThing setBucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public Long getId() {
            return id;
        }

        public BucketThing setId(Long id) {
            this.id = id;
            return this;
        }
    }
}
