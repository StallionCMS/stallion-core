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

import io.stallion.restfulEndpoints.ResourceToEndpoints;
import io.stallion.restfulEndpoints.RestEndpointBase;
import io.stallion.restfulEndpoints.JavaRestEndpoint;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.testing.MockRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;

public class EndpointResourceTest extends AppIntegrationCaseBase {

    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/a_minimal_site");
    }

    @Test
    public void testIntrospectResource() {
        List<JavaRestEndpoint> endpoints = new ResourceToEndpoints("/_stx/junit-endpoints").convert(new HelloResource());
        Assert.assertEquals(4, endpoints.size());
        JavaRestEndpoint nameEndpoint = null;
        JavaRestEndpoint personEndpoint = null;
        for(JavaRestEndpoint endpoint: endpoints) {
            if (endpoint.getRoute().contains(":name")) {
                nameEndpoint = endpoint;
            } else if (endpoint.getRoute().contains(":person")) {
                personEndpoint = endpoint;
            }
        }
        
        Assert.assertEquals("/_stx/junit-endpoints/hello/:name", nameEndpoint.getRoute());
        Assert.assertEquals("/_stx/junit-endpoints/hello/:person", personEndpoint.getRoute());
        Assert.assertEquals("POST", personEndpoint.getMethod());
        Assert.assertEquals(2, personEndpoint.getArgs().size());
        Assert.assertEquals(2, personEndpoint.getArgs().size());
        Assert.assertEquals("person", personEndpoint.getArgs().get(0).getName());
    }

    @Test
    public void testRouteResource() throws Exception {
        StRequest request = new MockRequest("/_stx/junit-endpoints/hello/phadraig?language=chinese", "GET");
        List<JavaRestEndpoint> endpoints = new ResourceToEndpoints("/_stx/junit-endpoints").convert(new HelloResource());
        List<RestEndpointBase> bEndpoints = new ArrayList<>();
        for(JavaRestEndpoint end: endpoints) {
            bEndpoints.add(end);
        }

        RouteResult result = new RoutesRegistry().routeForEndpoints(request, bEndpoints);
        Assert.assertNotNull(result);

        String output = new RequestProcessor(request, new StResponse()).dispatchWsEndpoint(result);
        Assert.assertTrue(output.contains("Ni hao, phadraig"));


        {
            StRequest request2 = new MockRequest("/_stx/junit-endpoints/greetings/frank/foo?language=chinese", "GET");
            RouteResult result2 = new RoutesRegistry().routeForEndpoints(request2, bEndpoints);
            Assert.assertNotNull(result2);

            String output2 = new RequestProcessor(request, new StResponse()).dispatchWsEndpoint(result2);
            Assert.assertTrue(output2.contains("Hair for frank is unknown"));
        }

        {
            StRequest request2 = new MockRequest("/_stx/junit-endpoints/booyah/12343210", "GET");
            RouteResult result2 = new RoutesRegistry().routeForEndpoints(request2, bEndpoints);
            Assert.assertNotNull(result2);

            String output2 = new RequestProcessor(request, new StResponse()).dispatchWsEndpoint(result2);
            Assert.assertTrue(output2.contains("thingId is 12343210"));

        }

    }



}
