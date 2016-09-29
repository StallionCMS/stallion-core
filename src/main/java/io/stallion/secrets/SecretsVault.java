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



import com.fasterxml.jackson.core.type.TypeReference;
import io.stallion.exceptions.UsageException;
import io.stallion.settings.childSections.SecretsSettings;
import io.stallion.utils.Encrypter;
import io.stallion.utils.json.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class SecretsVault {
    private String passPhrase;
    private String secretsPath = "";
    private static SecretsSettings secretsSettings;

    private HashMap<String, String> secrets = new HashMap<>();

    private static String appPath = "";
    private static Map<String, String> appSecrets;

    public static Map<String, String> getAppSecrets() {
        if (empty(appPath)) {
            throw new UsageException("You cannot call getAppSecrets() before init() is called");
        }
        if (appSecrets == null) {
            appSecrets = loadIfExists(appPath);
            if (appSecrets == null) {
                appSecrets = new HashMap<>();
            }
        }
        return appSecrets;
    }

    public static void init(String theAppPath, SecretsSettings theSecretsSettings) {
        // We have to pass in the application path, since we cannot rely on Settings.instance() being available.
        // However, we do not want to actually load the secrets vault, since that adds complexity and overhead
        // in circumstances when it may not even being necessary (such as when running locally when there are no
        // production keys). So we lazy-load the actual vault when it is first requested.
        appPath = theAppPath;
        secretsSettings = theSecretsSettings;
    }


    public static Map<String, String> loadIfExists(String appPath) {
        String rawPath = appPath + "/conf/secrets.json";
        String encryptedPath = appPath + "/conf/secrets.json.aes";
        File rawFile = new File(rawPath);
        if (rawFile.isFile()) {
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
            String json = null;
            try {
                json = FileUtils.readFileToString(rawFile, UTF8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return JSON.parse(json, typeRef);
        }
        if (new File(encryptedPath).isFile()) {
            // Get passphrase
            SecretsVault vault = new SecretsCommandLineManager().loadVault(appPath, secretsSettings);
            if (vault != null) {
                return vault.getSecrets();
            }

        }
        return null;
    }

    public SecretsVault(String appPath, String passPhrase) {
        this.passPhrase = passPhrase;
        if (passPhrase.length() < 16) {
            throw new UsageException("Your passPhrase is not long enough!");
        }
        secretsPath = appPath + "/conf/secrets.json.aes";
        File file = new File(secretsPath);
        if (!file.isFile()) {
            secrets = new HashMap<>();
            save();
        } else {
            try {
                String encrypted = FileUtils.readFileToString(file, UTF8);
                encrypted = encrypted.replace("\n", "");
                secrets = decryptAndParse(encrypted);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public String secretsToJson() {
        return JSON.stringify(getSecrets());
    }

    public HashMap<String, String> getSecrets() {
        return secrets;
    }

    public List<String> getSecretNames() {
        List<String> secretNames = new ArrayList<String>(secrets.keySet());
        secretNames.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        return secretNames;
    }

    public String getSecret(String name) {
        return secrets.get(name);
    }

    public SecretsVault add(String name, String value) {
        if (empty(name)) {
            throw new UsageException("Name is empty.");
        }
        if (secrets.containsKey(name)) {
            throw new UsageException("Secrets vault already contains secret with name " + name);
        }
        secrets.put(name, value);
        return this;
    }

    public SecretsVault update(String name, String value) {
        if (!secrets.containsKey(name)) {
            throw new UsageException("No secret with name '" + name + "' exists");
        }
        secrets.put(name, value);
        return this;
    }

    public HashMap<String,String> decryptAndParse(String encrypted) {
        TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
        String json = Encrypter.decryptString(passPhrase, encrypted);
        secrets = JSON.parse(json, typeRef);
        return secrets;
    }

    public String dumpAndEncrypt() {
        return WordUtils.wrap(Encrypter.encryptString(passPhrase, JSON.stringify(secrets)), 80, "\n", true);
    }

    public void save() {
        Long version = Long.parseLong(secrets.getOrDefault("version", "1")) + 1L;
        secrets.put("version", version.toString());
        String encrypted = dumpAndEncrypt();
        File file = new File(secretsPath);
        try {
            FileUtils.write(file, encrypted, UTF8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
