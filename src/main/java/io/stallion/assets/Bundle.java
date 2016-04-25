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

package io.stallion.assets;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * A bundle is a collection of asset files that get concatenated together as one file
 * on production, but rendered independently when in developer mode. A bundle can either
 * be made via a text file, or manually via code (DefinedBundle).
 *
 */
public class Bundle {
    private List<BundleFile> bundleFiles = new ArrayList<>();

    public List<BundleFile> getBundleFiles() {
        return bundleFiles;
    }

    public void setBundleFiles(List<BundleFile> bundleFiles) {
        this.bundleFiles = bundleFiles;
    }

    public String makeHashKey() {
        StringBuilder builder = new StringBuilder();
        for(BundleFile bf: bundleFiles) {
            builder.append(bf.getLiveUrl() + GSEP + bf.getTimeStamp());
        }
        return DigestUtils.md5Hex(builder.toString());
    }
}
