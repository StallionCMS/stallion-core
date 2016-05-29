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

package io.stallion.services;

import io.stallion.Context;
import io.stallion.dataAccess.db.DB;
import io.stallion.exceptions.ClientException;
import io.stallion.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;


public class SecureTempTokens {
    public static TempToken getOrCreate(String key) throws Exception {
        ZonedDateTime expires = DateUtils.utcNow();
        expires = expires.plusDays(7);
        return getOrCreate(key, expires);
    }
    public static TempToken getOrCreate(String key, ZonedDateTime expiresAt) throws Exception {
        TempToken token = (TempToken) DB.instance().fetchOne(TempToken.class, "customkey", key);
        ZonedDateTime now = DateUtils.utcNow();
        // If the token has already been used or is expired, we generate a new token
        if (token != null && token.getUsedAt() == null && now.isBefore(token.getExpiresAt())) {
            return token;
        }
        if (token == null) {
            token = new TempToken();
            token.setId(Context.dal().getTickets().nextId());
        }
        token.setCreatedAt(now);
        token.setExpiresAt(expiresAt);
        token.setCustomKey(key);
        token.setToken(idToRandomString((Long)token.getId()));
        token.setUsedAt(null);
        DB.instance().save(token);
        return token;
    }

    public static TempToken markUsed(TempToken token) throws Exception {
        token.setUsedAt(DateUtils.utcNow());
        DB.instance().save(token);
        return token;
    }

    public static TempToken fetchToken(String tokenString) throws Exception {
        if (StringUtils.isBlank(tokenString)) {
            throw new ClientException("The passed in token is empty");
        }
        TempToken token = (TempToken)DB.instance().fetchOne(TempToken.class, "token", tokenString);
        if (token == null) {
            throw new ClientException("Token not found");
        }
        ZonedDateTime now = DateUtils.utcNow();
        if (now.compareTo(token.getExpiresAt()) > 0) {
            throw new ClientException("Token has expired");
        }
        if (token.getUsedAt() != null) {
            throw new ClientException("Token was already used");
        }
        return token;
    }

    public static String idToRandomString(Long id) {
        SecureRandom random = new SecureRandom();
        Integer rand = random.nextInt();
        Long n = rand * 1000000000 + id;
        return Base64.getEncoder().encodeToString(BigInteger.valueOf(n).toByteArray());
    }
}
