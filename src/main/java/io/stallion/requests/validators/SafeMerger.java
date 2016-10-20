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

package io.stallion.requests.validators;


import io.stallion.exceptions.ClientException;
import io.stallion.reflection.PropertyUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.Sanitize;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.emptyObject;
import static io.stallion.utils.Literals.list;

public class SafeMerger {
    private List<OneParam> params = list();

    public static SafeMerger with() {
        return new SafeMerger();
    }

    /*
    public static SafeMerger nonEmpty(String...fieldNames) {
        SafeMerger merger = new SafeMerger().withNonEmpty(fieldNames);
        return merger;
    }

    public static SafeMerger nonNull(String...fieldNames) {
        SafeMerger merger = new SafeMerger().withNonNull(fieldNames);
        return merger;
    }

    public static SafeMerger optional(String...fieldNames) {
        SafeMerger merger = new SafeMerger().withOptional(fieldNames);
        return merger;
    }
    */


    public SafeMerger nonNull(String...fieldNames) {
        for (String field: fieldNames) {
            params.add(
                    new OneParam().setFieldName(field).setRequired(true)
            );
        }
        return this;
    }

    public SafeMerger nonEmpty(String...fieldNames) {
        for (String field: fieldNames) {
            params.add(new OneParam().setFieldName(field).setRequired(true).setNonEmpty(true));
        }
        return this;
    }

    public SafeMerger email(String...fieldNames) {
        for (String field: fieldNames) {
            params.add(new OneParam().setFieldName(field).setEmail(true));
        }
        return this;
    }

    public SafeMerger minLength(int minLength, String...fieldNames) {
        for (String field: fieldNames) {
            params.add(new OneParam().setFieldName(field).setRequired(true).setMinLength(minLength));
        }
        return this;
    }

    public SafeMerger patterns(Pattern pattern, String ...fieldNames) {
        for (String field: fieldNames) {
            params.add(new OneParam().setFieldName(field).setRequired(true).setPattern(pattern));
        }
        return this;
    }

    public SafeMerger optional(String...fieldNames) {
        for (String field: fieldNames) {
            params.add(new OneParam().setFieldName(field).setRequired(false));
        }
        return this;
    }

    public SafeMerger optionalEmail(String...fieldNames) {
        for (String field: fieldNames) {
            params.add(new OneParam().setFieldName(field).setRequired(false).setNonEmpty(false).setEmail(true));
        }
        return this;
    }

    public <T> T mergeMap(Map<String, Object> newValues, T dest) {
        List<String> errors = list();
        for (OneParam param : params) {
            Object val = newValues.getOrDefault(param.getFieldName(), null);
            mergeForParam(param, val, dest, errors);
        }
        if (errors.size() > 0) {
            throw new ClientException("Errors validating request: " + String.join(".\n", errors));
        }
        return dest;

    }

    public <T> T mergeMap(Map<String, Object> newValues, Class<? extends T> destClass) {
        T dest = null;
        try {
            dest = (T)destClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return mergeMap(newValues, dest);
    }


    public <T> T merge(T newValues, T dest) {
        List<String> errors = list();
        for (OneParam param : params) {
            Object val = PropertyUtils.getPropertyOrMappedValue(newValues, param.getFieldName());
            mergeForParam(param, val, dest, errors);
        }
        if (errors.size() > 0) {
            throw new ClientException("Errors validating request: " + String.join(".\n", errors));
        }
        return dest;
    }

    private void mergeForParam(OneParam param, Object val, Object dest, List<String> errors) {
        if (val == null) {
            if (param.isRequired()) {
                errors.add("Field " + param.getFieldName() + " is required.");
            }
            return;
        }
        if (val == null)  {
            if (param.isRequired()) {
                errors.add("Field " + param.getFieldName() + " is required.");
            } else {
                return;
            }
        }
        if (param.isNonEmpty() && emptyObject(val)) {
            errors.add("Field " + param.getFieldName() + " must not be empty.");
            return;
        }
        if (param.getMinLength() > 0) {
            if (val instanceof String) {
                if (((String) val).length() < param.getMinLength()) {
                    errors.add("Field " + param.getFieldName() + " must be at least " + param.getMinLength() + " character(s)");
                    return;
                }
            } else if (val instanceof Collection) {
                if (((Collection) val).size() < param.getMinLength()) {
                    errors.add("Field " + param.getFieldName() + " must have at least " + param.getMinLength() + " entries.");
                    return;
                }
            }
        }
        if (param.isEmail()) {
            String valString = (String)val;
            if (!GeneralUtils.isValidEmailAddress(valString)) {
                errors.add("Field " + param.getFieldName() + " must be a valid email address. Passed in value - " + Sanitize.stripAll(valString) + " - is not valid.");
                return;
            }
        }
        PropertyUtils.setProperty(dest, param.getFieldName(), val);
    }
    public <T> T merge(T newValues) {
        T dest = null;
        try {
            dest = (T)newValues.getClass().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return merge(newValues, dest);
    }

    public List<OneParam> getParams() {
        return params;
    }

    public SafeMerger setParams(List<OneParam> params) {
        this.params = params;
        return this;
    }



    static class OneParam {
        private String fieldName;
        private boolean required = false;
        private boolean nonEmpty = false;
        private int minLength = 0;
        private Pattern pattern = null;
        private boolean email = false;

        public String getFieldName() {
            return fieldName;
        }

        public OneParam setFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public OneParam setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public boolean isNonEmpty() {
            return nonEmpty;
        }

        public OneParam setNonEmpty(boolean nonEmpty) {
            this.nonEmpty = nonEmpty;
            return this;
        }

        public int getMinLength() {
            return minLength;
        }

        public OneParam setMinLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public OneParam setPattern(Pattern pattern) {
            this.pattern = pattern;
            return this;
        }

        public boolean isEmail() {
            return email;
        }

        public OneParam setEmail(boolean email) {
            this.email = email;
            return this;
        }
    }
}
