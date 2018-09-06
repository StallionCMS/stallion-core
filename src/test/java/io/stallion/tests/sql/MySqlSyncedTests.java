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

package io.stallion.tests.sql;

import io.stallion.StallionApplication;
import io.stallion.dataAccess.db.SqlMigrateCommandOptions;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.DataAccessRegistration;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.DbPersister;
import io.stallion.dataAccess.db.SqlMigrationAction;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.JerseyIntegrationBaseCase;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class MySqlSyncedTests extends JerseyIntegrationBaseCase {

    public static void setupMysql() {
        SqlMigrateCommandOptions options = new SqlMigrateCommandOptions();
        URL resourceUrl = AppIntegrationCaseBase.class.
                getResource("/mysql_site");
        Path resourcePath = null;
        try {
            resourcePath = Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String path = resourcePath.toString();
        options.setTargetPath(path);
        StallionApplication app = new StallionApplication.DefaultApplication();
        try {
            app.loadForTestsLightweight(path, null, "test");
        } catch (Exception e) {
            app.shutdownAll();
            throw new RuntimeException(e);
        }

        try {
            DB.load();
        } catch (RuntimeException err) {
            if (err.getCause() instanceof SQLException) {
                String sql = "CREATE DATABASE stallion_test;\n" +
                        "CREATE USER 'stallion_user'@'localhost' IDENTIFIED BY 'wyldstallyns';\n" +
                        "GRANT ALL PRIVILEGES ON stallion_test.* TO 'stallion_user'@'%' IDENTIFIED BY 'wyldstallyns' WITH GRANT OPTION;\n" +
                        "FLUSH PRIVILEGES;";
                Log.warn("You need to create the test database first. Here is the SQL for doing so: \n{0}", sql);
                throw new RuntimeException(err);
            }
            throw new RuntimeException(err);
        }


        SqlMigrationAction migrationAction = new SqlMigrationAction();

        try {
            migrationAction.execute(options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        app.shutdownAll();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        setupMysql();
        StallionApplication app = new StallionApplication.DefaultApplication();
        startApp("/mysql_site", app, new Feature() {
            @Override
            public boolean configure(FeatureContext context) {
                context.register(MySqlEndpoint.class);
                return true;
            }
        });
        DataAccessRegistry.instance().register(
                new DataAccessRegistration()
                        .setModelClass(House.class)
                        .setControllerClass(HouseController.class)
                        .setWritable(true)
                        .setPersisterClass(DbPersister.class)
                        .setTableName("stallion_test_house")
        );

        DataAccessRegistry.instance().get("stallion_test_house").getStash().loadAll();

    }

    @Test
    public void testBasicCrud() throws Exception {
        // Cleanup
        DB.instance().newQuery().update("DELETE FROM stallion_test_House WHERE postalCode=30303");


        House house1 = new House()
                .setAddress(UUID.randomUUID().toString())
                .setPostalCode("30303")
                .setBuildYear(1705)
                .setCondemned(false)
                .setTaxesPaid(false);
        HouseController.instance().save(house1);


        House house1again = HouseController.instance().filter("address", house1.getAddress()).first();

        assertEquals(house1.getBuildYear(), house1again.getBuildYear());

        // Check to make sure it actually made it into the DB
        Integer buildYear = DB.instance().queryScalar("SELECT buildYear FROM stallion_test_house WHERE address=?", house1.getAddress());
        assertEquals((Integer)house1.getBuildYear(), buildYear);

        // Update the row and verify was updated in memory and in the database
        house1again.setBuildYear(3034);
        HouseController.instance().save(house1again);
        House house1third = HouseController.instance().filter("address", house1.getAddress()).first();
        assertEquals(3034, house1third.getBuildYear());
        buildYear = DB.instance().queryScalar("SELECT buildYear FROM stallion_test_house WHERE address=?", house1.getAddress());
        assertEquals(3034, (int)buildYear);

        // Delete the row and verify was deleted in memory and in the database
        HouseController.instance().hardDelete(house1);
        House house = HouseController.instance().forId(house1.getId());
        assertNull(house);
        long count = DB.instance().queryScalar("SELECT COUNT(*) FROM stallion_test_house WHERE address=?", house1.getAddress());
        assertEquals(0L, count);

    }

    @Test
    public void testAutoSync() throws Exception {

        DB.instance().newQuery().update("DELETE FROM stallion_test_house WHERE postalCode=30302");

        House house1 = new House()
                .setAddress(UUID.randomUUID().toString())
                .setPostalCode("30302")
                .setBuildYear(1803)
                .setCondemned(false)
                .setTaxesPaid(false);
        HouseController.instance().save(house1);

        {
            Response response = GET("/st-mysql-tests/house/" + house1.getId());
            House house1result = response.readEntity(House.class);
            assertEquals(1803, house1result.getBuildYear());

            DB.instance().newQuery().update("UPDATE stallion_test_house SET buildYear=1944 WHERE id=?", house1result.getId());
        }



        {
            // This will still be 1803, since we do not resync on a GET request
            Response response = GET("/st-mysql-tests/house/" + house1.getId());
            House house1result = response.readEntity(House.class);
            assertEquals(1803, house1result.getBuildYear());
        }
        {

            // This will be 1944, since we do resync on a POST
            Response response = target("/st-mysql-tests/house/" + house1.getId())
                    .request()
                    .header("X-Requested-By", "test")
                    .post(Entity.entity("{}", "application/json"));
            assertEquals(200, response.getStatus());
            House house1result = response.readEntity(House.class);
            assertEquals(1944, house1result.getBuildYear());
        }

        {

            // This will be 1944, since we have resynced
            Response response = GET("/st-mysql-tests/house/" + house1.getId());
            House house1result = response.readEntity(House.class);
            assertEquals(1944, house1result.getBuildYear());
        }


    }

}
