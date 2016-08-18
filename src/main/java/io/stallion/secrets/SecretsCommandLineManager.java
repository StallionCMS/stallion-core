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

package io.stallion.secrets;

import io.stallion.boot.CommandOptionsBase;
import io.stallion.services.Log;
import io.stallion.settings.childSections.SecretsSettings;
import io.stallion.utils.Prompter;
import net.east301.keyring.BackendNotSupportedException;
import net.east301.keyring.Keyring;
import net.east301.keyring.PasswordRetrievalException;
import net.east301.keyring.PasswordSaveException;
import net.east301.keyring.util.LockException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import static io.stallion.utils.Literals.*;


public class SecretsCommandLineManager {

    private String password = "";
    private SecretsVault vault;
    private String targetFolder = "";
    private boolean keyringAccesible = false;
    private boolean inKeyChain = false;
    private String keyringServiceName = "";
    private static final String keyringAccountName = "stallion";

    private static final String lock = "            .-\"\"-.\n" +
            "           / .--. \\\n" +
            "          / /    \\ \\\n" +
            "          | |    | |\n" +
            "          | |.-\"\"-.|\n" +
            "         ///`.::::.`\\\n" +
            "        ||| ::/  \\:: ;\n" +
            "        ||; ::\\__/:: ;\n" +
            "         \\\\\\ '::::' /\n" +
            "          `=':-..-'`";

    public SecretsVault loadVault(String appPath, SecretsSettings secretsSettings) {
        targetFolder = appPath;
        keyringServiceName = "Stallion Secrets Key: " + targetFolder;

        if (secretsSettings == null) {
            Log.info("secretsSettings is null");
        } else {
            Log.info("Secrets passphrase file is {0}", secretsSettings.getPassPhraseFile());
        }
        password = findPasswordFromFile(secretsSettings);

        if (empty(password)) {
            password = findPasswordInKeyring();
        }

        if (empty(password)) {
            password = new Prompter("What is your encryption password? If you are creating a secrets file for the first time," +
                    "generate a new random string of at least 16 alpha-numeric characters, save it somewhere safe " +
                    "(like your password manager), and then paste it in here. This password is not recoverable. If you lost it, " +
                    "your secrets file will be locked forever.\nEnter your encryption password: ") {
                @Override
                public boolean validate(String line) {
                    if (line.length() < 15) {
                        this.lastErrorMessage = "Your password must be at least 20 characters.";
                        return false;
                    }
                    return true;
                }
            }.prompt();
        }


        vault = new SecretsVault(targetFolder, password);

        promptStorePassword(password);
        return vault;
    }

    public void start(CommandOptionsBase options) {
        System.out.print("\n\n" + lock + "\n\n");
        System.out.println("Welcome to the Stallion Secrets Manager.\n\n You can view your secrets, create new secrets, change secrets, delete secrets");

        targetFolder = options.getTargetPath();
        loadVault(options.getTargetPath(), null);

        while(true) {
            String line = new Prompter("What do you want to do? Options: new/edit/delete/list/quit ")
                    .setChoices("new", "edit", "delete", "list", "quit")
                    .prompt();
            switch (line) {
                case "quit":
                    return;
                case "new":
                    newSecret();
                    break;
                case "delete":
                    deleteSecrets();
                    break;
                case "list":
                    listSecrets();
                    break;
                case "edit":
                    editSecret();
                    break;
                default:
                    continue;
            }
        }

    }

