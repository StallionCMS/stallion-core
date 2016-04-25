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

package io.stallion.plugins.javascript;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class JsRegistry {

    private List<JsEndpoint> jsEndpoints = new ArrayList<>();

    private String filePath = "";

    public JsRegistry registerRootEndpoints(JsEndpoint ... newEndpoints) {
        for(JsEndpoint endpoint: newEndpoints) {
            registerRootEndpoint(endpoint);
        }
        return this;
    }

    public JsRegistry registerRootEndpoint(JsEndpoint endpoint) {
        jsEndpoints.add(endpoint);
        return this;
    }


    public JsRegistry registerEndpoint(JsEndpoint ... newEndpoints) {
        for(JsEndpoint endpoint: newEndpoints) {
            endpoint.setRoute("/_stx/" + new File(new File(filePath).getParent()).getName() + endpoint.getRoute());
            jsEndpoints.add(endpoint);
        }
        return this;
    }

    public JsRegistry registerEndpoints(JsEndpoint ... newEndpoints) {
        for(JsEndpoint endpoint: newEndpoints) {
            registerEndpoint(endpoint);
        }
        return this;
    }


    public List<JsEndpoint> getJsEndpoints() {
        return jsEndpoints;
    }

    public void setJsEndpoints(List<JsEndpoint> jsEndpoints) {
        this.jsEndpoints = jsEndpoints;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
