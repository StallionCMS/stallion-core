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

import io.stallion.templating.JinjaTemplating;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static io.stallion.utils.Literals.*;

/**
 * A command-line StallionRunAction that will walk the user through generating the
 * scaffolding for a brand new Stallion java plugin, including setting up the maven
 * pom.xml and a directory structure.
 */
public class NewJavaPluginRunAction implements StallionRunAction<CommandOptionsBase> {
    Scanner scanner = new Scanner (System.in);
    private String targetFolder = "";
    private CommandOptionsBase options;
    private JinjaTemplating templating;

    private String pluginName = "";
    private String pluginNameTitleCase = "";
    private String javaPackageName = "";
    private String groupId = "";
    private String artifactId = "";


    @Override
    public String getActionName() {
        return "new-java-plugin";
    }

    @Override
    public String getHelp() {
        return "Create a stallion plugin with a maven pom.xml";
    }

    @Override
    public void loadApp(CommandOptionsBase options) {

    }

    @Override
    public CommandOptionsBase newCommandOptions() {
        return new CommandOptionsBase();
    }

    @Override
    public void execute(CommandOptionsBase options) throws Exception {
        this.options = options;
        String folder = or(options.getTargetPath(), Paths.get(".").toAbsolutePath().normalize().toString());
        makeNewApp(folder);
    }
    public void makeNewApp(String javaFolder) throws Exception {
        targetFolder = javaFolder;

        templating = new JinjaTemplating(targetFolder, false);


        setGroupId(promptForInputOfLength("What is the maven group name? ", 5));
        setPluginName(promptForInputOfLength("What is the plugin package name (will be appended to the groupid)? ", 2));
        setArtifactId(promptForInputOfLength("What maven artifact id? ", 5));


        setPluginNameTitleCase(getPluginName().substring(0, 1).toUpperCase() + getPluginName().substring(1));
        setJavaPackageName(getGroupId() + "." + getPluginName());


        File dir = new File(javaFolder);
        if (!dir.isDirectory()) {
            FileUtils.forceMkdir(dir);
        }


        String sourceFolder = "/src/main/java/" + getJavaPackageName().replaceAll("\\.", "/");
        List<String> paths = list(
                targetFolder + "/src/main/resources",
                targetFolder + "/src/main/resources/sql",
                targetFolder + "/src/test/resources",
                targetFolder + "/src/main/java/" + getJavaPackageName().replaceAll("\\.", "/"),
                targetFolder + "/src/test/java/" + getJavaPackageName().replaceAll("\\.", "/")
        );
        for(String path: paths) {
            File file = new File(path);
            if (!file.isDirectory()) {
                FileUtils.forceMkdir(file);
            }
        }
        new File(targetFolder + "/src/main/resources/sql/migrations.txt").createNewFile();

        Map ctx = map(val("config", this));
        copyTemplate("/templates/wizard/pom.xml.jinja", "pom.xml", ctx);
        copyTemplate("/templates/wizard/PluginBooter.java.jinja", sourceFolder + "/" + pluginNameTitleCase + "Plugin.java", ctx);
        copyTemplate("/templates/wizard/PluginSettings.java.jinja", sourceFolder + "/" + pluginNameTitleCase + "Settings.java", ctx);
        copyTemplate("/templates/wizard/MainRunner.java.jinja", sourceFolder + "/MainRunner.java", ctx);
        copyTemplate("/templates/wizard/Endpoints.java.jinja", sourceFolder + "/Endpoints.java", ctx);
        copyTemplate("/templates/wizard/build.py.jinja", "build.py", ctx);
        copyTemplate("/templates/wizard/run-dev.jinja", "run-dev.sh", ctx);

        Files.setPosixFilePermissions(FileSystems.getDefault().getPath(targetFolder + "/build.py"),
                set(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
        );
        Files.setPosixFilePermissions(FileSystems.getDefault().getPath(targetFolder + "/run-dev.sh"),
                set(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
        );
        copyFile("/templates/wizard/app.bundle", "src/main/resources/assets/app.bundle");
        copyFile("/templates/wizard/app.js", "src/main/resources/assets/app.js");
        copyFile("/templates/wizard/app.scss", "src/main/resources/assets/app.scss");
        copyFile("/templates/wizard/file1.js", "src/main/resources/assets/common/file1.js");
        copyFile("/templates/wizard/file2.js", "src/main/resources/assets/common/file2.js");
        copyFile("/assets/vendor/jquery-1.11.3.js", "src/main/resources/assets/vendor/jquery-1.11.3.js");
        copyFile("/assets/basic/stallion.js", "src/main/resources/assets/vendor/stallion.js");
        copyFile("/templates/wizard/app.jinja", "src/main/resources/templates/app.jinja");

    }



    public void copyTemplate(String resourcePath, String relativePath, Map ctx) throws IOException  {
        String source = templating.renderTemplate(
                IOUtils.toString(getClass().getResource(resourcePath), UTF8),
                ctx);
        File file = new File(targetFolder + "/" + relativePath);
        File parent = file.getParentFile();
        if (!parent.isDirectory()) {
            parent.mkdirs();
        }
        FileUtils.writeStringToFile(file, source, "utf-8");
    }

    public void copyFile(String resourcePath, String relativePath) throws IOException {
        File file = new File(targetFolder + "/" + relativePath);
        File parent = file.getParentFile();
        if (!parent.isDirectory()) {
            parent.mkdirs();
        }
        String source = IOUtils.toString(getClass().getResource(resourcePath), UTF8);
        FileUtils.writeStringToFile(file, source, "utf-8");
    }



    public boolean promptYesNo(String prompt) {
        System.out.print(prompt + " Answer yes/no or y/n: ");
        for (int i : safeLoop(100)) {
            String input = scanner.next().trim().toLowerCase();
            if (input.startsWith("y")) {
                return true;
            } else if (input.startsWith("n")) {
                return false;
            }
            System.out.println("Please answer y or n.");
        }
        return false;
    }

    public String promptForInputOfLength(String prompt, int length) {
        System.out.print(prompt);
        for (int i : safeLoop(100)) {
            String input = scanner.next().trim();
            if (input.length() >= length) {
                return input;
            }
            System.out.println("Input must be at least" + length + " characters.");
        }
        return "";
    }

    public String promptForInputOfLength(String prompt, int length, String contains) {
        System.out.print(prompt);
        for (int i : safeLoop(100)) {
            String input = scanner.next().trim();
            if (input.length() >= length && input.contains(contains)) {
                return input;
            }
            System.out.println("Input must be at least" + length + " characters and contain " + contains);
        }
        return "";
    }


    public String promptForValidInput(String prompt, String...validChoices) {
        System.out.print(prompt);
        for (int i: safeLoop(100)) {
            String input = scanner.next().trim();
            if (ArrayUtils.contains(validChoices, input)) {
                return input;
            }
            System.out.println("Invalid choice!");
        }
        return "";
    }


    public String getPluginName() {
        return pluginName;
    }

    public NewJavaPluginRunAction setPluginName(String pluginName) {
        this.pluginName = pluginName;
        return this;
    }

    public String getJavaPackageName() {
        return javaPackageName;
    }

    public NewJavaPluginRunAction setJavaPackageName(String javaPackageName) {
        this.javaPackageName = javaPackageName;
        return this;
    }

    public String getPluginNameTitleCase() {
        return pluginNameTitleCase;
    }

    public NewJavaPluginRunAction setPluginNameTitleCase(String pluginNameTitleCase) {
        this.pluginNameTitleCase = pluginNameTitleCase;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public NewJavaPluginRunAction setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public NewJavaPluginRunAction setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }
}
