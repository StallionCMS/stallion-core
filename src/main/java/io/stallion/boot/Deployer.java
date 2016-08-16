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

import io.stallion.Context;
import io.stallion.exceptions.CommandException;
import io.stallion.exceptions.UsageException;
import io.stallion.secrets.SecretsVault;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.settings.childSections.DeploymentsConfig;
import io.stallion.utils.ProcessHelper;
import io.stallion.utils.json.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

/**
 * The Publisher is an action that can be run from the command line. It publishes the
 * targetted Stallion web site to the remote server(s) configured in the stallion.toml
 *
 *
 */
class Deployer implements StallionRunAction<DeployCommandOptions> {
    private DeploymentsConfig config;
    private String deployEnv = "";
    private DeployCommandOptions options;

    @Override
    public String getActionName() {
        return "deploy";
    }

    @Override
    public String getHelp() {
        return "Deploys the stallion site to linux server host defined in the publishing config section of the stallion.toml";
    }

    @Override
    public void loadApp(DeployCommandOptions options) {
        AppContextLoader.loadWithSettingsOnly(options);
    }

    @Override
    public DeployCommandOptions newCommandOptions() {
        return new DeployCommandOptions();
    }

    @Override
    public void execute(DeployCommandOptions options) throws Exception {
        this.options = options;
        publish((DeployCommandOptions)options);
    }

    public void publish(DeployCommandOptions options) throws IOException, CommandException {
        List<DeploymentsConfig> configs = Context.getSettings().getDeploymentConfigs();

        if (configs.size() == 0) {
            throw new UsageException("You have no publishing configs set!");
        } else if (configs.size() == 1) {
            config = configs.get(0);
            deployEnv = or(config.getEnv(), "prod");
        } else {
            for (DeploymentsConfig aConfig : configs) {
                if (options.getEnv().equals(aConfig.getEnv())) {
                    config = aConfig;
                    deployEnv = config.getEnv();
                    break;
                }
            }
        }
        if (config == null) {
            if (empty(options.getEnv()) || "local".equals(options.getEnv())) {
                throw new CommandException("You did not pass in an environment to deploy to (-env=<env>) setting on the command line.");
            } else {
                throw new CommandException("No config found for the environment: \"" + newCommandOptions().getEnv()  + "\" ");
            }
        }
        if (empty(config.getHosts())) {
            throw new UsageException("Your publishing config has no hosts!");
        }
        for(String host: config.getHosts()) {
            doPublish(host);
        }


    }


    private void doPublish(String host) throws IOException, CommandException {
        String localFolder = Context.settings().getTargetFolder();
        String user = options.getUser();
        if (config != null) {
            if (empty(user)) {
                user = config.getUser();
            }
        }
        if (empty(user)) {
            user = System.getenv("USER");
        }
        if (empty(user)) {
            user = System.getProperty("user.name");
        }

        Log.info("Publish target folder {0} to host {1}", Context.settings().getTargetFolder(), host);
        if (config == null || empty(host) || empty(user)) {
            throw new CommandException("You must define a [publishing] section in your stallion.toml, and add a host=hostname key=value pair");
        }

        String remoteFolder = empty(config.getRootFolder()) ? "/home/" + config.getUser() + "/stallion-root" : config.getRootFolder();
        String wharfFolder = remoteFolder + "/wharf-upload";

        if (!localFolder.endsWith("/")) {
            localFolder += "/";
        }
        if (empty(remoteFolder) || remoteFolder.equals("/")) {
            throw new CommandException("Invalid remote folder: " + remoteFolder);
        }
        if (empty(config.getDomain())) {
            throw new CommandException("The publishing section must have a key=\"value\" pair for \"domain\"!");
        }

        if (new File(localFolder + ".git").isDirectory()) {
            if (true != options.isForce()) {
                ProcessHelper.CommandResult result = new
                        ProcessHelper("git", "--no-pager", "status", "--porcelain")
                        .withDirectory(localFolder)
                        .run();
                if (result.getCode() != 0 || !empty(result.getOut())) {
                    throw new CommandException("You have uncommitted changes to your git repo. There is danger that publishing could overwrite changes to someone else's work. Either commit your changes, or run this again with the -force option to ignore this warning.");
                }
                result = new ProcessHelper("git", "fetch", "origin", "master")
                        .withDirectory(localFolder)
                        .run();
                result = new ProcessHelper("git", "--no-pager", "diff", "origin/master...HEAD")
                        .withDirectory(localFolder)
                        .run();
                if (result.getCode() != 0 || !empty(result.getOut())) {
                    throw new CommandException("You have differences between your git repo and origin/master. There is danger that publishing could overwrite changes to someone else's work. Either pull the changes from origin, or run this again with the -force option to ignore this warning.");
                }

            }
        }



        File scriptsFolder = new File(localFolder);
        File publishScript = new File(localFolder + ".stallion-scripts/publish-site.ipy");
        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdir();
        }

