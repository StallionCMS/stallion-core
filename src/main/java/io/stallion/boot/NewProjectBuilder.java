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
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;

import static io.stallion.utils.Literals.*;


public class NewProjectBuilder implements StallionRunAction<CommandOptionsBase> {
    Scanner scanner = new Scanner (System.in);
    private String targetFolder = "";
    private CommandOptionsBase options;
    private JinjaTemplating templating;
    private ProjectSettingsBuilder builder;

    private boolean shouldMakePages = false;
    private boolean shouldMakeTemplates = false;
    private boolean shouldMakeAssets = false;
    private boolean shouldMakeBlog = false;

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

    public boolean requireDatabase() {
        return false;
    }

    @Override
    public CommandOptionsBase newCommandOptions() {
        return new CommandOptionsBase();
    }

    protected void init(CommandOptionsBase options) {
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

    }

    @Override
    public void execute(CommandOptionsBase options) throws Exception {
        init(options);
        String msg = "Choose the starting scaffolding: \n" +
                "1) Barebones\n" +
                "2) Text File Web Site\n" +
                "3) Javascript Application\n" +
                "4) Java Application" +
                "\n\nChoose a number: ";
        String choice = new Prompter(msg).setChoices("1", "2", "3", "4").prompt();
        if ("1".equals(choice)) {
            makeBareBonesSite(getTargetFolder());
        } else if ("2".equals(choice)) {
            makeSimpleTextSite(getTargetFolder());
        } else if ("3".equals(choice)) {
            makeJavascriptSite(getTargetFolder());
        } else if ("4".equals(choice)) {
            makeJavaApplication(getTargetFolder());
        } else {
            throw new UsageException("Invalid choice " + choice);
        }


    }

    protected void makeBasicScaffold(String folder) throws Exception {
        makeStandardConf();
        copyFile("/templates/wizard/deployment.toml", "conf/deployment.toml");
        copyFile("/templates/wizard/hosts.toml", "conf/hosts.toml");

        if (shouldMakeTemplates) {
            makeTemplates();
        }
        if (shouldMakeAssets) {
            makeAssets();
        }
        if (shouldMakePages) {
            makePages();
        }

        if (shouldMakeBlog) {
            copyFile("/templates/wizard/post.jinja", "templates/post.jinja");
            copyFile("/templates/wizard/listing.jinja", "templates/listing.jinja");
            makePosts();
        }


        boolean shouldMakeUser = new Prompter("Do you want to create an admin user right now? This is needed to use some of the internal tools from the web. You can do this later by running >stallion user-add. (Y/n)? ").yesNo();
        if (shouldMakeUser) {
            makeUser();
        }


    }

    protected void makeBareBonesSite(String folder) throws Exception {
        shouldMakeTemplates = true;
        shouldMakePages = true;
        shouldMakeAssets = true;
        makeBasicScaffold(folder);
        System.out.printf("\n\nYour site is now complete! You can test it out by running >bin/stallion serve\n\n");
    }

    protected void makeSimpleTextSite(String folder) throws Exception {
        shouldMakeBlog = true;
        shouldMakeTemplates = true;
        shouldMakePages = true;
        shouldMakeAssets = true;
        makeBasicScaffold(folder);
        System.out.printf("\n\nYour site is now complete! You can test it out by running >bin/stallion serve\n\n");

    }

    protected void makeJavascriptSite(String folder) throws Exception {
        shouldMakeTemplates = true;
        shouldMakePages = true;
        shouldMakeAssets = true;
        makeBasicScaffold(folder);
        removeFile("pages/home.txt");
        makeJs();
        copyFile("/templates/wizard/app.jinja", "templates/app.jinja");
        System.out.printf("\n\nYour site is now complete! You can test it out by running >bin/stallion serve\n\n");
    }

    protected void makeJavaApplication(String folder) throws Exception {
        targetFolder = targetFolder + "/site";
        templating = new JinjaTemplating(targetFolder, false);

        File file = new File(targetFolder);
        if (!file.isDirectory()) {
            new File(targetFolder).mkdirs();
        }
        if (new File(targetFolder + "/site/conf").exists()) {
            throw new UsageException("You have already initialized an application at this location.");
        }

        // Make the simple site
        shouldMakeTemplates = false;
        shouldMakePages = false;
        shouldMakeAssets = false;
        makeBasicScaffold(folder);
        removeFile("pages/home.txt");

        NewJavaPluginRunAction javaAction = new NewJavaPluginRunAction();
        file = new File(folder + "/java-app");
        if (!file.exists()) {
            file.mkdirs();
        }
        javaAction.makeNewApp(folder + "/java-app");
        System.out.printf("\n\nYour site is now complete! Run it by going to " + folder + "/java-app and running ./run-dev.sh");
    }

