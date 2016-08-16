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

package io.stallion.plugins.javascript;

import com.moandjiezana.toml.Toml;
import io.stallion.exceptions.UsageException;
import io.stallion.plugins.PluginRegistry;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.settings.SettingsLoader;
import io.stallion.settings.childSections.CustomSettings;
import io.stallion.utils.json.JSON;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.options.Options;
import org.apache.commons.io.IOUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;


public class JsPluginEngine {


    private ScriptEngine scriptEngine;
    //private ScriptEngineManager scriptEngineManager;
    private HashSet<String> watchedPaths = new HashSet<>();
    private Boolean watchFiles = true;
    private String folder;

    public JsPluginEngine() {
        this(true);
    }

    public JsPluginEngine(Boolean watchFiles) {
        this.watchFiles = watchFiles;

    }

    public void shutdown() {

    }

    public Object evaluate(String source) throws Exception {
        Log.info("eval {0}", source);
        return scriptEngine.eval(source);
    }

    public void loadJavascript(String plugin, String fullPath) throws Exception {


        JsPluginSettings pluginSettings = new SettingsLoader().loadSettings(plugin, JsPluginSettings.class);


        Sandbox box = Sandbox.forPlugin(plugin);
        if (box == null) {
            loadUnrestrictedJavascript(fullPath, pluginSettings);
        } else {
            loadSandboxedJavascript(box, fullPath, pluginSettings);
        }

    }

    public void loadSandboxedJavascript(Sandbox box, String fullPath, JsPluginSettings pluginSettings) throws Exception {
        SandboxClassFilter classFilter = new SandboxClassFilter(box);

        this.scriptEngine = new NashornScriptEngineFactory().getScriptEngine(classFilter);

        Log.info("Load sandboxed js file {0}", fullPath);
        folder = new File(fullPath).getParent();
        scriptEngine.put("javaToJsHelpers", new JavaToJsHelpers(box));

        SandboxedContext ctx = new SandboxedContext(folder, box, pluginSettings);
        scriptEngine.put("myContext", ctx);
        String stallionSharedJs = IOUtils.toString(getClass().getResource("/jslib/stallion_shared.js"), UTF8);
        classFilter.setDisabled(true); // Turn off white-listing while loading stallion_shared, since we need access to more classes

        scriptEngine.eval("load(" + JSON.stringify(map(val("script", stallionSharedJs), val("name", "stallion_shared.js"))) + ");");
        classFilter.setDisabled(false); // Turn whitelisting back on
        scriptEngine.put("stallionClassLoader", new SandboxedClassLoader(box));
        //scriptEngine.eval("Java = {extend: Java.extend, type: function(className) { return stallionClassLoader.loadClass(className).static;  }}");
        //scriptEngine.eval("Packages = undefined;java = undefined;");
        scriptEngine.eval("load(\"" + fullPath + "\");");

        Log.info("Loaded js plugin {0}", fullPath);
    }

    public void runTestsInFileAndPrintResults(String testFile) {
        List<TestResults> allResults = runTestsInFile(testFile);

        boolean hasError = false;

        for (TestResults results: allResults) {
            if (results.hasAnyErrors()) {
                hasError = true;
            }
            results.printResults();
        }

        if (hasError) {
            throw new AssertionError("Javascript tests failed with errors for file: " + testFile);
        }
    }

