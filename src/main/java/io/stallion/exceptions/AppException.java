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

import javax.ws.rs.ServerErrorException;

/**
 * Generic fatal exception that is the fault of the application creator and will generate a 500 error
 */
@Deprecated
public class AppException extends ServerErrorException {
    public AppException(String message, int statusCode) {
        super(message, statusCode);
    }
    public AppException(String message) {
        super(message, 500);
    }
}
