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

import io.stallion.exceptions.UsageException;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.AssetPreprocessorConfig;

import java.io.File;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * A registry of external-command pre-processors.
 */
public class ExternalCommandPreProcessorRegistry {
    private static ExternalCommandPreProcessorRegistry _instance;

    public static ExternalCommandPreProcessorRegistry instance() {
        if (_instance == null) {
            _instance = new ExternalCommandPreProcessorRegistry();
            _instance.registerDefaults();
        }
        return _instance;
    }

    public static void shutdown() {
        _instance = null;
    }

    public static void register(ExternalCommandAssetPreProcessor preProcessor) {
        instance().register(preProcessor);
    }



    private Map<String, ExternalCommandAssetPreProcessor> preProcessorByName = map();


    public boolean hasPreProcessor(String name) {
        return preProcessorByName.containsKey(name);
    }

    public void registerDefaults() {

        registerPreProcessor(
                new ExternalCommandAssetPreProcessor()
                        .setName("sass {original} {compiled}")
                        .setExtension("sass")
                        .setCommand("sass")
        );
        if (!Settings.isNull()) {
            for(AssetPreprocessorConfig config: Settings.instance().getAssetPreprocessors()) {
                String[] args = new String[]{};
                if (!empty(config.getCommandArgs())) {
                    args = config.getCommandArgs().split(" ");
                }
                registerPreProcessor(
                        new ExternalCommandAssetPreProcessor()
                                .setCommand(config.getCommand())
                                .setExtension(config.getExtension())
                                .setCommandArgs(args)
                                .setName(config.getName())
                );
            }
        }
    }

    public void registerPreProcessor(ExternalCommandAssetPreProcessor preProcessor) {
        if (Settings.instance().getBundleDebug() && Settings.instance().getLocalMode()) {
            if (empty(preProcessor.getName())) {
                throw new UsageException("Empty preprocessor name");
            }

            if (empty(preProcessor.getExtension())) {
                throw new UsageException("Empty preprocessor extension");
            }

            preProcessorByName.put(preProcessor.getName(), preProcessor);
        }
    }

    public void preProcessIfNeeded(String processorName, String path) {
        // Only pre-process if we are in bundle debug mode and local mode
        if (!settings().getBundleDebug() || !settings().getLocalMode()) {
            throw new UsageException("You can only use the pre-processor when in both bundle-debug mode, and local-mode");
        }
        if (path.contains("..")) {
            throw new UsageException("Invalid path " + path);
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.contains("..") || path.startsWith(".")) {
            throw new UsageException("Invalid path " + path);
        }
        File file = new File(settings().getTargetFolder() + "/assets/" + path);
        if (!preProcessorByName.containsKey(processorName)) {
            throw new UsageException("Pre-processor not registered: " + processorName);
        }
        preProcessorByName.get(processorName).processIfNeeded(file);

    }

}
