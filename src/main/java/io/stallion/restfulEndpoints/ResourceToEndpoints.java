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

import com.fasterxml.jackson.annotation.JsonView;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.users.Role;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.empty;


public class ResourceToEndpoints {

    private String basePath = "";

    public ResourceToEndpoints() {

    }

    public ResourceToEndpoints(String basePath) {
        this.basePath = basePath;
    }

    public List<JavaRestEndpoint> convert(EndpointResource resource) {
        Class cls = resource.getClass();
        List<JavaRestEndpoint> endpoints = new ArrayList<>();

        // Get defaults from the resource

        Role defaultMinRole = Settings.instance().getUsers().getDefaultEndpointRoleObj();
        MinRole minRoleAnno = (MinRole)cls.getAnnotation(MinRole.class);
        if (minRoleAnno != null) {
            defaultMinRole = minRoleAnno.value();
        }

        String defaultProduces = "text/html";
        Produces producesAnno = (Produces)cls.getAnnotation(Produces.class);
        if (producesAnno != null && producesAnno.value().length > 0) {
            defaultProduces = producesAnno.value()[0];
        }

        Path pathAnno = (Path)cls.getAnnotation(Path.class);
        if (pathAnno != null) {
            basePath += pathAnno.value();
        }

        Class defaultJsonViewClass = null;
        DefaultJsonView jsonView = (DefaultJsonView)cls.getAnnotation(DefaultJsonView.class);
        if (jsonView != null) {
            defaultJsonViewClass = jsonView.value();
        }


        for(Method method: cls.getDeclaredMethods()) {
            JavaRestEndpoint endpoint = new JavaRestEndpoint();
            endpoint.setRole(defaultMinRole);
            endpoint.setProduces(defaultProduces);
            if (defaultJsonViewClass != null) {
                endpoint.setJsonViewClass(defaultJsonViewClass);
            }

            Log.finer("Resource class method: {0}", method.getName());
            for(Annotation anno: method.getDeclaredAnnotations()) {
                if (Path.class.isInstance(anno)) {
                    Path pth = (Path)anno;
                    endpoint.setRoute(getBasePath() + pth.value());
                } else if (GET.class.isInstance(anno)) {
                    endpoint.setMethod("GET");
                } else if (POST.class.isInstance(anno)) {
                    endpoint.setMethod("POST");
                } else if (DELETE.class.isInstance(anno)) {
                    endpoint.setMethod("DELETE");
                } else if (PUT.class.isInstance(anno)) {
                    endpoint.setMethod("PUT");
                } else if (Produces.class.isInstance(anno)) {
                    endpoint.setProduces(((Produces) anno).value()[0]);
                } else if (MinRole.class.isInstance(anno)) {
                    endpoint.setRole(((MinRole) anno).value());
                } else if (XSRF.class.isInstance(anno)) {
                    endpoint.setCheckXSRF(((XSRF)anno).value());
                } else if (JsonView.class.isInstance(anno)) {
                    Class[] classes = ((JsonView)anno).value();
                    if (classes == null || classes.length != 1) {
                        throw new UsageException("JsonView annotation for method " + method.getName() + " must have exactly one view class");
                    }
                    endpoint.setJsonViewClass(classes[0]);
                }
            }
            if (!empty(endpoint.getMethod()) && !empty(endpoint.getRoute())) {
                endpoint.setJavaMethod(method);
                endpoint.setResource(resource);
                endpoints.add(endpoint);
                Log.fine("Register endpoint {0} {1}", endpoint.getMethod(), endpoint.getRoute());
            } else {
                continue;
            }
            int x = -1;
            for(Parameter param: method.getParameters()) {
                x++;
                RequestArg arg = new RequestArg();
                for (Annotation anno: param.getAnnotations()) {
                    arg.setAnnotationInstance(anno);
                    Log.finer("Param Annotation is: {0}, {1}", anno, anno.getClass().getName());
                    if (BodyParam.class.isInstance(anno)) {
                        BodyParam bodyAnno = (BodyParam)(anno);
                        arg.setType("BodyParam");
                        if (empty(bodyAnno.value())) {
                            arg.setName(param.getName());
                        } else {
                            arg.setName(((BodyParam) anno).value());
                        }
                        arg.setAnnotationClass(BodyParam.class);
                        arg.setRequired(bodyAnno.required());
                        arg.setEmailParam(bodyAnno.isEmail());
                        arg.setMinLength(bodyAnno.minLength());
                        arg.setAllowEmpty(bodyAnno.allowEmpty());
                        if (!empty(bodyAnno.validationPattern())) {
                            arg.setValidationPattern(Pattern.compile(bodyAnno.validationPattern()));
                        }
                    } else if (ObjectParam.class.isInstance(anno)) {
                        ObjectParam oParam = (ObjectParam)anno;
                        arg.setType("ObjectParam");
                        arg.setName("noop");
                        if (oParam.targetClass() == null || oParam.targetClass().equals(Object.class)) {
                            arg.setTargetClass(param.getType());
                        } else {
                            arg.setTargetClass(oParam.targetClass());
                        }
                        arg.setAnnotationClass(ObjectParam.class);
                    } else if (MapParam.class.isInstance(anno)) {
                        arg.setType("MapParam");
                        arg.setName("noop");
                        arg.setAnnotationClass(MapParam.class);
                    } else if (QueryParam.class.isInstance(anno)) {
                        arg.setType("QueryParam");
                        arg.setName(((QueryParam)anno).value());
                        arg.setAnnotationClass(QueryParam.class);
                    } else if (PathParam.class.isInstance(anno)) {
                        arg.setType("PathParam");
                        arg.setName(((PathParam)anno).value());
                        arg.setAnnotationClass(PathParam.class);
                    } else if (DefaultValue.class.isInstance(anno)) {
                        arg.setDefaultValue(((DefaultValue) anno).value());
                    } else if (NotNull.class.isInstance(anno)) {
                        arg.setRequired(true);
                    } else if (Nullable.class.isInstance(anno)) {
                        arg.setRequired(false);
                    } else if (NotEmpty.class.isInstance(anno)) {
                        arg.setRequired(true);
                        arg.setAllowEmpty(false);
                    } else if (Email.class.isInstance(anno)) {
                        arg.setEmailParam(true);
                    }
                }
                if (StringUtils.isEmpty(arg.getType())) {
                    arg.setType("ObjectParam");
                    arg.setName(param.getName());
                    arg.setTargetClass(param.getType());
                    arg.setAnnotationClass(ObjectParam.class);
                }

                if (StringUtils.isEmpty(arg.getName())) {
                    arg.setName(param.getName());
                }
                endpoint.getArgs().add(arg);
            }
        }
        return endpoints;
    }

    public String getBasePath() {
        return basePath;
    }

    public ResourceToEndpoints setBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }
}