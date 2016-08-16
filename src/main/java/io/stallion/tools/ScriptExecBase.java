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

package io.stallion.tools;

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.StallionRunAction;
import io.stallion.exceptions.CommandException;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.javascript.*;
import io.stallion.utils.json.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;


import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class ScriptExecBase implements StallionRunAction<ScriptOptions> {



    @Override
    public ScriptOptions newCommandOptions() {
        return new ScriptOptions();
    }

    @Override
    public String getActionName() {
        return "exec-script";
    }

    @Override
    public String getHelp() {
        return "Executes a jython or javascript script.";
    }

    @Override
    public void loadApp(ScriptOptions options) {

        AppContextLoader.loadCompletelyForScript(options);
    }

    @Override
    public void execute(ScriptOptions options) throws Exception {
        if (options.getArguments().size() < 2) {
            throw new CommandException("You must pass in the script that you want to execute as a positional argument");
        }
        String scriptArg = options.getArguments().get(1);

        String plugin = "";
        String path = scriptArg;
        String[] parts = scriptArg.split(":");
        String scriptPath = "";
        if (parts.length > 1) {
            plugin = parts[0];
            path = parts[1];
        }

        URL url = null;
        String folder = null;
        if (!empty(plugin)) {
            if ("stallion".equals(plugin)) {
                url = getClass().getResource(path);
            }
            if (url == null && PluginRegistry.instance() != null) {
                StallionJavaPlugin booter = PluginRegistry.instance().getJavaPluginByName().getOrDefault(plugin, null);
                if (booter != null) {
                    url = booter.getClass().getResource(path);
                }
            }
            if (url == null) {
                if (plugin.equals("js")) {
                    String fullPath = settings().getTargetFolder() + "/js" + path;
                    File file = new File(fullPath);
                    if (file.isFile()) {
                        url = new URL("file:" + fullPath);
                        scriptPath = file.getParent();
                        folder = settings().getTargetFolder() + "/js";
                    }

                } else {

                    String fullPath = settings().getTargetFolder() + "/plugins/" + plugin + path;
                    File file = new File(fullPath);
                    if (file.isFile()) {
                        url = new URL("file:" + fullPath);
                        scriptPath = file.getParent();
                        folder = settings().getTargetFolder() + "/plugins/" + plugin;
                    }
                }
            }
        } else {
            url = new URL("file:" + settings().getTargetFolder() + "/js/" + path);
            folder = settings().getTargetFolder() + "/js/";
        }
        if (url == null) {
            throw new CommandException("Could not find matching script for '" + scriptArg + "' in any folder or jar resource.");
        }
        if (empty(folder)) {
            folder = new File(url.toString()).getParent();
        }
        String source = IOUtils.toString(url, UTF8);
        List<String> args = list(path);
        if (options.getArguments().size() > 2) {
            args.addAll(options.getArguments().subList(2, options.getArguments().size()));
        }
        if (path.endsWith(".js")) {
            executeJavascript(source, url, scriptPath, folder, args, plugin);
        } else {
            throw new CommandException("Unknown extension for path. Supported extensions are .py for jython and .js for javascript. Path was: " + path);
        }
    }

    public void executeJavascript(String source, URL url, String scriptPath, String folder, List<String> args, String plugin) throws Exception {
        ScriptEngine scriptEngine = null;
        if (plugin.equals("js") || plugin.equals("stallion")) {
            JsPluginEngine pluginEngine = PluginRegistry.instance().getEngine("main.js");
            if (pluginEngine != null) {
                scriptEngine = pluginEngine.getScriptEngine();
            }
        } else if (!empty(plugin)) {
            JsPluginEngine pluginEngine = PluginRegistry.instance().getEngine(plugin);
            if (pluginEngine != null) {
                scriptEngine = pluginEngine.getScriptEngine();
            }
        }
        if (scriptEngine == null) {
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            scriptEngine = scriptEngineManager.getEngineByName("nashorn");
            scriptEngine.eval(IOUtils.toString(getClass().getResource("/jslib/jvm-npm.js"), UTF8));
            scriptEngine.eval(IOUtils.toString(getClass().getResource("/jslib/stallion_shared.js"), UTF8));
            String nodePath = folder + "/node_modules";
            scriptEngine.eval("require.NODE_PATH = \"" + nodePath + "\"");
            scriptEngine.put("myContext", new SandboxedContext(plugin, Sandbox.allPermissions(), new JsPluginSettings()));
        }
        if (true || newCommandOptions().isDevMode()) {
            Scanner in = new Scanner(System.in);
            while (true) {
                source = IOUtils.toString(url, UTF8);
                try {
                    scriptEngine.eval("load(" + JSON.stringify(map(val("script", source), val("name", url.toString()))) + ");");
                    //scriptEngine.eval(IOUtils.)
                } catch(Exception e) {
                    ExceptionUtils.printRootCauseStackTrace(e);
                } finally {

                }
                System.out.println("Hit enter to re-run the script. Type quit and hit enter to stop.");
                String line = in.nextLine().trim();
                if (empty(line)) {
                    continue;
                } else {
                    break;
                }
            }
        } else {
            scriptEngine.eval(source);
        }

    }


}
