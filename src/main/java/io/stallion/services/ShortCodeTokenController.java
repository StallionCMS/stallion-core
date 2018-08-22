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

package io.stallion.services;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.exceptions.UsageException;
import io.stallion.utils.GeneralUtils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.utcNow;


public class ShortCodeTokenController extends StandardModelController<ShortCodeToken> {
    public static ShortCodeTokenController instance() {
        ShortCodeTokenController c = (ShortCodeTokenController) DataAccessRegistry.instance().get("stallion_short_code_tokens");
        if (c == null) {
            throw new UsageException("You must first call ShortCodeTokenController.register() before you use it. Try registering during the boot phase of your plugin.");
        }
        return c;
    }

    public static void register() {
        ShortCodeTokenController c = (ShortCodeTokenController) DataAccessRegistry.instance().get("stallion_short_code_tokens");
        if (c == null) {
            DataAccessRegistry.instance().registerDbModel(ShortCodeToken.class, ShortCodeTokenController.class, false);
        }
    }

    public ShortCodeToken newToken(String actionKey) {
        return newToken(actionKey, 15);
    }

    public ShortCodeToken newToken(String actionKey, Integer expiresInMinutes) {
        ZonedDateTime expiresAt = utcNow().plusMinutes(expiresInMinutes);

        String code = GeneralUtils.secureRandomNumeric(6);

        Long id = DB.instance().getTickets().nextId();
        SecureRandom random = new SecureRandom();
        Integer rand = random.nextInt();
        Long n = rand * 1000000000 + id;
        String key = Base64.getEncoder().encodeToString(BigInteger.valueOf(n).toByteArray());

        ShortCodeToken scode = new ShortCodeToken()
                .setActionKey(actionKey)
                .setCreatedAt(utcNow())
                .setExpiresAt(expiresAt)
                .setKey(key)
                .setCode(code)
                .setId(id)
        ;
        save(scode);
        return scode;
    }

    public boolean verify(String actionKey, String key, String code) {
        ShortCodeToken scode = forUniqueKey("key", key);
        if (scode == null) {
            return false;
        }
        if (!empty(scode.getUsedAt())) {
            return false;
        }
        if (scode.getExpiresAt().isBefore(utcNow())) {
            return false;
        }
        if (empty(code)) {
            return false;
        }
        if (empty(actionKey)) {
            return false;
        }
        if (!actionKey.equals(scode.getActionKey())) {
            return false;
        }
        if (scode.getCode().equals(code)) {
            return true;
        }
        return false;
    }
}
