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

import javax.ws.rs.RedirectionException;
import java.net.URI;

/**
 * Short circuits the request and returns a 302 redirect.
 */
@Deprecated
public class RedirectException extends RedirectionException {
    private String url = "";

    public RedirectException(String url) {
        super(302, URI.create(url));
        this.setUrl(url);
    }

    public RedirectException(String url, int code) {
        super(code, URI.create(url));
        this.setUrl(url);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
