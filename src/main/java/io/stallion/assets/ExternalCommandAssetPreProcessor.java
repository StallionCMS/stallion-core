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

import io.stallion.exceptions.AppException;
import io.stallion.exceptions.UsageException;
import io.stallion.settings.Settings;
import io.stallion.utils.ProcessHelper;
import io.stallion.utils.SimpleTemplate;
import org.apache.commons.io.FilenameUtils;
import org.parboiled.common.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static io.stallion.utils.Literals.*;

/**
 * Defines a pre-processor that uses an external command, such as a command-line
 * sass or coffeescript compiler in order to pre-process an asset file. This is superior
 * to using a watcher because it is guaranteed to always finish the processing before
 * rendering to the web server, thus you never get the problem where you edit a file,
 * reload, and then don't see your changes.
 */
public class ExternalCommandAssetPreProcessor {
    private String command;
    private String name;
    private String extension;
    private String[] commandArgs;



    /**
     * Finds the original file based on the processedFile path. If the original has new changes,
     * or the processed file does not exist, then execute process(originalPath, processedPath)
     *
     * @param compiledFile - the actual file to be included on the web page
     */
    public void processIfNeeded(File compiledFile) {
        String originalPath = FilenameUtils.removeExtension(compiledFile.getAbsolutePath()) + "." + extension;
        File original = new File(originalPath);
        if (!original.isFile()) {
            throw new UsageException("Could not find original " + extension + " file for compiled file " + compiledFile);
        }
        if (!compiledFile.exists()) {
            process(original, compiledFile);
        } else if (compiledFile.lastModified() < original.lastModified()) {
            process(original, compiledFile);
        }
    }

    /**
     *
     * @param original  - the original, unprocessed file (the scss file, or coffeescript file, etc.)
     * @param compiled - the file to be included on the web page (the compiled js or css file)
     */
    public void process(File original, File compiled) {
        if (empty(getCommand())) {
            throw new UsageException("Empty pre-processor command field");
        }
        ProcessHelper.CommandResult result;
        String[] argsArray = new SimpleTemplate(command)
                .put("original", original.getAbsolutePath())
                .put("compiled", compiled.getAbsolutePath())
                .render()
                .split(" ");
        result = new ProcessHelper().setInheritIO(false).setShowDotsWhileWaiting(false).run(argsArray);
        if (!result.succeeded()) {
            if (Settings.instance().getDebug() == true) {
                throw new UsageException("Could not pre-process file " + original + "\n\n\n" + result.getOut() + "\n\n\n" + result.getErr() + "\n\n");
            } else {
                throw new AppException("Could not pre-process file " + original);
            }
        }

    }

    public String getCommand() {
        return command;
    }

    public ExternalCommandAssetPreProcessor setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getName() {
        return name;
    }

    public ExternalCommandAssetPreProcessor setName(String name) {
        this.name = name;
        return this;
    }

    public String getExtension() {
        return extension;
    }

    public ExternalCommandAssetPreProcessor setExtension(String extension) {
        if (extension != null) {
            if (extension.startsWith(".")) {
                extension = extension.substring(1);
            }
        }
        this.extension = extension;
        return this;
    }

    public String[] getCommandArgs() {
        return commandArgs;
    }

    public ExternalCommandAssetPreProcessor setCommandArgs(String ...commandArgs) {
        this.commandArgs = commandArgs;
        return this;
    }
}
