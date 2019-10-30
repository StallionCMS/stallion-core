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

import io.stallion.services.Log;
import io.stallion.templating.TemplateRenderer;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Map;

import static io.stallion.utils.Literals.map;
import static io.stallion.utils.Literals.val;

@Provider
public class WebApplicationExceptionMapper extends BaseExceptionMapper<WebApplicationException> {



    @Override
    public Response toResponse(WebApplicationException exception) {
        if (exception.getResponse().getStatus() >= 300 && exception.getResponse().getStatus() < 400) {
            return exception.getResponse();
        }

        try {
            if (exception.getResponse().getStatus() == 400) {
                Log.exception(exception, "BadRequest exception in main request loop");
            }
            if (exception.getResponse().getStatus() >= 500) {
                Log.exception(exception, "ServerWebApplicationException in main request loop");
            }

            Log.finest("WebApplicationException: " + exception.getResponse().getStatus()
                    + exception.getClass().getCanonicalName() + exception.getMessage() + StringUtils.join(ExceptionUtils.getRootCauseStackTrace(exception), '\n'));
            if (isJson(exception.getResponse())) {
                Map info = map(
                        val("succeeded", false),
                        val("status", exception.getResponse().getStatus()),
                        val("message", exception.getMessage())
                );
                return Response
                        .status(exception.getResponse().getStatus())
                        .entity(JSON.stringify(info))
                        .type("application/json").build();
            } else {
                String html;
                if (exception instanceof NotFoundException) {
                    html = TemplateRenderer.instance().render404Html();
                } else {
                    html = TemplateRenderer.instance().render500Html(exception);
                }
                return Response
                        .status(exception.getResponse().getStatus())
                        .entity(html)
                        .type("text/html").build();
            }

        } catch (Exception e) {
            Log.exception(e, "Exception when trying to render error page for error exception: " + exception.toString());
            return exception.getResponse();
        }



    }



}
