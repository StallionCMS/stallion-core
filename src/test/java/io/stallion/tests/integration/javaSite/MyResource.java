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

package io.stallion.tests.integration.javaSite;

import com.fasterxml.jackson.annotation.JsonView;

import io.stallion.dataAccess.SafeMerger;
import io.stallion.utils.json.JSON;
import io.stallion.utils.json.RestrictedViews;

import javax.ws.rs.*;

@Path("/")
@Consumes("application/json")
@Produces("application/json")
public class MyResource {

    @GET
    @Path("/my-hello/simple")
    public String simpleHello(@QueryParam("name") String name) {
        return "Hello, " + name;
    }


    @POST()
    @Path("/my-hello/creatify")
    @JsonView(RestrictedViews.Member.class)
    @Consumes("application/json")
    @Produces("application/json")
    public Object creatify(ExamplePojo rawThing) {
        ExamplePojo pojo = SafeMerger.with()
                .nonEmpty("userName", "displayName", "content", "age")
                .optionalEmail("email")
                .ignoreOptionalEmpty()
                .merge(rawThing);
        //ExamplePojo pojo = ExamplePojoController.instance().mergeWithDefaults(thing);
        pojo.setUpdateMessage("This was updated by the method creatify");
        pojo.setUpdated(123456789L);
        pojo.setInternalSecret("theInternalSecret");
        return pojo;
    }


    @POST
    @Path("/my-hello/{id}/updatify")
    @Produces("application/json")
    @JsonView(RestrictedViews.Member.class)
    public Object updateForId(@PathParam("id") Long id, ExamplePojo thing) {
        thing.setId(id);

        return thing;
    }


    @POST()
    @Path("/my-hello/updatifyHello")
    @JsonView(RestrictedViews.Member.class)
    public Object updatify(ExamplePojo thing) {
        ExamplePojo pojo = SafeMerger.with().optional("displayName", "age", "content").merge(thing);
        pojo.setUpdateMessage("This was updated by the method updatify");
        pojo.setUpdated(123456789L);
        pojo.setInternalSecret("theInternalSecret");
        return pojo;
    }

    @POST()
    @Path("/my-hello/moderatify")
    @JsonView(RestrictedViews.Owner.class)
    public Object moderatify(ExamplePojo thing) {
        ExamplePojo pojo = SafeMerger.with().optional("displayName", "age", "content", "userName", "email", "status").merge(thing);
        pojo.setUpdateMessage("This was updated by the method moderatify");
        pojo.setUpdated(123456789L);
        pojo.setInternalSecret("theInternalSecret");
        return pojo;
    }



}
