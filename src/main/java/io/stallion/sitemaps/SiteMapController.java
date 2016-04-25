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

package io.stallion.sitemaps;

import io.stallion.dal.base.Displayable;
import io.stallion.restfulEndpoints.SlugRegistry;
import io.stallion.settings.SecondaryDomain;

import java.util.ArrayList;
import java.util.List;

import static io.stallion.Context.settings;
import static io.stallion.utils.Literals.empty;


public class SiteMapController {
    private static SiteMapController _instance;
    public static SiteMapController instance() {
        return _instance;
    }
    public static void load() {
        _instance = new SiteMapController();
    }
    public static void shutdown() {
        _instance = null;
    }

    private List<SiteMapItem> extraItems = new ArrayList<>();

    public List<SiteMapItem> getAllItems() {
        List<SiteMapItem> items = new ArrayList<>();
        items.addAll(extraItems);
        for(Displayable item: SlugRegistry.instance().listAll()) {
           items.add(
                   new SiteMapItem()
                           .setPermalink(item.getPermalink())
           );
        }
        return items;
    }

    /**
     * Get all items that exist on the given domain, as opposed to being accessible from a secondary
     * domain.
     *
     * @param domain
     * @return
     */
    public List<SiteMapItem> getAllItemsForDomain(String domain) {
        boolean isDefaultDomain = true;
        for(SecondaryDomain sd: settings ().getSecondaryDomains()) {
            if (sd.getDomain().equals(domain)) {
                isDefaultDomain = false;
            }
        }
        List<SiteMapItem> items = new ArrayList<>();
        items.addAll(extraItems);
        for(Displayable item: SlugRegistry.instance().listAll()) {
            if (isDefaultDomain && !empty(item.getOverrideDomain())) {
                continue;
            }
            if (!isDefaultDomain && !domain.equals(item.getOverrideDomain())) {
                continue;
            }
            items.add(
                    new SiteMapItem()
                            .setPermalink(item.getPermalink())
            );
        }
        return items;
    }

    public SiteMapController addItem(SiteMapItem item) {
        extraItems.add(item);
        return this;
    }

    public SiteMapController addDisplayable(Displayable displayableItem) {
        extraItems.add(
                new SiteMapItem()
                        .setPermalink(displayableItem.getPermalink())
        );
        return this;
    }
}
