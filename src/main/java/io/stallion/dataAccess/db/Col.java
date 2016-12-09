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

package io.stallion.dataAccess.db;


import io.stallion.dataAccess.db.converters.AttributeConverter;
import io.stallion.dataAccess.db.converters.JavascriptDbColumnConverter;

/**
 * This class is used internally when reflecting over a Java model and converting it into
 * a table schema.
 */
public class Col {
    private String name = "";
    private String propertyName = "";
    private String dbType = null;
    private Class jType = String.class;
    private Boolean alternativeKey = false;
    private Boolean uniqueKey = false;
    private Boolean updateable = true;
    private Boolean insertable = true;
    private String converterClassName = "";
    private JavascriptDbColumnConverter jsDbColumnConverter = null;
    private Boolean nullable = true;
    private int length = 0;

    private Object defaultValue = null;
    private AttributeConverter attributeConverter;

    /**
     * The name of the column in the database table.
     * @return
     */
    public String getName() {
        return name;
    }

    public Col setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * The database column type for the column
     * @return
     */
    public String getDbType() {
        return dbType;
    }

    public Col setDbType(String dbType) {
        this.dbType = dbType;
        return this;
    }

    /**
     * The Java type of the data
     * @return
     */
    public Class getjType() {
        return jType;
    }

    public Col setjType(Class jType) {
        this.jType = jType;
        return this;
    }

    /**
     * Should the column be included in update queries?
     * @return
     */
    public Boolean getUpdateable() {
        return updateable;
    }

    public Col setUpdateable(Boolean updateable) {
        this.updateable = updateable; return this;
    }

    /**
     * Should the column be included in insert queries?
     * @return
     */
    public Boolean getInsertable() {
        return insertable;
    }

    public Col setInsertable(Boolean insertable) {
        this.insertable = insertable;
        return this;
    }

    /**
     * The Java model property name for this column.
     * @return
     */
    public String getPropertyName() {
        return propertyName;
    }

    public Col setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }

    /**
     * The canonical class name for the converter (the converter converts
     * between the database value type and the java value type for the column)
     * @return
     */
    public String getConverterClassName() {
        return converterClassName;
    }

    public Col setConverterClassName(String converterClassName) {
        this.converterClassName = converterClassName;
        return this;
    }

    public JavascriptDbColumnConverter getJsDbColumnConverter() {
        return jsDbColumnConverter;
    }

    public Col setJsDbColumnConverter(JavascriptDbColumnConverter jsDbColumnConverter) {
        this.jsDbColumnConverter = jsDbColumnConverter;
        return this;
    }

    public AttributeConverter getAttributeConverter() {
        return attributeConverter;
    }

    public Col setAttributeConverter(AttributeConverter attributeConverter) {
        this.attributeConverter = attributeConverter;
        return this;
    }



    /**
     * Is this column indexed or a key?
     * @return
     */
    public Boolean getAlternativeKey() {
        return alternativeKey;
    }

    public Col setAlternativeKey(Boolean alternativeKey) {
        this.alternativeKey = alternativeKey;
        return this;
    }

    /**
     * Is this column a unique key?
     * @return
     */
    public Boolean getUniqueKey() {
        return uniqueKey;
    }

    public Col setUniqueKey(Boolean uniqueKey) {
        this.uniqueKey = uniqueKey;
        return this;
    }

    /**
     * A default value that should be used during insert/updates if null
     * @return
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    public Col setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }


    public Boolean getNullable() {
        return nullable;
    }

    public Col setNullable(Boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public int getLength() {
        return length;
    }

    public Col setLength(int length) {
        this.length = length;
        return this;
    }
}
