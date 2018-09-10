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

import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.net.URL;

import static io.stallion.utils.Literals.*;


public class ModeFlags {
    private RunningFrom runningFrom;
    private DataEnvironmentType environmentType;
    private ActionModeFlags[] actionModeFlags;

    public ModeFlags(CommandOptionsBase options, StallionRunAction action, Settings settings) {
        DataEnvironmentType environmentType = settings.getDataEnvironmentType();
        if (environmentType == null) {
            String env = options.getEnv();

            if ("prod".equals(env) || "production".equals(env) || "live".equals(env)) {
                environmentType = DataEnvironmentType.PRODUCTION;
            } else if ("dev".equals(env) || "local".equals(env)) {
                environmentType = DataEnvironmentType.SANDBOX;
            } else if ("staging".equals(env) || "qa".equals(env)) {
                environmentType = DataEnvironmentType.STAGING;
            } else if ("test".equals(env) || "jenkins".equals(env)) {
                environmentType = DataEnvironmentType.TEST;
            } else {
                environmentType = DataEnvironmentType.STAGING;
            }
        }

        RunningFrom runningFrom = options.getRunningFrom();
        if (runningFrom == null) {
            if (!empty(System.getenv().getOrDefault("STALLION_DEPLOY_TIME", "")) ) {
                runningFrom = RunningFrom.DEPLOYED_SERVICE;
            } else {

                //
                // Could also check for the java command, looking for JUnit or maven:
                // "sun.java.command" -> "com.intellij.rt.execution.junit.JUnitStarter -ideVersion5 io.clubby.server.TestEndpoints,testRoot"
                if (new File("./pom.xml").isFile()) {
                    runningFrom = RunningFrom.DEVELOPER;
                } else {
                    runningFrom = RunningFrom.MANUAL_COMMAND;
                }
            }
        }


        ActionModeFlags[] actionModeFlags = action.getActionModeFlags();
        if (options.getLightweightMode() != null && options.getLightweightMode()) {
            actionModeFlags = new ActionModeFlags[]{ActionModeFlags.NO_JOB_AND_TASK_EXECUTION, ActionModeFlags.NO_PRELOADED_DATA};
        }
        init(environmentType, runningFrom, actionModeFlags, null, null);
        Log.info("Environment type: {0} RunningFrom: {1} ActionModeFlags: {2}", environmentType, runningFrom, JSON.stringify(actionModeFlags));
    }

    public ModeFlags(DataEnvironmentType environmentType, RunningFrom runningFrom, ActionModeFlags...actionModeFlags) {
        init(environmentType, runningFrom, actionModeFlags, null, null);
    }

    public ModeFlags(DataEnvironmentType environmentType, RunningFrom runningFrom, ActionModeFlags[] actionModeFlags, boolean localMode, boolean devMode) {
        init(environmentType, runningFrom, actionModeFlags, localMode, devMode);
    }


    private void init(
            DataEnvironmentType environmentType,
            RunningFrom runningFrom,
            ActionModeFlags[] actionModeFlags,
            Boolean localMode,
            Boolean devMode) {
        if (actionModeFlags == null) {
            actionModeFlags = new ActionModeFlags[0];
        }
        this.environmentType = environmentType;
        this.actionModeFlags = actionModeFlags;
        this.runningFrom = runningFrom;

    }



    public boolean isLoadNothing() {
        return ArrayUtils.contains(actionModeFlags, ActionModeFlags.LOAD_NOTHING);
    }

    public boolean isSettingsOnly() {
        return ArrayUtils.contains(actionModeFlags, ActionModeFlags.SETTINGS_ONLY);
    }

    public boolean isPreloadedData() {
        return !ArrayUtils.contains(actionModeFlags, ActionModeFlags.NO_PRELOADED_DATA) &&
                !ArrayUtils.contains(actionModeFlags, ActionModeFlags.SETTINGS_ONLY) &&
                !isLoadNothing()
                ;
    }

    public boolean isStartJobAndTaskExecution() {
        return !ArrayUtils.contains(actionModeFlags, ActionModeFlags.NO_JOB_AND_TASK_EXECUTION) &&
                !ArrayUtils.contains(actionModeFlags, ActionModeFlags.NO_PRELOADED_DATA) &&
                !ArrayUtils.contains(actionModeFlags, ActionModeFlags.SETTINGS_ONLY) &&
                !isLoadNothing()
                ;
    }

    public boolean isLoadJersey() {
        return !ArrayUtils.contains(actionModeFlags, ActionModeFlags.NO_JERSEY) ||
                !ArrayUtils.contains(actionModeFlags, ActionModeFlags.SETTINGS_ONLY) &&
                        !isLoadNothing();
    }

    public boolean isDeveloperMode() {
        return runningFrom.equals(RunningFrom.DEVELOPER);
    }

    public boolean isLocalMode() {
        return !runningFrom.equals(RunningFrom.DEPLOYED_SERVICE);
    }



    public boolean isProduction() {
        return environmentType.equals(DataEnvironmentType.PRODUCTION);
    }

    public boolean isSandboxEnvironment() {
        return environmentType.equals(DataEnvironmentType.SANDBOX);
    }

    public boolean isStaging() {
        return environmentType.equals(DataEnvironmentType.STAGING);
    }

    public boolean isTest() {
        return environmentType.equals(DataEnvironmentType.TEST);
    }



    public RunningFrom getRunningFrom() {
        return runningFrom;
    }

    public ModeFlags setRunningFrom(RunningFrom runningFrom) {
        this.runningFrom = runningFrom;
        return this;
    }


    public DataEnvironmentType getEnvironmentType() {
        return environmentType;
    }

    public ModeFlags setEnvironmentType(DataEnvironmentType environmentType) {
        this.environmentType = environmentType;
        return this;
    }


    public ActionModeFlags[] getActionModeFlags() {
        return actionModeFlags;
    }

    public ModeFlags setActionModeFlags(ActionModeFlags[] actionModeFlags) {
        this.actionModeFlags = actionModeFlags;
        return this;
    }
}
