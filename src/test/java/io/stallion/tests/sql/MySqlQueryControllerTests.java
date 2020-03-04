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

import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.DataAccessRegistration;
import io.stallion.dataAccess.NoStash;
import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.db.DbPersister;
import io.stallion.dataAccess.db.mysql.MySqlFilterChain;
import io.stallion.dataAccess.filtering.FilterOperator;
import io.stallion.dataAccess.filtering.Pager;
import io.stallion.dataAccess.filtering.SortDirection;
import io.stallion.services.DynamicSettings;
import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.JerseyIntegrationBaseCase;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static io.stallion.utils.Literals.*;
import static org.junit.Assert.*;



public class MySqlQueryControllerTests extends JerseyIntegrationBaseCase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/mysql_site", new Feature() {
            @Override
            public boolean configure(FeatureContext context) {
                context.register(MySqlEndpoint.class);
                return true;
            }
        });
        DataAccessRegistry.instance().register(
                new DataAccessRegistration()
                        .setModelClass(Payment.class)
                        .setControllerClass(PaymentController.class)
                        .setStashClass(NoStash.class)
                        .setWritable(true)
                        .setPersisterClass(DbPersister.class)
                        .setTableName("stallion_test_payment")
        );
        DataAccessRegistry.instance().register(
                new DataAccessRegistration()
                        .setModelClass(Picnic.class)
                        .setControllerClass(PicnicController.class)
                        .setStashClass(NoStash.class)
                        .setWritable(true)
                        .setPersisterClass(DbPersister.class)
                        .setTableName("stallion_test_picnic")
        );
        DB.instance().registerConverter(new PicnicAttendeesConverter());

    }



    @Test
    public void testHostSettings() {
        String val = GeneralUtils.randomTokenBase32(30);
        String name = "somethingsomething";
        DynamicSettings.instance().put("core", name, val);

        assertEquals(val, DynamicSettings.instance().get("core", name));

    }



    @Test
    public void testQueryController() {
        DB.instance().execute("DELETE FROM stallion_test_payment WHERE accountId='franklin-warzal-19380203'");

        Payment payment = new Payment()
                .setAccountId("franklin-warzal-19380203")
                .setAmount(3949)
                .setMemo("via check")
                .setOnTime(true)
                .setDate(DateUtils.mils())
                ;
        PaymentController.instance().save(payment);

        Payment paymentQ = PaymentController.instance().filter("accountId", "franklin-warzal-19380203").first();

        assertEquals(payment.getId() + "", paymentQ.getId() + "");
        assertEquals(3949, paymentQ.getAmount());

        // Update
        paymentQ.setMemo("via cash rebate");
        PaymentController.instance().save(paymentQ);
        Payment paymentQ2 = PaymentController.instance().filter("accountId", "franklin-warzal-19380203").setUseCache(false).first();
        assertEquals("via cash rebate", paymentQ2.getMemo());

        // Delete
        PaymentController.instance().hardDelete(payment);
        Payment paymentQ3 = PaymentController.instance().filter("onTime", "true").filter("accountId", "franklin-warzal-19380203").first();
        assertNull(paymentQ3);

    }

    @Test
    public void testSumsAverages() {
        DB.instance().execute("DELETE FROM stallion_test_payment WHERE accountId='franklin-bruce-19380203'");

        Payment payment = new Payment()
                .setAccountId("franklin-bruce-19380203")
                .setAmount(90)
                .setMemo("via check")
                .setOnTime(true)
                .setDate(DateUtils.mils())
                ;
        PaymentController.instance().save(payment);


        payment = new Payment()
                .setAccountId("franklin-bruce-19380203")
                .setAmount(50)
                .setMemo("via check")
                .setOnTime(true)
                .setDate(DateUtils.mils())
                ;
        PaymentController.instance().save(payment);

        Pager<Payment> payments = PaymentController.instance()
                .filter("accountId", "franklin-bruce-19380203")
                .sum("amount")
                .avg("amount")
                .pager(1, 10);

        assertEquals(payments.getAverages().get("amount"), (Double)70.0);
        assertEquals(payments.getSums().get("amount"), (Double)140.0);



    }

    @Test
    public void testComplexQueries() {
        DB.instance().execute("DELETE FROM stallion_test_picnic");
        {
            Picnic picnic = new Picnic()
                    .setDescription("testComplexQueries")
                    .setLocation("Arboretum")
                    .setAdminIds(set(123L, 124L))
                    .setDishes(list("salad", "burgers"));
            PicnicController.instance().save(picnic);
        }
        {
            Picnic picnic = new Picnic()
                    .setDescription("testComplexQueries")
                    .setLocation("Breeds Hill")
                    .setAdminIds(set(500L, 124L))
                    .setDishes(list("chips", "burgers", "tapas"));
            PicnicController.instance().save(picnic);
        }
        {
            Picnic picnic = new Picnic()
                    .setDescription("testComplexQueries")
                    .setLocation("Esplanad")
                    .setAdminIds(set(600L, 700L))
                    .setDishes(list("chips", "pitas", "salad"));
            PicnicController.instance().save(picnic);
        }
        {
            Picnic picnic = new Picnic()
                    .setDescription("testComplexQueries")
                    .setLocation("Washington Square")
                    .setAdminIds(set(500L, 900L))
                    .setDishes(list("pasta", "cookies", "cake"));
            PicnicController.instance().save(picnic);
        }

        {
            List<Picnic> results = PicnicController.instance()
                    .filterChain()
                    .filterBy("dishes", list("salad", "burgers"), FilterOperator.INTERSECTS)
                    .sortBy("location", SortDirection.ASC)
                    .all();
            assertEquals(3, results.size());
        }

        {
            List<Picnic> results = PicnicController.instance()
                    .filterChain()
                    .filterBy("dishes", list("burgers", "sdf'ads`f"), FilterOperator.INTERSECTS)
                    .sortBy("location", SortDirection.ASC)
                    .all();
            assertEquals(2, results.size());
        }

        {
            List<Picnic> results = PicnicController.instance()
                    .filterChain()
                    .filterBy("dishes", list(), FilterOperator.INTERSECTS)
                    .sortBy("location", SortDirection.ASC)
                    .all();
            assertEquals(4, results.size());
        }

        {
            List<Picnic> results = PicnicController.instance()
                    .filterChain()
                    .filterBy("adminIds", list(500L, 124L), FilterOperator.INTERSECTS)
                    .sortBy("location", SortDirection.ASC)
                    .all();
            assertEquals(3, results.size());
        }

        Pager<Picnic> pager = ((MySqlFilterChain<Picnic>)PicnicController.instance().filterChain()).setBaseSql("" +
                "SELECT * FROM stallion_test_picnic WHERE stallion_test_picnic.type='INVITEES_ONLY' "
        ).filter("location", "Arboretum").sortBy("id", SortDirection.DESC).pager(1, 20);
        assertEquals(1, pager.getItems().size());
    }


    @Test
    public void testCachingEndpoints() throws SQLException {
        DB.instance().execute("DELETE FROM stallion_test_payment WHERE accountId='jeffrey-warzal-19380203'");

        Payment payment = new Payment()
                .setAccountId("jeffrey-warzal-19380203")
                .setAmount(54321)
                .setMemo("via check")
                .setOnTime(true)
                .setDate(DateUtils.mils())
                ;
        PaymentController.instance().save(payment);

        {
            Response response = GET("/st-mysql-tests/payment/" + payment.getId());
            assertEquals(200, response.getStatus());
            payment = response.readEntity(Payment.class);
            assertEquals(54321, payment.getAmount());
        }

        DB.instance().newQuery().update("UPDATE stallion_test_payment SET amount=12345 WHERE id=?", payment.getId());

        {
            // This will still be 54321, since we do not resync on a GET request
            Response response = GET("/st-mysql-tests/payment/" + payment.getId());
            assertEquals(200, response.getStatus());
            payment = response.readEntity(Payment.class);//JSON.parse(response.getContent(), Payment.class);

            assertEquals(54321, payment.getAmount());
        }

        {
            // POST request, so we skip the cache. This will now be the updated value 12345
            Response response = target("/st-mysql-tests/payment/" + payment.getId())
                    .request()
                    .header("X-Requested-By", "test")
                    .post(Entity.entity(map(), "application/json"))
                    ;
            assertEquals(200, response.getStatus());
            payment = response.readEntity(Payment.class);
            assertEquals(12345, payment.getAmount());
        }

        {

            // Now the GET request will also be updated
            Response response = target("/st-mysql-tests/payment/" + payment.getId())
                    .request()
                    .get();
            assertEquals(200, response.getStatus());
            payment = response.readEntity(Payment.class);
            assertEquals(12345, payment.getAmount());
        }

    }



    @Test
    public void testAllColumnTypes() {
        String location = "Central Park " + UUID.randomUUID().toString();
        LocalDate replyBy = LocalDate.of(2019, 3, 14);
        Picnic picnic = new Picnic()
                .setAdminIds(set(123L, 124L))
                .setDishes(list("salad", "burgers"))
                .setAttendees(list(
                        new PicnicAttendence().setCount(2).setName("Jamie Doe").setRsvp(PicnicRsvpStatus.YES),
                        new PicnicAttendence().setCount(1).setName("Jane Smith").setRsvp(PicnicRsvpStatus.NO)
                ))
                .setReplyBy(replyBy)
                .setCanceled(false)
                .setDate(ZonedDateTime.of(2019, 3, 20, 10, 30, 0, 0, ZoneId.of("UTC")))
                .setDescription("We will be meeting at the Great Park")
                .setLocation(location)
                .setType(PicnicType.FRIENDS_AND_FAMILY)
                .setExtra(map(val("byob", true), val("tagLine", "Summer fun!"), val("permitPurchased", false), val("costPerPerson", 5)))
                ;
        PicnicController.instance().save(picnic);

        Picnic picnic2 = PicnicController.instance().originalForId(picnic.getId());


        // Make sure it is not actually the same object in memory, otherwise conversion won't be tested.
        assertTrue(picnic != picnic2);
        assertEquals(picnic.getId(), picnic2.getId());
        assertEquals(JSON.stringify(picnic.getAdminIds()), JSON.stringify(picnic2.getAdminIds()));
        assertEquals(JSON.stringify(picnic.getDishes()), JSON.stringify(picnic2.getDishes()));
        assertEquals(picnic.getExtra(), picnic2.getExtra());
        assertEquals(picnic.getDate(), picnic2.getDate());
        assertEquals("Jamie Doe", picnic.getAttendees().get(0).getName());
        assertEquals(picnic.getReplyBy(), picnic2.getReplyBy());

        String picnicJson = JSON.stringify(picnic);
        Picnic picnic3 = JSON.parse(picnicJson, Picnic.class);

        assertEquals(picnic.getDate(), picnic3.getDate());
        assertEquals(picnic.getReplyBy(), picnic3.getReplyBy());

        Picnic picnic4 = JSON.parse("{\"replyBy\": \"2019-03-14\"}", Picnic.class);
        assertEquals(picnic.getReplyBy(), picnic4.getReplyBy());

    }

}
