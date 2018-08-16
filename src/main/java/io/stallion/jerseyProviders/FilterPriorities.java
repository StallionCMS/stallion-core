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

package io.stallion.jerseyProviders;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;


public class FilterPriorities {

    // Request Filters, executed in ascending order

    public final static int POPULATE_CONTEXT_REQUEST_FILTER = 200;
    public final static int INTERNAL_REWRITE_REQUEST_FILTER = 600;
    public final static int CORS_REQUEST_FILTER = 800;
    public final static int USER_AUTHENTICATION_FILTER = 1000;
    public final static int ENDPOINT_AUTHORIZATION_FILTER = 2000;


    // Response Filters, executed in descending order
    public final static int CORS_RESPONSE_HANDLER = 4000;
    public final static int XFRAME_OPTIONS_RESPONSE_FILTER = 4100;
    public final static int COOKIES_AND_HEADERS_RESPONSE_FILTER = 3000;
    public final static int POSTBACK_RESPONSE_FILTER = 3050;
    public final static int HEALTH_TRACKING_RESPONSE_FILTER = 750;
    public final static int TEARDOWN_CONTEXT_RESPONSE_FILTER = 500;
}
