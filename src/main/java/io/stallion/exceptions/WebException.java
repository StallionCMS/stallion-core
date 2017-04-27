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

package io.stallion.exceptions;

import javax.persistence.Column;
import java.util.Map;

import static io.stallion.utils.Literals.map;

/**
 * Generic web exception that will generate a 4xx or 5xx response to the client with a user friendly message.
 */
public class WebException extends RuntimeException {
    private int statusCode = 500;
    private Map extra = map();

    public WebException(String message) {
        this(message, 500);
    }
    public WebException(String message, int statusCode) {
        super(message);
        this.setStatusCode(statusCode);
    }

    public WebException(String message, int statusCode, Map extra) {
        super(message);
        this.setStatusCode(statusCode);
        this.extra = extra;
    }

    public WebException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }


    public Map<String, Object> getExtra() {
        return extra;
    }

    public WebException setExtra(Map<String, Object> extra) {
        this.extra = extra;
        return this;
    }
}