        String scriptSource = IOUtils.toString(getClass().getResource("/publish-site.ipy"), UTF8);
        int redirectToSsl = 0;
        int redirectToPrimary = 0;
        if (config.getRedirectToSsl()) {
            redirectToSsl = 1;
        }

        int fullRebuild = 0;
        if (options.isFullRebuild()) {
            fullRebuild = 1;
        }

        Map conf = map(
                val("log_level", Log.getLogLevel().toString().toLowerCase()),
                val("host", host),
                val("env", deployEnv),
                val("site_url", Settings.instance().getSiteUrl()),
                val("redirect_to_ssl", redirectToSsl),
                val("full_rebuild", fullRebuild),
                val("alias_domains", config.getAliasDomains()),
                val("redirect_domains", config.getRedirectDomains()),
                val("check_urls", config.getCheckUrls()),
                val("domain", config.getDomain()),
                val("executable_name", Settings.instance().getExecutableName()),
                val("ssl_crt", or(config.getSslCertChain(), "")),
                val("ssl_key", or(config.getSslPrivateKey(), "")),
                val("base_port", config.getBasePort()));
        scriptSource = scriptSource.replace("#$$$conf$$$", "conf = " + JSON.stringify(conf));

        FileUtils.write(publishScript, scriptSource, UTF8);


        ProcessHelper.CommandResult result = ProcessHelper.run("ssh", user + "@" + host, "test -d " + wharfFolder);
        if (result.getCode() != 0) {
            // Create the wharf folder and set permissions, if it does not exist
            result = ProcessHelper.run("ssh", "-t", user + "@" + host,
                    "sudo groupadd -f stallion;" +
                    "sudo usermod -a -G stallion " + user + ";" +
                    "sudo mkdir -p " + wharfFolder + ";" +
                    "sudo chown " + user + ".stallion " + wharfFolder + ";" +
                    "sudo chmod 775 " + wharfFolder);
            if (result.getCode() != 0) {
                Log.warn("Deploy failed to create wharf folder. Exiting.");
                return;
            }
        }
        result = ProcessHelper.run(
                "rsync",
                "-r", // recursive
                "-p", // preserve permissions
                "-t", // preserve modification times
                "-g", // preserve group
                "-o", // preserve owner (super-user only)
                "-D", // preserve device files
                "--copy-links",
                "--specials", // preserve special files
                "--compress",
                "--verbose",
                "--delete",
                "--include",
                ".stallion-scripts",
                "--exclude",
                ".*",
                "--exclude",
                "app-data",
                "--exclude",
                "secrets.json",
                localFolder,
                user + "@" + host + ":" + wharfFolder);
        if (result.getCode() != 0) {
            Log.warn("Deploy failed to rsync local files to wharf folder. Exiting.");
            return;
        }

        // Decrypt and upload the secrets, if any
        Map<String, String> secrets = SecretsVault.loadIfExists(localFolder);
        if (!empty(secrets)) {
            String secretsJson = JSON.stringify(secrets);
            // ssh -T -e none "cat >/backup.tar.bz2"
            result = new ProcessHelper()
                    .run(
                            "ssh",
                            "-T",
                            "-e",
                            "none",
                            user + "@" + host,
                            "cat << HEREDOCEOF > " + wharfFolder + "/conf/secrets.json\n" + secretsJson + "\nHEREDOCEOF"
                );
            if (result.getCode() != 0) {
                Log.warn("Deploy failed to upload secrets file. Exiting.");
                return;
            }
        }
        result = new ProcessHelper(
                "ssh", "-t", user + "@" + host, "cd " + wharfFolder + "/.stallion-scripts;sudo ipython publish-site.ipy"
                ).setShowDotsWhileWaiting(false).run();
        if (result.getCode() != 0) {
            Log.warn("Deploy failed to publish site!");
            return;
        }

    }

}
