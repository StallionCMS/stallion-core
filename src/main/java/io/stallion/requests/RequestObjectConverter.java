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

import io.stallion.exceptions.ClientException;
import io.stallion.exceptions.JsonMappingException;
import io.stallion.restfulEndpoints.RequestArg;

import static io.stallion.utils.Literals.*;

import io.stallion.utils.json.JSON;

class RequestObjectConverter {
    private RequestArg arg;
    private StRequest request;
    public RequestObjectConverter(RequestArg arg, StRequest request) {
        this.arg = arg;
        this.request = request;
    }

    public Object convert() throws JsonMappingException {
        String content = request.getContent();
        if (empty(content)) {
            throw new ClientException("Required a JSON body for this request, but content was empty.");
        }
        try {
            Object instance = JSON.parse(content, arg.getTargetClass());
            return instance;
        } catch (Exception e) {
            throw new JsonMappingException("Error parsing JSON to target object.", e);
        }
    }
}
