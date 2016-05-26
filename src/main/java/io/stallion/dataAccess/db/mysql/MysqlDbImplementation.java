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

package io.stallion.dataAccess.db.mysql;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

import java.util.List;
import java.util.Map;

import io.stallion.dataAccess.Tickets;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.DbImplementation;
import io.stallion.services.Log;


public class MySqlDbImplementation implements DbImplementation {

    public String getName() { return "mysql"; }

    public String getCurrentTimeStampQuery() {
        return "SELECT CURRENT_TIMESTAMP();";
    }

    public Tickets initTicketsService(DB db) {
        return new MySqlTickets(db);
    }
}
