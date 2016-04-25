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

package io.stallion.boot;

import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.templating.JinjaTemplating;
import io.stallion.users.UserAdder;
import io.stallion.utils.Prompter;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;

import static io.stallion.utils.Literals.*;


public class NewProjectBuilder implements StallionRunAction<CommandOptionsBase> {
    Scanner scanner = new Scanner (System.in);
    private String targetFolder = "";
    private CommandOptionsBase options;
    private JinjaTemplating templating;

    @Override
    public String getActionName() {
        return "new";
    }

    @Override
    public String getHelp() {
        return "Create a new Stallion website";
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
        String folder = or(options.getTargetPath(), Paths.get(".").toAbsolutePath().normalize().toString());
        targetFolder = folder;
        this.options = options;
        templating = new JinjaTemplating(targetFolder, false);

        File dir = new File(folder);
        if (!dir.isDirectory()) {
            throw new UsageException("The target folder does not exist: " + folder);
        }
        File confDir = new File(folder + "/conf");
        if (confDir.exists()) {
            throw new UsageException("You have already initialized the folder " + folder);
        }
        String msg = "Choose the starting scaffolding: \n" +
                "1) Barebones\n" +
                "2) Simple Text Site\n" +
                "3) Javascript Site\n\nChoose a number: ";
        String choice = new Prompter(msg).setChoices("1", "2", "3").prompt();
        if ("1".equals(choice)) {
            makeBareBonesSite(folder);
        } else if ("2".equals(choice)) {
            makeSimpleTextSite(folder);
        } else if ("3".equals(choice)) {
            makeJavascriptSite(folder);
        } else {
            throw new UsageException("Invalid choice " + choice);
        }


    }

    protected void makeBareBonesSite(String folder) throws Exception {
        makeStandardConf();
        makeTemplates();
        makeAssets();
        makePages();
        boolean shouldMakeUser = new Prompter("Do you want to create an admin user right now? This is needed to use some of the internal tools from the web. You can do this later by running >stallion user-add. (y/n)? ").yesNo();
        if (shouldMakeUser) {
            makeUser();
        }
        System.out.printf("\n\nYour site is now complete! You can test it out by running >bin/stallion serve\n\n");
    }

    protected void makeSimpleTextSite(String folder) throws Exception {
        makeBareBonesSite(folder);

    }

    protected void makeJavascriptSite(String folder) throws Exception {
        makeSimpleTextSite(folder);
        removeFile("pages/home.txt");
        makeJs();
        copyFile("/templates/wizard/app.jinja", "templates/app.jinja");
    }

    protected void makeStandardConf() throws IOException {
        File file = new File(targetFolder + "/conf");
        if (!file.isDirectory()) {
            new File(targetFolder).mkdirs();
        }
        ProjectSettingsBuilder builder = new ProjectSettingsBuilder()
                .setHealthCheckSecret(GeneralUtils.randomToken(20))
                .setSiteName(new Prompter("What is the name of your new site? ").prompt())
                .setSiteDescription(new Prompter("What is a one or two sentence description of your site? ").prompt())
                .setSiteUrl(new Prompter("What is the URL that this site will be publicly available at? ").prompt())
                .setTitle(Prompter.prompt("What is the title for your site? "));
        String source = templating.renderTemplate(
                IOUtils.toString(getClass().getResource("/templates/wizard/stallion.toml.jinja")),
                map(val("builder", builder)));
        String prodSource = templating.renderTemplate(
                IOUtils.toString(getClass().getResource("/templates/wizard/stallion.prod.toml.jinja")),
                map(val("builder", builder)));
        FileUtils.writeStringToFile(new File(targetFolder + "/conf/stallion.toml"), source, "UTF-8");
        FileUtils.writeStringToFile(new File(targetFolder + "/conf/stallion.prod.toml"), prodSource, "UTF-8");
    }

    protected void makePages() throws IOException {
        File f = new File(targetFolder + "/pages");
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        copyFile("/templates/wizard/home.txt", "pages/home.txt");
        copyFile("/templates/wizard/about.txt", "pages/about.txt");
    }

    protected void makeTemplates() throws IOException {
        File f = new File(targetFolder + "/templates");
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        copyFile("/templates/wizard/page.jinja", "templates/page.jinja");
        //copyFile("/templates/wizard/home.jinja", "templates/home.jinja");
        copyFile("/templates/wizard/base.jinja", "templates/base.jinja");
    }

    protected void removeFile(String relativePath) throws IOException {
        File file = new File(targetFolder + "/" + relativePath);
        if (file.isFile()) {
            file.delete();
        }
    }

    protected void makeAssets() throws IOException {
        File f = new File(targetFolder + "/assets");
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        copyFile("/templates/wizard/site.css", "assets/site.css");
        copyFile("/templates/wizard/site.js", "assets/site.js");
        copyFile("/templates/wizard/site.bundle.css", "assets/site.bundle.css");
        copyFile("/templates/wizard/site.bundle.js", "assets/site.bundle.js");

    }

    protected void makeUser() throws Exception {
        Log.setLogLevel(Level.WARNING);
        AppContextLoader.loadCompletely(options);
        UserAdder adder = new UserAdder();
        adder.execute(options, "new");
    }

    public void makeJs() throws Exception {
        File file = new File(targetFolder + "/js");
        file.mkdirs();
        copyFile("/templates/wizard/main.js", "js/main.js");
    }

    public void copyFile(String resourcePath, String relativePath) throws IOException {
        File file = new File(targetFolder + "/" + relativePath);
        String source = IOUtils.toString(getClass().getResource(resourcePath));
        FileUtils.writeStringToFile(file, source, "utf-8");
    }




}