    protected void makeStandardConf() throws IOException {
        File file = new File(targetFolder + "/conf");
        if (!file.isDirectory()) {
            new File(targetFolder).mkdirs();
        }
        builder = new ProjectSettingsBuilder()
                .setMakeBlog(shouldMakeBlog)
                .setHealthCheckSecret(GeneralUtils.randomToken(20))
                .setSiteName(new Prompter("What is the name of your new site? ").prompt())
                .setSiteDescription(new Prompter("What is a one or two sentence description of your site? ").prompt())
                .setSiteUrl(new Prompter("What is the URL that this site will be publicly available at? ").prompt())
                .setTitle(Prompter.prompt("What is the title for your site? "));
        builder.setHighlightColor(or(new Prompter("Choose a (hex) highlight color for default pages and emails. Leave empty to default to blue (#2184A5) ")
                .setValidPattern("(#[a-fA-F0-9]{6,6}|)")
                .setAllowEmpty(true).prompt(), "#2184A5"));
        boolean configureEmails = new Prompter("Do you want to configure email sending? Configuring email will allow " +
                "your Stallion application to email you exceptions, form submissions, comments, et cetera. " +
                "You will need the username, password, and host for an SMTP server. You can get" +
                "a free email sending account from a service like Sendgrid, Postmark, or Mailgun. Configure email sending? (Y/n) ").yesNo();
        if (configureEmails) {
            builder.setEmailHost(Prompter.prompt("Email host? "));
            builder.setEmailPort(Long.parseLong(or(new Prompter("Email port? (Default is 587) ").setAllowEmpty(true).prompt(), "587")));
            builder.setAdminEmail(new Prompter("Administrator email address (will get exception messages, etc.)? ").setValidPattern(".+@.+").prompt());
            builder.setEmailPassword(Prompter.prompt("Email password? "));
            builder.setEmailUsername(Prompter.prompt("Email username? "));
        }
        boolean configureDatabase = requireDatabase();
        if (!configureDatabase) {
            configureDatabase = new Prompter("Configure a local database? (Y/n) ").yesNo();
        }
        if (configureDatabase) {
            builder.setDatabaseUrl(Prompter.prompt("Database URL?" ));
            builder.setDatabaseUsername(Prompter.prompt("Database username? "));
            builder.setDatabasePassword(new Prompter("Database password?" ).setAllowEmpty(true).prompt());
        }




        String source = templating.renderTemplate(
                IOUtils.toString(getClass().getResource("/templates/wizard/stallion.toml.jinja"), UTF8),
                map(val("builder", builder)));
        String prodSource = templating.renderTemplate(
                IOUtils.toString(getClass().getResource("/templates/wizard/stallion.prod.toml.jinja"), UTF8),
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
        copyFile("/templates/wizard/contact-us.txt", "pages/contact-us.txt");
    }

    protected void makePosts() throws IOException {
        File f = new File(targetFolder + "/posts");
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        copyFile("/templates/wizard/first-post.txt", "posts/first-post.txt");
        copyFile("/templates/wizard/second-post.txt", "posts/second-post.txt");
    }

    protected void makeTemplates() throws IOException {
        File f = new File(targetFolder + "/templates");
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        copyFile("/templates/wizard/page.jinja", "templates/page.jinja");
        copyFile("/templates/wizard/contact-us.jinja", "templates/contact-us.jinja");
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
        String colorsCss = "a {\n" +
                "    color: " + builder.getHighlightColor() + ";\n" +
                "}\n" +
                ".sidebar, .pure-button-primary {\n" +
                "    background: " + builder.getHighlightColor() + ";\n" +
                "}";
        replaceString("/assets/site.css", "/**--highlight-color-section--*/", colorsCss);
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
        String source = IOUtils.toString(getClass().getResource(resourcePath), UTF8);
        FileUtils.writeStringToFile(file, source, "utf-8");
    }


    protected void replaceString(String relativePath, String old, String newString) {
        File file = new File(getTargetFolder() + "/" + relativePath);

        try {
            String content = FileUtils.readFileToString(file, UTF8);
            content = content.replace(old, newString);
            FileUtils.write(file, content, Charset.forName("utf-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public Scanner getScanner() {
        return scanner;
    }

    public NewProjectBuilder setScanner(Scanner scanner) {
        this.scanner = scanner;
        return this;
    }

    protected String getTargetFolder() {
        return targetFolder;
    }

    protected NewProjectBuilder setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
        return this;
    }

    protected CommandOptionsBase getOptions() {
        return options;
    }

    protected NewProjectBuilder setOptions(CommandOptionsBase options) {
        this.options = options;
        return this;
    }

    protected JinjaTemplating getTemplating() {
        return templating;
    }

    protected NewProjectBuilder setTemplating(JinjaTemplating templating) {
        this.templating = templating;
        return this;
    }
}