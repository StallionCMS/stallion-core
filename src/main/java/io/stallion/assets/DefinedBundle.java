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

import java.util.*;

import static io.stallion.utils.Literals.map;


public class DefinedBundle extends Bundle {

    private static DefinedBundle alwaysFooterJavascripts;
    private static DefinedBundle alwaysHeadStylesheets;
    private static DefinedBundle adminFooterJavascripts;
    private static DefinedBundle adminHeadStylesheets;
    private static Map<String, DefinedBundle> bundleRegistry;

    /** Static methods for the bundle singletons
     */
    public static void load() {
        loadAdmin();
        loadAlways();
        bundleRegistry = map();
    }

    public static DefinedBundle getByName(String name) {
        if ("alwaysFooterJavascripts".equals(name)) {
            return alwaysFooterJavascripts;
        } else if ("alwaysHeadStylesheets".equals(name)) {
            return alwaysHeadStylesheets;
        } else if ("adminHeadStylesheets".equals(name)) {
            return adminHeadStylesheets;
        } else if ("adminFooterJavascripts".equals(name)) {
            return adminFooterJavascripts;
        } else if (bundleRegistry.containsKey(name)) {
            return bundleRegistry.get(name);
        }

        throw new UsageException("DefinedBundle not found for name " + name);
    }

    public static void register(DefinedBundle bundle) {
        bundleRegistry.put(bundle.getName(), bundle);
    }

    private static void loadAlways() {
        alwaysFooterJavascripts = new DefinedBundle(
                "alwaysFooterJavascripts",
                ".js",
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("always/jquery-1.11.3.min.js"),
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("always/riot-and-compiler.min.js")
                        .setDebugUrl("always/riot-and-compiler.js")
                ,
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("always/stallion.js")
        );
        alwaysHeadStylesheets = new DefinedBundle(
                "alwaysHeadStylesheets",
                ".css",
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("always/pure-min.css"),
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("always/grids-responsive-min.css"),
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("always/stallion.css")
        );


    }

    private static void loadAdmin() {
        adminHeadStylesheets = new DefinedBundle(
                "adminHeadStylesheets",
                ".css",
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("admin/bootstrap3.min.css"),
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("admin/admin.css")
        );
        adminFooterJavascripts = new DefinedBundle(
                "adminFooterJavascripts",
                ".js",
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("admin/moment.min.js")
                ,
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("admin/react.min.js")
                        .setDebugUrl("admin/react.js"),
                new BundleFile()
                        .setPluginName("stallion")
                        .setLiveUrl("admin/react-router.0.13.3.min.js")
                        .setDebugUrl("admin/react-router.0.13.3.js")
        );
    }

    public static void shutdown() {
        alwaysFooterJavascripts = null;
        alwaysHeadStylesheets = null;
        adminFooterJavascripts = null;
        adminHeadStylesheets = null;
        bundleRegistry = null;
    }

    public static DefinedBundle getAlwaysFooterJavascripts() {
        return alwaysFooterJavascripts;
    }

    public static DefinedBundle getAlwaysHeadStylesheets() {
        return alwaysHeadStylesheets;
    }

    public static DefinedBundle getAdminFooterJavascripts() {
        return adminFooterJavascripts;
    }

    public static DefinedBundle getAdminHeadStylesheets() {
        return adminHeadStylesheets;
    }


    /* The properties of the defined bundle instance */

    private String extension = "";
    private String name;
    private Set<BundleFile> bundleFiles = new LinkedHashSet<>();

    public DefinedBundle(String name, String extension, BundleFile...files) {
        for (BundleFile bf: files) {
            this.bundleFiles.add(bf);
        }
        this.name = name;
        this.extension = extension;

    }

    public DefinedBundle add(BundleFile bf) {
        bundleFiles.add(bf);
        return this;
    }

    public DefinedBundle add(String plugin, String path) {
        this.add(new BundleFile().setPluginName(plugin).setLiveUrl(path));
        return this;
    }


    public DefinedBundle registerBundleFile(BundleFile...files) {
        for (BundleFile bf: files) {
            this.bundleFiles.add(bf);
        }
        return this;
    }

    @Override
    public List<BundleFile> getBundleFiles() {
        return new ArrayList<BundleFile>(bundleFiles);
    }

    @Override
    public void setBundleFiles(List<BundleFile> bundleFiles) {
        /* noop */
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
