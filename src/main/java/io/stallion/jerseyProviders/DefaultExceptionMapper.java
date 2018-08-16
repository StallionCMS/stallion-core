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

import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;
import io.stallion.templating.TemplateRenderer;
import io.stallion.utils.json.JSON;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultExceptionMapper  extends BaseExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        Log.exception(exception, "Exception bubbled up to DefaultExceptionMapper");
        String friendlyMessage = "There was an internal server error while trying to serve your request.";
        try {
            if (isJson(null)) {
                Map info = map(
                        val("succeeded", false),
                        val("status", 500),
                        val("message", friendlyMessage)
                );
                return Response
                        .status(500)
                        .entity(JSON.stringify(info))
                        .type("application/json").build();
            } else {
                String html = TemplateRenderer.instance().render500Html(exception);
                return Response
                        .status(500)
                        .entity(html)
                        .type("text/html").build();
            }

        } catch (Exception e) {
            Log.exception(e, "Exception when trying to render error page for error exception: " + exception.toString());
            return Response
                    .status(500)
                    .entity("Multiple internal exceptions when trying to serve your request.")
                    .type("text/html").build();
        }



    }



}

