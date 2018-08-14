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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.ExceptionMapper;


public abstract class BaseExceptionMapper<T extends Throwable>  implements ExceptionMapper<T> {

    @javax.ws.rs.core.Context
    protected HttpServletRequest httpRequest;

    @javax.ws.rs.core.Context
    protected HttpServletResponse httpResponse;


    protected boolean isJson() {
        if ("text/html".equals(httpResponse.getHeader("Content-type"))) {
            return false;
        }

        if ("XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"))) {
            return true;
        }
        if ("application/json".equals(httpResponse.getHeader("Content-type"))) {
            return true;
        }
        return false;
    }
}
