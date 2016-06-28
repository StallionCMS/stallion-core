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

package io.stallion.plugins.javascript;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class BaseJavascriptColumn {

    private String _name;
    private String _column;
    private String _jsType;
    private Object _defaultValue;

    public BaseJavascriptColumn() {

    }

    public BaseJavascriptColumn(String name) {
        this._name = name;
    }

    private BaseJavascriptColumn column(String column) {
        this._column = column;
        return this;
    }

    private BaseJavascriptColumn jsType(String jsType) {
        this._jsType = jsType;
        return this;
    }

    private BaseJavascriptColumn defaultValue(String defaultValue) {
        this._defaultValue = defaultValue;
        return this;
    }

    public String getName() {
        return _name;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public String getColumn() {
        return _column;
    }

    public void setColumn(String _column) {
        this._column = _column;
    }

    public String getJsType() {
        return _jsType;
    }

    public void setJsType(String _jsType) {
        this._jsType = _jsType;
    }

    public Object getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(Object _defaultValue) {
        this._defaultValue = _defaultValue;
    }
}
