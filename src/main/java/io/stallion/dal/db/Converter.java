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

package io.stallion.dal.db;

import io.stallion.dal.db.converters.AttributeConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation that allows defining a custom AttributeConverter,
 * add this to a model property getter to define a custom class for
 * converting to and fro database format.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Converter {
    /**
     * The canonical class name of the converter. (It is better to use cls instead of this, for
     * better type checking.
     * @return
     */
    public String name() default "";

    /**
     * The AttributeConverter to use. Preferring setting this to setting name.
     * @return
     */
    public Class<? extends AttributeConverter> cls() default AttributeConverter.class;
}
