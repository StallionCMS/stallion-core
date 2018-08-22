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

package io.stallion.plugins;

import io.stallion.boot.StallionRunAction;
import io.stallion.services.DynamicSettings;
import io.stallion.services.Log;
import org.apache.commons.lang3.StringUtils;
import org.apache.xbean.classloader.JarFileClassLoader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static io.stallion.utils.Literals.*;

/**
 * Registry Service that loads plugins, boots them, and keeps track of them.
 *
 */
public class PluginRegistry {


    private JarFileClassLoader classLoader;

    private Map<String, StallionJavaPlugin> javaPluginByName = map();


    private static PluginRegistry _instance;

    public static PluginRegistry instance() {

        return _instance;
    }

    public static PluginRegistry loadWithJavaPlugins(String targetPath) {
        if (_instance == null) {
            _instance = new PluginRegistry();
            _instance.loadJarPlugins(targetPath);
        }
        return _instance;
    }

    public static PluginRegistry loadWithJavaPlugins(String targetPath, StallionJavaPlugin ...extraPlugins) {
        if (_instance == null) {
            _instance = new PluginRegistry();
            _instance.loadJarPlugins(targetPath);
            for (StallionJavaPlugin plugin: extraPlugins) {
                _instance.loadPluginFromBooter(plugin);
            }
        }
        return _instance;
    }

    PluginRegistry() {
        //new JarFileClassLoader()
        this.classLoader = new JarFileClassLoader(
                "PluginClassLoader" + System.currentTimeMillis(),
                new URL[]{},
                PluginRegistry.class.getClassLoader()
        );
    }

    /*
    public static PluginRegistry load() {
        return PluginRegistry.load(new ArrayList<StallionPlugin>());
    }

    public static PluginRegistry load(List<StallionPlugin> extraPlugins) {
        _instance = new PluginRegistry();
        try {

            if (extraPlugins != null) {
                for(StallionPlugin booter: extraPlugins) {
                    _instance.loadPluginFromBooter(booter);
                }
            }
            _instance.load(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return _instance;
    }
    */

    public static void shutdown() {
        if (_instance != null) {

            for (StallionJavaPlugin plugin: _instance.getJavaPluginByName().values()) {
                plugin.shutdown();
            }
        }
        _instance = null;
    }



   // public void load(Boolean shouldWatchFiles) throws Exception {
   //     loadPlugins(shouldWatchFiles);
    //}

    public List<StallionRunAction> getAllPluginDefinedStallionRunActions() {
        List<StallionRunAction> actions = list();
        for (StallionJavaPlugin plugin: getJavaPluginByName().values()) {
            actions.addAll(plugin.getActions());
        }
        return actions;
    }



    public void loadJarPlugins(String targetPath) {
        try {
            doLoadJarPlugins(targetPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private void doLoadJarPlugins(String targetPath) throws Exception {
        String jarFolderPath = targetPath + "/jars";
        Log.fine("Loading jars at {0}", jarFolderPath);
        File jarFolder = new File(jarFolderPath);
        if (!jarFolder.exists() || !jarFolder.isDirectory()) {
            Log.fine("No jar folder exists at {0}. No jar plugins will be loaded.", jarFolderPath);
            return;
        }
        File[] files = jarFolder.listFiles();
        ArrayList<URL> urls = new ArrayList<URL>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".jar")) {
                //urls.add(files[i].toURL());
                Log.fine("Loading plugin jar {0}", files[i].toURI());
                urls.add(files[i].toURI().toURL());
            }
        }

        String pluginBooterClass = "";

        for (URL jarUrl: urls) {
            // Add jars to the class loader
            getClassLoader().addURL(jarUrl);
        }

        for (URL jarUrl: urls) {
            // Load the booter class
            Manifest m = new JarFile(jarUrl.getFile()).getManifest();
            Log.finer("Found manifest for jar {0}", jarUrl);
            if (!StringUtils.isEmpty(m.getMainAttributes().getValue("pluginBooterClass"))) {
                pluginBooterClass = m.getMainAttributes().getValue("pluginBooterClass");
            }
            if (empty(pluginBooterClass)) {
                continue;
            }
            Log.fine("Load plugin class {0} from jar={1}", pluginBooterClass, jarUrl);
            Class booterClass = getClassLoader().loadClass(pluginBooterClass);
            Log.finer("Booter class was loaded from: {0} ", booterClass.getProtectionDomain().getCodeSource().getLocation());
            StallionJavaPlugin booter = (StallionJavaPlugin)booterClass.newInstance();
            loadPluginFromBooter(booter);
            DynamicSettings.instance().initGroup(booter.getPluginName());
        }
    }

    /**
     * Boot all jar plugins that have already been loaded.
     *
     */
     public void bootJarPlugins()  {
        for(StallionJavaPlugin plugin: getJavaPluginByName().values()) {
            try {
                plugin.boot();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loadPluginFromBooter(StallionJavaPlugin booter) {
        Log.info("Load plugin {0} from class {1}", booter.getPluginName(), booter.getClass().getName());
        if (getJavaPluginByName().containsKey(booter.getPluginName())) {
            Log.warn("Plugin already loaded, skipping {0}", booter.getPluginName());
            return;
        }
        booter.setPluginRegistry(this);
        getJavaPluginByName().put(booter.getPluginName(), booter);
    }


    public JarFileClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(JarFileClassLoader classLoader) {
        this.classLoader = classLoader;
    }


    public Map<String, StallionJavaPlugin> getJavaPluginByName() {
        return javaPluginByName;
    }
}
