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

package io.stallion.settings;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class SecondaryDomain {
    private String domain = "";
    private String rewriteRoot = "";
    private String scheme;
    private boolean stripRootFromPageSlug = true;

    public String getDomain() {
        return domain;
    }

    public SecondaryDomain setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getRewriteRoot() {
        return rewriteRoot;
    }

    public SecondaryDomain setRewriteRoot(String rewriteRoot) {
        this.rewriteRoot = rewriteRoot;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public SecondaryDomain setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public boolean isStripRootFromPageSlug() {
        return stripRootFromPageSlug;
    }

    public SecondaryDomain setStripRootFromPageSlug(boolean stripRootFromPageSlug) {
        this.stripRootFromPageSlug = stripRootFromPageSlug;
        return this;
    }
}
