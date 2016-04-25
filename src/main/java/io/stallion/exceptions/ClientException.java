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

package io.stallion.exceptions;

/**
 * The end user did something wrong and needs to correct their request.
 * Will short-circuit the request and return a 400 error by default.
 */
public class ClientException extends WebException {

    /**
     * Message will get displayed to the end user, so make it polite and friendly.
     * @param message
     */
    public ClientException(String message) {
        super(message, 400);
    }
    /**
     * Message will get displayed to the end user, so make it polite and friendly.
     * @param message
     * @param statusCode the HTTP status code for the request
     */
    public ClientException(String message, int statusCode) {
        super(message, statusCode);
    }

    /**
     * Message will get displayed to the end user, so make it polite and friendly.
     * @param message
     * @param statusCode the HTTP status code for the request
     */
    public ClientException(String message, int statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    public ClientException(ValidationException validationException) {
        super(validationException.getMessage(), 400, validationException);
    }
}
