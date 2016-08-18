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

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.NoStash;
import io.stallion.dataAccess.StandardModelController;
import io.stallion.services.Log;


public class TransactionLogController extends StandardModelController<TransactionLog> {
    public static TransactionLogController instance() {
        return (TransactionLogController) DataAccessRegistry.instance().get("stallion_transaction_logs");
    }

    public static void register() {
        DataAccessRegistry.instance().registerDbModel(TransactionLog.class, TransactionLogController.class, NoStash.class);
    }
}
