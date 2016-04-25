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

package io.stallion.restfulEndpoints;

import io.stallion.dal.base.SettableOptions;


public class RequestArg {
    private String type = "";
    private String name = "";
    private Object defaultValue = null;
    private Class annotationClass;
    private Class targetClass = null;
    private Class<? extends SettableOptions.BaseSettable> settableAllowedForClass = null;

    public Class getTargetClass() {
        return targetClass;
    }

    public RequestArg setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    public String getType() {
        return type;
    }

    public RequestArg setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public RequestArg setName(String name) {
        this.name = name;
        return this;
    }


    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }


    public Class<? extends SettableOptions.BaseSettable> getSettableAllowedForClass() {
        return settableAllowedForClass;
    }

    public void setSettableAllowedForClass(Class<? extends SettableOptions.BaseSettable> settableAllowedForClass) {
        this.settableAllowedForClass = settableAllowedForClass;
    }

    public Class getAnnotationClass() {
        return annotationClass;
    }

    public void setAnnotationClass(Class annotationClass) {
        this.annotationClass = annotationClass;
    }
}