    public List<TestResults> runTestsInFile(String testFile) {

        if (testFile.contains("..") || testFile.startsWith("/")) {
            throw new UsageException("Invalid file name: " + testFile);
        }
        String path = folder + "/" + testFile;
        if (!new File(path).isFile()) {
            throw new UsageException("Javascript test file not found: " + path);
        }
        Log.info("Running javascript tests for file {0}", path);
        SuitesHolder holder = new SuitesHolder().setFile(testFile);
        scriptEngine.put("jsSuitesHolder", holder);
        try {
            scriptEngine.eval("load(\"" + path + "\");");
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        boolean hasError = false;
        List<TestResults> results = list();

        for (JsTestSuite suite: holder.getSuites()) {
            results.add(suite.getResults());
        }
        return results;
    }


    private void loadUnrestrictedJavascript(String fullPath, JsPluginSettings pluginSettings) throws Exception {
        folder = new File(fullPath).getParent();
        this.scriptEngine = (NashornScriptEngine)new NashornScriptEngineFactory().getScriptEngine();

        scriptEngine.put("javaToJsHelpers", new JavaToJsHelpers(null));

        String jvmNpm = IOUtils.toString(getClass().getResource("/jslib/jvm-npm.js"), UTF8);
        scriptEngine.eval("load(" + JSON.stringify(map(val("script", jvmNpm), val("name", "jvm-npm.js"))) + ");");

        SandboxedContext ctx = new SandboxedContext(folder, Sandbox.allPermissions(), pluginSettings);
        scriptEngine.put("myContext", ctx);

        String stallionSharedJs = IOUtils.toString(getClass().getResource("/jslib/stallion_shared.js"), UTF8);
        scriptEngine.eval("load(" + JSON.stringify(map(val("script", stallionSharedJs), val("name", "stallion_shared.js"))) + ");");


        scriptEngine.put("stallionClassLoader", new UnrestrictedJsClassLoader());
        //scriptEngine.eval("Java.type = function(className) { var cls = stallionClassLoader.loadClass(className); if (cls) {return cls.static} else { print('Could not find class ' + className); return null; }  };");

        scriptEngine.put("pluginFolder", folder);
        scriptEngine.put("jsEngine", this);



        //Global global = Context.getGlobal();
        //global.put("astring", "foo", true);

        //Log.info("astring: {0}", scriptEngine.get("astring"));



        Log.info("Load js file {0}", fullPath);
        String folder = new File(fullPath).getParent();
        String nodePath = folder + "/node_modules";
        scriptEngine.eval("require.NODE_PATH = \"" + nodePath + "\"");

        scriptEngine.eval("load(\"" + fullPath + "\");");

        Log.info("Finish loading js {0}", fullPath);

    }

           /*
    private void experimentalloadUnrestrictedJavascript(String fullPath, JsPluginSettings pluginSettings) throws Exception {
        ErrorManager errors = new ErrorManager();

        Options options = new Options("nashorn");
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());


        Global global = context.createGlobal();
        Context.setGlobal(global);

        global.put("javaToJsHelpers", new JavaToJsHelpers(null), true);

        String jvmNpm = IOUtils.toString(getClass().getResource("/jslib/jvm-npm.js"));
        context.eval(global, "load(" + JSON.stringify(map(val("script", jvmNpm), val("name", "jvm-npm.js"))) + ");", global, "<engineloading>");

        String stallionSharedJs = IOUtils.toString(getClass().getResource("/jslib/stallion_shared.js"));
        context.eval(global, "load(" + JSON.stringify(map(val("script", stallionSharedJs), val("name", "stallion_shared.js"))) + ");", global, "<engineloading>");


        global.put("stallionClassLoader", new UnrestrictedJsClassLoader(), true);
        //scriptEngine.eval("Java.type = function(className) { var cls = stallionClassLoader.loadClass(className); if (cls) {return cls.static} else { print('Could not find class ' + className); return null; }  };");

        global.put("pluginFolder", folder, true);
        global.put("jsEngine", this, true);

        SandboxedContext ctx = new SandboxedContext(folder, Sandbox.allPermissions(), pluginSettings);
        global.put("myContext", ctx, true);


        global.put("astring", "foo", true);

        //Log.info("astring: {0}", scriptEngine.get("astring"));

        global.addShellBuiltins();

        //ScriptFunction value = ScriptFunction.createBuiltin("calc", EvalLoop.getCalcHandle2(global, context));
        global.addOwnProperty("calc", 2, value);

        Log.info("Load js file {0}", fullPath);
        String folder = new File(fullPath).getParent();
        String nodePath = folder + "/node_modules";
        context.eval(global, "require.NODE_PATH = \"" + nodePath + "\"", global, "<loading>");

        context.eval(global, "load(\"" + fullPath + "\");", global, "<evalscript>");

        Log.info("Finish loading js {0}", fullPath);
    }
    */


    public void fileChangeCallback(String jsPath) throws Exception {
        //loadJavascript(jsPath);
        Log.info("File change callback {0}", jsPath);
    }




    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public void setScriptEngine(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }


}
