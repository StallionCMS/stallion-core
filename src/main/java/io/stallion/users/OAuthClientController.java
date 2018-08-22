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

import io.stallion.dataAccess.*;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.DbPersister;
import io.stallion.settings.Settings;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.map;


public class OAuthClientController extends StandardModelController<OAuthClient> {
    private Map<String, OAuthClient> builtinClientsByFullKey = map();
    private Map<Long, OAuthClient> builtinClientsById = map();

    public static OAuthClientController instance() {
        return (OAuthClientController) DataAccessRegistry.instance().get("oauth_clients");
    }

    public static void register() {
        DataAccessRegistration registration = new DataAccessRegistration()
                .setBucket("oauth_clients")
                .setControllerClass(OAuthClientController.class)
                .setModelClass(OAuthClient.class)
                ;
        if (DB.available()) {
            registration
                    .setStashClass(NoStash.class)
                    .setTableName("oauth_clients")
                    .setPersisterClass(DbPersister.class);
        } else {
            registration
                    .setStashClass(LocalMemoryStash.class)
                    .setPath("oauth_clients")
                    .setShouldWatch(false);

        }
        DataAccessRegistry.instance().register(registration);

        // Add in the default clients...
        if (!empty(Settings.instance().getoAuth().getClients())) {
            for(OAuthClient client: Settings.instance().getoAuth().getClients()) {
                if (empty(client.getFullClientId())) {
                    client.setFullClientId(client.getId().toString());
                }
                instance().builtinClientsByFullKey.put(client.getFullClientId(), client);
                instance().builtinClientsById.put(client.getId(), client);
            }
        }

    }

    public OAuthClient forId(Long id) {
        if (builtinClientsById.containsKey(id)) {
            return builtinClientsById.get(id);
        }
        return super.forId(id);
    }


    public OAuthClient clientForFullId(String fullClientId) {
        if (builtinClientsByFullKey.containsKey(fullClientId)) {
            return builtinClientsByFullKey.get(fullClientId);
        }
        Long clientId = fullIdToLongId(fullClientId);
        OAuthClient client = null;
        if (empty(clientId)) {
            client = forUniqueKey("fullClientId", fullClientId);
        } else {
            client = forId(clientId);
            if (client != null && !client.getFullClientId().equals(fullClientId)) {
                client = null;
            }
        }
        return client;
    }

    public static long fullIdToLongId(String fullClientId) {
        String idPart = "";
        if (!fullClientId.contains("-")) {
            idPart = fullClientId;
        } else {
            idPart = StringUtils.split(fullClientId, "-", 2)[0];
        }
        if (!StringUtils.isNumeric(idPart)) {
            return 0;
        }
        return Long.parseLong(idPart);
    }


}
