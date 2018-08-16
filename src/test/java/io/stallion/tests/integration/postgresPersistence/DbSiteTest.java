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

package io.stallion.tests.integration.postgresPersistence;


import org.apache.commons.lang3.NotImplementedException;
import org.junit.BeforeClass;
import org.junit.Test;


public class DbSiteTest {


    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    @Test
    public void testTickets() throws Exception {
        /*
        String key = "my-custom-key-" + new DateTime().getMillis();
        TempToken token = SecureTempTokens.getOrCreate(key);
        Assert.assertTrue(token.getAccessToken().length() > 5);
        TempToken retrieved = SecureTempTokens.fetchToken(token.getAccessToken());
        Assert.assertEquals(key, retrieved.getCustomKey());

        TempToken token2 = SecureTempTokens.getOrCreate(key);
        Assert.assertEquals(token2.getAccessToken(), retrieved.getAccessToken());
          */
    }


    @Test
    public void testCrud() throws Exception {
        throw new NotImplementedException("uncomment and fix");
        /*
        Wizbong wizbo = new Wizbong();
        String name = "Wz" + new DateTime().getMillis();
        String first = "First String :" + new DateTime().getMillis();
        String second = "Second String :" + new DateTime().getMillis();
        LocalDateTime locallyAt = LocalDateTime.of(2014, 2, 14, 10, 45);
        ZonedDateTime publishedAt = ZonedDateTime.of(2013, 3, 15, 10, 45, 0, 0, ZoneId.of("UTC"));
        wizbo.setName(name);
        //wizbo.setLocallyAtDt(locallyAt);
        wizbo.setPublishedAt(publishedAt);
        wizbo.data().add(first);
        wizbo.data().add(second);
        DB db = Context.app().getDb();
        PojoController<Wizbong> wizController = (PojoController<Wizbong>)Context.app().getDal().get("wizbongs");

        wizController.save(wizbo);
        Assert.assertTrue((Long)wizbo.getId() > 0);
        Wizbong wb = (Wizbong)db.fetchOne(Wizbong.class, wizbo.getId());
        Assert.assertEquals(name, wb.getName());
        Assert.assertEquals(first, wb.data().get(0));
        Assert.assertEquals(second, wb.data().get(1));
        //Assert.assertEquals(locallyAt, wb.getLocallyAt());
        Assert.assertEquals(publishedAt.toInstant().toEpochMilli(), wb.getPublishedAt().toInstant().toEpochMilli());

        // Test serialize and deserialize from JSON

        String wizboJson = JSON.stringify(wizbo);
        Log.info("WizBoJson {0}", wizboJson);
        Wizbong wbj = (Wizbong)JSON.parse(wizboJson, new TypeReference<Wizbong>(){});
        Assert.assertEquals(name, wbj.getName());
        Assert.assertEquals(first, wbj.data().get(0));
        Assert.assertEquals(second, wbj.data().get(1));
        //Assert.assertEquals(locallyAt, wb2.getLocallyAt());
        //Assert.assertEquals(publishedAt, wb2.getPublishedAt());
        Assert.assertEquals(publishedAt.toInstant().toEpochMilli(), wbj.getPublishedAt().toInstant().toEpochMilli());

        String secondName = "Wz2 " + new DateTime().getMillis();
        Wizbong wiz2 = new Wizbong();
        wiz2.setName(secondName);
        wiz2.setAge(120L);
        wiz2.setPublishedAt(publishedAt.plusDays(7));
        Context.app().getDal().get("wizbongs").save(wiz2);

        List<Wizbong> wizzes = db.fetchAll(Wizbong.class);
        Assert.assertTrue(wizzes.size() >= 2);
        wizzes = (List<Wizbong>)db.query(Wizbong.class, "SELECT * FROM wizbongs WHERE name=?", name);
        Assert.assertEquals(1, wizzes.size());

        wizzes = (List<Wizbong>)db.where(Wizbong.class, "name=?", name);
        Assert.assertEquals(1, wizzes.size());

        Wizbong wizd = (Wizbong)db.fetchOne(Wizbong.class, "name", name);
        Assert.assertEquals(name, wizd.getName());

        db.delete(wizd);

                */
    }

}
