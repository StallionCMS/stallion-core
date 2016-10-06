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

package io.stallion.boot;

import com.mashape.unirest.http.Unirest;
import io.stallion.dataAccess.db.SqlCheckNeedsMigrationAction;
import io.stallion.dataAccess.db.SqlGenerationAction;
import io.stallion.dataAccess.db.SqlMigrationAction;
import io.stallion.exceptions.CommandException;
import io.stallion.exceptions.UsageException;
import io.stallion.plugins.PluginRegistry;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.plugins.PluginTestsRunAction;
import io.stallion.secrets.SecretsAction;
import io.stallion.secrets.SecretsDecryptAction;
import io.stallion.services.Log;
import io.stallion.tools.ExportToHtml;
import io.stallion.tools.ScriptExecBase;
import io.stallion.users.UserAdder;
import io.stallion.utils.Literals;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static io.stallion.utils.Literals.*;

/**
 * Does the main work of booting up Stallion, and executing one of the boot actions.
 */
public class Booter {

    private static final List<StallionRunAction> builtinActions = Literals.list(
            new StallionServer(),
            new UserAdder(),
            new PluginTestsRunAction(),
            new ScriptExecBase(),
            new NewProjectBuilder(),
            new NewDraftPageAction(),
            //new NewJavaPluginRunAction(),
            new ExportToHtml(),
            new SecretsAction(),
            new SqlMigrationAction(),
            new SqlCheckNeedsMigrationAction(),
            new SqlGenerationAction(),
            new InteractiveJavascriptRunAction(),
            new ForceTaskAction(),
            new SecretsDecryptAction()
    );

    public void boot(String[] args) throws Exception {
        boot(args, new StallionJavaPlugin[]{});
    }

    public void boot(String[] args, StallionJavaPlugin[] plugins) throws Exception {

        // Load the plugin jars, to get additional actions
        String targetPath = new File(".").getAbsolutePath();
        for(String arg: args) {
            if (arg.startsWith("-targetPath=")) {
                targetPath = StringUtils.split(arg, "=", 2)[1];
            }
        }
        if (!new File(targetPath).isDirectory() || empty(targetPath) || targetPath.equals("/") || targetPath.equals("/.")) {
            throw new CommandException("You ran stallion with the target path " + targetPath + " but that is not a valid folder.");
        }

        PluginRegistry.loadWithJavaPlugins(targetPath, plugins);
        List<StallionRunAction> actions = new ArrayList(builtinActions);
        actions.addAll(PluginRegistry.instance().getAllPluginDefinedStallionRunActions());

        // Load the action executor
        StallionRunAction action = null;
        for(StallionRunAction anAction: actions) {
            if (empty(anAction.getActionName())) {
                throw new UsageException("The action class " + anAction.getClass().getName() + " has an empty action name");
            }
            if (args.length > 0 && anAction.getActionName().equals(args[0])) {
                action = anAction;
            }
        }
        if (action == null) {
            String msg = "\n\nError! You must pass in a valid action as the first command line argument. For example:\n\n" +
                    ">stallion serve -port=8090 -targetPath=~/my-stallion-site\n";
            if (args.length > 0) {
                msg += "\n\nYou passed in '" + args[0] + "', which is not a valid action.\n";
            }
            msg += "\nAllowed actions are:\n\n";
            for (StallionRunAction anAction: actions) {
                //Log.warn("Action: {0} {1} {2}", action, action.getActionName(), action.getHelp());
                msg += anAction.getActionName() + " - " + anAction.getHelp() + "\n";
            }
            msg += "\n\nYou can get help about an action by running: >stallion <action> help\n\n";
            System.err.println(msg);
            System.exit(1);
        }



        // Load the command line options
        CommandOptionsBase options = action.newCommandOptions();

        CmdLineParser parser = new CmdLineParser(options);

        if (args.length > 1 && "help".equals(args[1])) {
            System.out.println(action.getActionName() + " - " + action.getHelp() + "\n\n");
            System.out.println("\n\nOptions for command " + action.getActionName() + "\n\n");
            parser.printUsage(System.out);
            System.exit(0);
        }

        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println("\n\nError!\n\n" + e.getMessage());
            System.err.println("\n\nAllowed options: \n");
            parser.printUsage(System.err);
            System.err.println("\n");
            System.exit(1);
        }

        options.setExtraPlugins(Arrays.asList(plugins));

        if (!empty(options.getLogLevel())) {
            try {
                Log.setLogLevel(Level.parse(options.getLogLevel()));
            } catch (IllegalArgumentException e) {
                System.err.println("\nError! Invalid log level: " + options.getLogLevel() + "\n\n" + CommandOptionsBase.ALLOWED_LEVELS);
                System.exit(1);
            }
        }
        if (empty(options.getTargetPath())) {
            options.setTargetPath(new File(".").getAbsolutePath());
        }

        // Shutdown hooks
        Thread shutDownHookThread = new Thread() {
            public void run() {
                try {
                    Unirest.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        shutDownHookThread.setName("stallion-shutdown-hook");
        Runtime.getRuntime().addShutdownHook(shutDownHookThread);


        // Execute the action
        action.loadApp(options);
        action.execute(options);



        if (!AppContextLoader.isNull()) {
            AppContextLoader.shutdown();
        }

    }
}
