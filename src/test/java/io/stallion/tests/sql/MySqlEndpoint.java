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



import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


public class MySqlEndpoint   {
    @GET
    @Path("/house/:id")
    public House fetchHouse(@PathParam("id") Long id) {
        return HouseController.instance().forId(id);
    }

    @POST
    @Path("/house/:id")
    public House updateHouse(@PathParam("id") Long id) {
        return HouseController.instance().forId(id);
    }

    @GET
    @Path("/payment/:id")
    public Payment fetchPayment(@PathParam("id") Long id) {
        return PaymentController.instance().forId(id);
    }

    @POST
    @Path("/payment/:id")
    public Payment updatePayment(@PathParam("id") Long id) {
        return PaymentController.instance().forId(id);
    }
}

