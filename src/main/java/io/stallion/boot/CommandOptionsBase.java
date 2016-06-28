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

import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.settings.Settings;
import io.stallion.settings.StrictnessLevel;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;


import java.util.ArrayList;
import java.util.List;

import static io.stallion.utils.Literals.list;


public class CommandOptionsBase {
    public static final String ALLOWED_LEVELS = "Allowed values: OFF, SEVERE, WARNING, INFO, FINE, FINER, FINEST";

    private String action = "";
    @Option(name="-targetPath", usage="The path to your project directory with the settings.toml file in it")
    private String targetPath = "";
    @Option(name="-logLevel", usage="How verbose the logging is. " + ALLOWED_LEVELS)
    private String logLevel = "";
    @Option(name="-loggingAlwaysIncludesLineNumber", usage="If true, even FINE and INFO log statements will use the stack frame to find the log line and class")
    private boolean loggingAlwaysIncludesLineNumber = false;

    @Option(name="-strictnessLevel", usage="If strict, will throw exceptions for errors such as missing templates, bad configs, etc. If lax, will eat the errors and try to keep running anyway.")
    private StrictnessLevel strictnessLevel;
    @Option(name="-autoReload", usage="If a javascript file is touched, the entire server will reload. Use this only during development")
    private boolean autoReload = false;

    @Option(name="-env", usage="The environment you are running in. The file settings.(env).toml will be merged into your settings.")        // no usage
    private String env = "local";

    @Option(name="-devMode", usage="Set to 'true' if you want to use the development URL for resource assets")
    private Boolean devMode = false;

    @Argument(index = 0)
    private List<String> arguments = new ArrayList<String>();

    private List<StallionJavaPlugin> extraPlugins = list();


    public Settings hydrateSettings(Settings settings) {
        if (strictnessLevel != null) {
            settings.setStrictnessLevel(strictnessLevel);
        }
        return settings;
    }


    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }


    public String getAction() {
        if (getArguments().size() == 0) {
            return "";
        }
        return getArguments().get(0);
    }

    public String getActionTarget() {
        if (getArguments().size() <= 1) {
            return "";
        }
        return getArguments().get(1);
    }


    public void setAction(String action) {
        this.action = action;
    }

    public List<String> getArguments() {
        return arguments;
    }


    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }


    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public List<StallionJavaPlugin> getExtraPlugins() {
        return extraPlugins;
    }

    public void setExtraPlugins(List<StallionJavaPlugin> extraPlugins) {
        this.extraPlugins = extraPlugins;
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public CommandOptionsBase setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
        return this;
    }


    public String getEnv() {
        return env;
    }



    public void setEnv(String env) {
        this.env = env;
    }


    public Boolean isDevMode() {
        return devMode;
    }


    public void setDevMode(Boolean devMode) {
        this.devMode = devMode;
    }

    public boolean isLoggingAlwaysIncludesLineNumber() {
        return loggingAlwaysIncludesLineNumber;
    }

    public CommandOptionsBase setLoggingAlwaysIncludesLineNumber(boolean loggingAlwaysIncludesLineNumber) {
        this.loggingAlwaysIncludesLineNumber = loggingAlwaysIncludesLineNumber;
        return this;
    }
}
