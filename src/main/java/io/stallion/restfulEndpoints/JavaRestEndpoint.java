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

package io.stallion.restfulEndpoints;

import java.lang.reflect.Method;

/**
 * Represents a RESTful endpoint defined via a Java object and method.
 */
public class JavaRestEndpoint extends RestEndpointBase {

    private Object resource;
    private String methodName = "";
    private Method javaMethod;


    public Object getResource() {
        return resource;
    }

    public JavaRestEndpoint setResource(Object resource) {
        this.resource = resource;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public JavaRestEndpoint setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public Method getJavaMethod() {
        return javaMethod;
    }

    public JavaRestEndpoint setJavaMethod(Method javaMethod) {
        this.javaMethod = javaMethod;
        return this;
    }
}
