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

package io.stallion.users;

import io.stallion.boot.AppContextLoader;
import io.stallion.Context;
import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.StallionRunAction;
import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.GeneralUtils;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Console;
import java.util.Scanner;

import static io.stallion.utils.Literals.*;

/**
 * Command line action for adding a predfined user. User will be added as a flat-file configuration and included as a user
 * anywhere the application is deployed.
 */
public class UserAdder implements StallionRunAction<CommandOptionsBase> {
    @Override
    public String getActionName() {
        return "users";
    }

    @Override
    public String getHelp() {
        return "Add admin users or edit existing users";
    }

    @Override
    public void loadApp(CommandOptionsBase options) {
        AppContextLoader.loadCompletely(options);
    }

    public void execute(CommandOptionsBase options) throws Exception {
        execute(options, "");
    }
    public void execute(CommandOptionsBase options, String action) throws Exception {


        Log.info("Settings: siteName {0} email password {1}", Settings.instance().getSiteName(), Settings.instance().getEmail().getPassword());

        Scanner scanner = new Scanner(System.in);
        Console console = System.console();

        if (empty(action)) {
            //System.out.print("Create new user or edit existing? (new/edit): ");

            //String newEdit = scanner.next();

            System.out.print("Create new user or edit existing? (new/edit): ");
            //String newEdit = console.readLine("Create new user or edit existing? (new/edit): ");
            action = scanner.nextLine();
        }
        User user = null;
        if ("new".equals(action)) {
            user = new User();
            user.setPredefined(true);
        } else if("edit".equals(action)) {
            System.out.print("Enter the email, username, or ID of the user you wish to edit:");
            String idMaybe = scanner.next();
            if (StringUtils.isNumeric(idMaybe)) {
                user = (User)UserController.instance().forId(Long.parseLong(idMaybe));
            }
            if (user == null) {
                user = (User)UserController.instance().forUniqueKey("email", idMaybe);
            }
            if (user == null) {
                user = (User)UserController.instance().forUniqueKey("username", idMaybe);
            }
            if (user == null) {
                System.out.print("Could not find user for key: " + idMaybe);
                System.exit(1);
            }
        } else {
            System.out.print("Invalid choice. Choose either 'new' or 'edit'");
            System.exit(1);
        }

        System.out.print("User's given name: ");
        String givenName = scanner.nextLine();
        if (!empty(givenName)) {
            user.setGivenName(givenName);
        }

        System.out.print("User's family name: ");
        String familyName = scanner.nextLine();
        if (!empty(familyName)) {
            user.setFamilyName(familyName);
            user.setDisplayName(user.getGivenName() + " " + user.getFamilyName());
        }

        System.out.print("User's email: ");
        String email = scanner.nextLine();
        if (!empty(email)) {
            user.setEmail(email);
            user.setUsername(user.getEmail());
        }

        System.out.print("Enter password: ");
        String password = "";
        if (console != null) {
            password = new String(console.readPassword());
        } else {
            password = new String(scanner.nextLine());
        }
        //System.out.printf("password: \"%s\"\n", password);
        if (empty(password) && empty(user.getBcryptedPassword())) {
            throw new UsageException("You must set a password!");
        } else if (!empty(password)) {
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            user.setBcryptedPassword(hashed);
        }

        if (empty(user.getSecret())) {
            user.setSecret(RandomStringUtils.randomAlphanumeric(18));
        }
        if (empty(user.getEncryptionSecret())) {
            user.setEncryptionSecret(RandomStringUtils.randomAlphanumeric(36));
        }

        user.setPredefined(true);
        user.setRole(Role.ADMIN);
        user.setId(Context.dal().getTickets().nextId());
        user.setFilePath(GeneralUtils.slugify(user.getEmail() + "---" + user.getId().toString()) + ".json");
        UserController.instance().save(user);

        System.out.print("User saved with email " + user.getEmail() + " and id " + user.getId());

    }


}
