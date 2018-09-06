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

package io.stallion.http;

import io.stallion.Context;
import io.stallion.requests.IRequest;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;


public abstract class BaseExceptionMapper<T extends Throwable>  implements ExceptionMapper<T> {




    protected boolean isJson(@Nullable  Response response) {
        IRequest request = Context.getRequest();

        if (response != null) {
            if ("text/html".equals(response.getHeaderString("Content-type"))) {
                return false;
            }
            if ("application/json".equals(response.getHeaderString("Content-type"))) {
                return true;
            }
        }
        if (request != null) {

            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                return true;
            }

            Boolean guessIsJson = (Boolean) request.getProperty(ProducesDetectionRequestFilter.GUESS_IS_JSON_PROPERTY_NAME);
            if (guessIsJson != null && guessIsJson) {
                return true;
            }
        }

        return false;
    }
}
