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

package io.stallion.requests;

import io.stallion.restfulEndpoints.BodyParam;
import io.stallion.restfulEndpoints.EndpointResource;

import javax.ws.rs.*;

import static io.stallion.utils.Literals.or;


public class HelloResource implements EndpointResource {

    @GET()
    @Path("/hello/:name")
    public String hello(@PathParam("name") String name, @DefaultValue("english") @QueryParam("language") String language) {
        switch (language) {
            case "chinese":
                return "Ni hao, " + name;
            case "spanish":
                return "holla, " + name;
            case "english":
                return "Hello, "
                        + name;
        }
        return "Unknown language";
    }

    @POST()
    @Path("/hello/:person")
    public String updateAttitude(@PathParam("person") String name, @BodyParam("attitude") String attitude) {
        return "New attitude for " + name + " is " + attitude;
    }

    @GET()
    @Path("/greetings/{person}/foo")
    public String greetings(@PathParam("person") String name, @QueryParam("hair") String hair) {
        return "Hair for " + name + " is " + or(hair, "unknown");
    }

    @GET()
    @Path("/booyah/{thingId}")
    public String booyah(@PathParam("thingId") Long thingId, @QueryParam("hair") String hair) {
        return "thingId is " + thingId;
    }


    @GET
    @Path("/wildy/*")
    public String wildy() {
        return "Wildy wildcard endpoint";
    }
}
