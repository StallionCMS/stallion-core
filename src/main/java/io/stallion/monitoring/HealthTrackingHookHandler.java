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

package io.stallion.monitoring;

import io.stallion.monitoring.HealthTracker;
import io.stallion.requests.PostRequestHookHandler;
import io.stallion.requests.StRequest;
import io.stallion.requests.StResponse;


public class HealthTrackingHookHandler extends PostRequestHookHandler {
    @Override
    public void handleRequest(StRequest request, StResponse response) {
        HealthTracker.instance().logResponse(response);
    }
}
