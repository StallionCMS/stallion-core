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

import io.stallion.Context;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.ModelController;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.dataAccess.db.DB;
import io.stallion.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.ClientErrorException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;


public class SecureTempTokens extends StandardModelController<TempToken> {

    public static SecureTempTokens instance() {
        return (SecureTempTokens) DataAccessRegistry.instance().get("stallion_temp_tokens");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(TempToken.class, SecureTempTokens.class, false);
    }



    public TempToken getOrCreate(String key) {
        ZonedDateTime expires = DateUtils.utcNow();
        expires = expires.plusDays(7);
        return getOrCreate(key, expires);
    }

    public TempToken getOrCreate(String key, ZonedDateTime expiresAt) {
        List<TempToken> tokens = DB.instance().queryBean(TempToken.class,
                "SELECT * FROM stallion_temp_tokens WHERE customKey=? LIMIT 1",
                key);

        TempToken token = null;
        if (tokens.size() > 0) {
            token = tokens.get(0);
        }
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

    public TempToken markUsed(TempToken token) {
        token.setUsedAt(DateUtils.utcNow());
        DB.instance().save(token);
        return token;
    }

    public TempToken fetchToken(String tokenString) {
        if (StringUtils.isBlank(tokenString)) {
            throw new ClientErrorException("The passed in token is empty", 400);
        }
        TempToken token = DB.instance().fetchBean(TempToken.class,
                "SELECT * FROM stallion_temp_tokens WHERE token=? LIMIT 1",
                tokenString);
        if (token == null) {
            throw new ClientErrorException("Token not found", 400);
        }
        ZonedDateTime now = DateUtils.utcNow();
        if (now.compareTo(token.getExpiresAt()) > 0) {
            throw new ClientErrorException("Token has expired", 400);
        }
        if (token.getUsedAt() != null) {
            throw new ClientErrorException("Token was already used", 400);
        }
        return token;
    }

    public String idToRandomString(Long id) {
        SecureRandom random = new SecureRandom();
        Integer rand = random.nextInt();
        Long n = rand * 1000000000 + id;
        return Base64.getEncoder().encodeToString(BigInteger.valueOf(n).toByteArray());
    }
}
