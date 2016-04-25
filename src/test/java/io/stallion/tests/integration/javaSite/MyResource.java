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

package io.stallion.tests.integration.javaSite;

import com.fasterxml.jackson.annotation.JsonView;
import static io.stallion.dal.base.SettableOptions.*;

import io.stallion.restfulEndpoints.EndpointResource;
import io.stallion.restfulEndpoints.ObjectParam;
import io.stallion.utils.json.RestrictedViews;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class MyResource implements EndpointResource {
    @POST()
    @Path("/hello/creatify")
    @JsonView(RestrictedViews.Member.class)
    public Object creatify(@ObjectParam(targetClass = ExamplePojo.class, restricted = Createable.class) ExamplePojo thing) {
        ExamplePojo pojo = ExamplePojoController.instance().mergeWithDefaults(thing);
        pojo.setUpdateMessage("This was updated by the method creatify");
        pojo.setUpdated(123456789L);
        pojo.setInternalSecret("theInternalSecret");
        return pojo;
    }


    @POST()
    @Path("/:id/updatify")
    public Object updateForId(@PathParam("id") Long id, @ObjectParam(targetClass = ExamplePojo.class, restricted = AnyUpdateable.class) ExamplePojo thing) {
        thing.setId(id);

        return thing;
    }


    @POST()
    @Path("/hello/updatifyHello")
    public Object updatify(@ObjectParam(targetClass = ExamplePojo.class, restricted = AnyUpdateable.class) ExamplePojo thing) {
        ExamplePojo pojo = ExamplePojoController.instance().mergeWithDefaults(thing);
        pojo.setUpdateMessage("This was updated by the method updatify");
        pojo.setUpdated(123456789L);
        pojo.setInternalSecret("theInternalSecret");
        return pojo;
    }

    @POST()
    @Path("/hello/moderatify")
    @JsonView(RestrictedViews.Owner.class)
    public Object moderatify(@ObjectParam(targetClass = ExamplePojo.class, restricted = OwnerUpdateable.class) ExamplePojo thing) {
        ExamplePojo pojo = ExamplePojoController.instance().mergeWithDefaults(thing);
        pojo.setUpdateMessage("This was updated by the method moderatify");
        pojo.setUpdated(123456789L);
        pojo.setInternalSecret("theInternalSecret");
        return pojo;
    }



}
