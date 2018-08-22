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

import io.stallion.boot.StallionRunAction;
import org.apache.commons.io.FileUtils;

import java.io.File;

import static io.stallion.utils.Literals.UTF8;


public class SecretsDecryptAction implements StallionRunAction<SecretsDecryptOptions> {
    @Override
    public String getActionName() {
        return "secrets-decrypt";
    }

    @Override
    public String getHelp() {
        return "Decrypts the file conf/secrets.json.aes to conf/secrets.json";
    }

    @Override
    public void loadApp(SecretsDecryptOptions options) {

    }


    @Override
    public SecretsDecryptOptions newCommandOptions() {
        return new SecretsDecryptOptions();
    }

    @Override
    public void execute(SecretsDecryptOptions options) throws Exception {

        SecretsCommandLineManager manager = new SecretsCommandLineManager();
        SecretsVault vault = manager.loadVault(options.getTargetPath(), null, options.getPassphrase());
        String secretsJson = vault.secretsToJson();
        File file = new File(options.getTargetPath() + "/conf/secrets.json");
        FileUtils.write(file, secretsJson, UTF8);


    }


}