    public String findPasswordFromFile(SecretsSettings secretsSettings) {
        if (secretsSettings == null) {
            return "";
        }
        String path = or(secretsSettings.getPassPhraseFile(), "/usr/local/etc/stallion-secrets-passphrase");
        Log.info("Looking for secrets pass phrase in file {0}", path);
        File passPhraseFile = new File(path);

        if (!passPhraseFile.exists()) {
            return "";
        }
        try {
            return FileUtils.readFileToString(passPhraseFile, UTF8).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String findPasswordInKeyring() {

        Keyring keyring;
        try {
            keyring = Keyring.create();
        } catch (BackendNotSupportedException ex) {
            Log.info("Backend not supported for storing passphrase in Keyring");
            return "";
        }

        keyringAccesible = true;


        // some backend directory handles a file to store password to disks.
        // in this case, we must set path to password store file by Keyring.setKeyStorePath
        // before using Keyring.getPassword and Keyring.getPassword.
        if (keyring.isKeyStorePathRequired()) {
            try {
                File keyStoreFile = File.createTempFile("keystore", ".keystore");
                keyring.setKeyStorePath(keyStoreFile.getPath());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        //
        // Retrieve password from password store
        //

        // Password can be retrieved by using Keyring.getPassword method.
        // PasswordRetrievalException is thrown when some error happened while getting password.
        // LockException is thrown when keyring backend failed to lock password store file.
        try {
            String password = keyring.getPassword(keyringServiceName, keyringAccountName);
            if (!empty(password)) {
                inKeyChain = true;
            }
            return password;
        } catch (LockException ex) {
            throw new RuntimeException(ex);
        } catch (PasswordRetrievalException ex) {
            Log.info("Could not retrieve passphrase from the keychain");
            return "";
        }
    }

    public void promptStorePassword(String password) {
        if (!keyringAccesible) {
            return;
        }
        if (inKeyChain) {
            return;
        }
        boolean shouldStore = new Prompter("Store this passphrase in your local keychain? (Y/n) ").yesNo();
        if (!shouldStore) {
            Log.info("Not should store!");
            return;
        }


        Keyring keyring;
        try {
            keyring = Keyring.create();
        } catch (BackendNotSupportedException ex) {
            Log.info("Backend not supported for storing passphrase in Keyring");
            return;
        }

        // some backend directory handles a file to store password to disks.
        // in this case, we must set path to password store file by Keyring.setKeyStorePath
        // before using Keyring.getPassword and Keyring.getPassword.
        if (keyring.isKeyStorePathRequired()) {
            try {
                File keyStoreFile = File.createTempFile("keystore", ".keystore");
                keyring.setKeyStorePath(keyStoreFile.getPath());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        // Password can be stored to password store by using Keyring.setPassword method.
        // PasswordSaveException is thrown when some error happened while saving password.
        // LockException is thrown when keyring backend failed to lock password store file.
        try {
            keyring.setPassword(keyringServiceName, keyringAccountName, password);
        } catch (LockException ex) {
            throw new RuntimeException(ex);
        } catch (PasswordSaveException ex) {
            throw new RuntimeException(ex);
        }


    }

    public void newSecret() {
        String name = new Prompter("Secret name (You will use this name in your settings file to retrieve the secret): ")
                .prompt();
        String value = new Prompter("Secret value: ")
                .prompt();
        vault.add(name, value);
        vault.save();
        System.out.println("Secret added.");

    }

    public void editSecret() {
        String name = new Prompter("Secret name: ")
                .setChoices(vault.getSecretNames())
                .prompt();
        String value = new Prompter("Secret value: ")
                .prompt();
        vault.update(name, value);
        vault.save();
        System.out.println("Secret updated.");

    }
    public void listSecrets() {
        MessageFormat format = new MessageFormat("{0}: {1}");
        System.out.print("\n");
        if (vault.getSecretNames().size() == 0) {
            System.out.println("No secrets defined.");
        }
        for(String name: vault.getSecretNames()) {
            System.out.println(format.format("{0}: {1}", name, vault.getSecret(name)));
        }
        System.out.print("\n");
    }
    public void deleteSecrets() {
        String name = new Prompter("Secret name: ")
                .setChoices(vault.getSecretNames())
                .prompt();
        vault.getSecrets().remove(name);
        vault.save();
        System.out.println("Secret deleted.");
    }


}
