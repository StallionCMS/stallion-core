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

package io.stallion.tests.unit;

import io.stallion.services.Log;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;
import io.stallion.utils.json.RestrictedViews;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static io.stallion.utils.Literals.*;


public class JsonSerializeTests {
    @Test
    public void testSerialize() throws IOException {
        User clerk = new User()
                .setEmail("secretemail@stallion.io")
                .setRole(Role.ADMIN)
                .setDisplayName("Public Handle")
                .setOrgId(123L);

        User merchant = new User()
                .setEmail("themerchant@stallion.io")
                .setRole(Role.REGISTERED)
                .setDisplayName("The merchant")
                .setOrgId(123L);

        User me = new User()
                .setEmail("meme@stallion.io")
                .setRole(Role.ADMIN)
                .setDisplayName("Myself")
                .setOrgId(124L);


        ContextView view = new ContextView();

        view.setClerk(clerk);
        view.setMe(JSON.stringify(me));

        MyFooBar featuredItem = new MyFooBar() {{
            setName("Iron Bar");
            setBarMaterial("iron");
            setCreationDate(ZonedDateTime.of(2015, 1, 20, 12, 40, 0, 0, GeneralUtils.UTC));
            setInStock(true);
            setRelatedProducts(list(145L, 167L));
            setStartingEpochMillis(14999888333L);
        }};
        MyFooBar foo1 = new MyFooBar() {{
            setName("Steel Bar");
            setBarMaterial("steel");
            setCreationDate(ZonedDateTime.of(2012, 1, 10, 12, 40, 0, 0, GeneralUtils.UTC));
            setInStock(true);
            setRelatedProducts(list(145L, 167L));
            setStartingEpochMillis(17999888333L);
        }};
        MyFooBar foo2 = new MyFooBar() {{
            setName("Carbon Fiber Beam");
            setBarMaterial("carbon-fiber");
            setCreationDate(ZonedDateTime.of(2013, 1, 10, 12, 40, 0, 0, GeneralUtils.UTC));
            setInStock(false);
            setRelatedProducts(list(145L, 167L));
            setStartingEpochMillis(17999888333L);
        }};


        MyFooBarItemView item1 = new MyFooBarItemView() {{
            setMyFooBar(foo1);
            setMerchant(merchant);
        }};
        MyFooBarItemView item2 = new MyFooBarItemView() {{
            setMyFooBar(foo1);
            setMerchant(merchant);
        }};


        view.setFeaturedProduct(featuredItem);
        view.setFooBarItems(list(item1, item2));

        String jsonOut = JSON.stringify(view, RestrictedViews.Public.class, true);
        Log.finer("RestrictedView.Public JSON OUT: {0}", jsonOut);
        ContextView actual = JSON.parse(jsonOut, ContextView.class);

        assertEquals(clerk.getDisplayName(), actual.getClerk().getDisplayName());
        // Email field should be blank, since it got stripped
        assertEquals("", actual.getClerk().getEmail());
        assertEquals("The merchant", actual.getFooBarItems().get(0).getMerchant().getDisplayName());
        assertEquals("", actual.getFooBarItems().get(0).getMerchant().getEmail());

        assertEquals(foo1.getName(), actual.getFooBarItems().get(0).getMyFooBar().getName());
        assertEquals(null, actual.getFooBarItems().get(0).getMyFooBar().getBarMaterial());
        // This was raw json, so it was preserved
        assertTrue(jsonOut.contains("meme@stallion.io"));


        // Now serialize with an Internal view
        jsonOut = JSON.stringify(view, RestrictedViews.Internal.class, true);
        Log.finer("RestrictedView.Internal JSON OUT: {0}", jsonOut);
        actual = JSON.parse(jsonOut, ContextView.class);
        assertEquals(merchant.getEmail(), actual.getFooBarItems().get(0).getMerchant().getEmail());
        assertEquals(foo1.getBarMaterial(), actual.getFooBarItems().get(0).getMyFooBar().getBarMaterial());


    }
}
