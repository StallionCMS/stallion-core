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

package io.stallion.tests.sql;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;
import static junit.framework.TestCase.assertEquals;

import java.util.List;
import java.util.Map;

import io.stallion.dataAccess.DataAccessRegistration;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.NoStash;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.DbPersister;
import io.stallion.restfulEndpoints.EndpointsRegistry;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import org.junit.BeforeClass;
import org.junit.Test;


public class PostgresSyncedTests extends AppIntegrationCaseBase {
    @BeforeClass
    public static void setUpClass() throws Exception {
        //startApp("/postgres_site");
        /*
        DataAccessRegistry.instance().register(
                new DataAccessRegistration()
                        .setModelClass(Payment.class)
                        .setControllerClass(PaymentController.class)
                        .setStashClass(NoStash.class)
                        .setWritable(true)
                        .setPersisterClass(DbPersister.class)
                        .setTableName("stallion_test_payment")
        );
        */

        //DB.instance().registerConverter(new PicnicAttendeesConverter());
        //EndpointsRegistry.instance().addResource("/st-mysql-tests", new MySqlEndpoint());
    }

    @Test
    public void testPostgres() {
        //long one = DB.instance().queryScalar("SELECT 1");
        //assertEquals(1, one);
        //org.postgresql.Driver

        Log.warn("Implement me");
    }
}
