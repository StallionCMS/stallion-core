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

package io.stallion.plugins.javascript;

import io.stallion.dataAccess.db.DB;
import io.stallion.settings.Settings;
import io.stallion.utils.json.JSON;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.runtime.*;
import jdk.nashorn.internal.runtime.options.Options;
import jline.console.ConsoleReader;
import org.apache.commons.io.IOUtils;

import javax.script.ScriptException;
import java.io.*;
import java.util.Locale;
import java.util.ResourceBundle;

import static io.stallion.utils.Literals.*;


public class JavascriptShell {
    private static final String MESSAGE_RESOURCE = "jdk.nashorn.tools.resources.Shell";
    private static final ResourceBundle bundle = ResourceBundle.getBundle("jdk.nashorn.tools.resources.Shell", Locale.getDefault());
    public static final int SUCCESS = 0;
    public static final int COMMANDLINE_ERROR = 100;
    public static final int COMPILATION_ERROR = 101;
    public static final int RUNTIME_ERROR = 102;
    public static final int IO_ERROR = 103;
    public static final int INTERNAL_ERROR = 104;

    public static void main(String[] args) {
        try {
            new JavascriptShell().runShell(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public int runShell(String[] args) throws IOException, ScriptException {
        OutputStream out = System.out;
        OutputStream err = System.err;
        InputStream in = System.in;

        Context context = makeContext(in, out, err, args);
        Global global = context.createGlobal();
        if(context == null) {
            return 100;
        } else {
            return readEvalPrint(in, out, context, global);
        }
    }


    private static Context makeContext(InputStream in, OutputStream out, OutputStream err, String[] args) {

        PrintStream pout = out instanceof PrintStream?(PrintStream)out:new PrintStream(out);
        PrintStream perr = err instanceof PrintStream?(PrintStream)err:new PrintStream(err);
        PrintWriter wout = new PrintWriter(pout, true);
        PrintWriter werr = new PrintWriter(perr, true);
        ErrorManager errors = new ErrorManager(werr);
        Options options = new Options("nashorn", werr);
        if(args != null) {
            try {
                options.process(args);
            } catch (IllegalArgumentException var27) {
                werr.println(bundle.getString("shell.usage"));
                options.displayHelp(var27);
                return null;
            }
        }
        return new Context(options, errors, wout, werr, Thread.currentThread().getContextClassLoader());
    }


    private static int readEvalPrint(InputStream input, OutputStream out, Context context, Global global) throws IOException, ScriptException {
        JsPluginSettings pluginSettings = new JsPluginSettings();



        //context.eval(global, "");

        //Global global = context.createGlobal();
        //ScriptEnvironment env = context.getEnv();

        String prompt = bundle.getString("shell.prompt");
        //BufferedReader in = new BufferedReader(new InputStreamReader(input));
        ConsoleReader in = new ConsoleReader();
        PrintWriter err = context.getErr();
        Global oldGlobal = Context.getGlobal();
        boolean globalChanged = oldGlobal != global;
        ScriptEnvironment env = context.getEnv();

        if (DB.available()) {
            global.put("DB", DB.instance(), false);
        }

        try {
            if(globalChanged) {
                Context.setGlobal(global);
            }

            global.addShellBuiltins();

            // Load builtins
            global.put("javaToJsHelpers", new JavaToJsHelpers(Sandbox.allPermissions()), true);
            SandboxedContext ctx = new SandboxedContext(Settings.instance().getTargetFolder() + "/js", Sandbox.allPermissions(), pluginSettings);
            global.put("myContext", ctx, true);

            String stallionSharedJs = IOUtils.toString(JavascriptShell.class.getResource("/jslib/stallion_shared.js"));
            context.eval(global, "load(" + JSON.stringify(map(val("script", stallionSharedJs), val("name", "stallion_shared.js"))) + ");", global, "<shellboot>");
            global.put("stallionClassLoader", new UnrestrictedJsClassLoader(), true);


            while(true) {
                String source;
                do {
                    //err.print(prompt);
                    //err.flush();
                    source = "";

                    try {
                        source = in.readLine(prompt);
                    } catch (IOException var14) {
                        err.println(var14.toString());
                    }

                    if(source == null) {
                        return 0;
                    }
                } while(source.isEmpty());

                try {
                    Object e = context.eval(global, source, global, "<shell>");
                    if(e != ScriptRuntime.UNDEFINED) {
                        err.println(JSType.toString(e));
                    }
                } catch (Exception var15) {
                    err.println(var15);
                    if(env._dump_on_error) {
                        var15.printStackTrace(err);
                    }
                }
            }
        } finally {
            if(globalChanged) {
                Context.setGlobal(oldGlobal);
            }

        }
    }
}
