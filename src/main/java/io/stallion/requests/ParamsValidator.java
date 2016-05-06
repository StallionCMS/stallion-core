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

package io.stallion.requests;

import io.stallion.exceptions.ClientException;
import io.stallion.reflection.PropertyUtils;

import java.util.List;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class ParamsValidator {
    List<OneParam> params = list();

    public ParamsValidator add(String name, Object value) {
        params.add(new OneParam().setName(name).setValue(value).setMinLength(1));
        return this;
    }

    public ParamsValidator add(String name, Object value, int minLength) {
        params.add(new OneParam().setName(name).setValue(value).setMinLength(minLength));
        return this;
    }

    public ParamsValidator optional(String name, Object value) {
        if (value == null || (value instanceof String && "".equals(value))) {
            return this;
        }
        params.add(new OneParam().setName(name).setValue(value));
        return this;

    }

    public ParamsValidator addEmail(String name, String value) {
        params.add(new OneParam().setName(name).setValue(value).setEmail(true));
        return this;
    }

    public <T> T toObject(Class<? extends T> cls) {
        validate();
        T o = null;
        try {
            o = cls.newInstance();
            for(OneParam p: params) {
                PropertyUtils.setProperty(o, p.getName(), p.getValue());
            }
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return o;
    }


    public void validate() {
        List<String> errors = list();
        for (OneParam p: params) {
            if (p.getValue() == null) {
                errors.add("Field " + p.getName() + " is required.");
                continue;
            }
            if (p.isEmail()) {
                String email = (String)p.getValue();
                if (email.length() < 3) {
                    errors.add("Field " + p.getName() + " must be a valid email address.");
                }
                if (!email.contains("@")) {
                    errors.add("Field " + p.getName() + " must be a valid email address.");
                }
                continue;
            }
            if (p.getValue() instanceof String) {
                String val = (String)p.getValue();
                if (p.getMinLength() > 0 && val.length() == 0) {
                    errors.add("Field " + p.getName() + " is required.");
                } else if (val.length() < p.getMinLength()) {
                    errors.add("Field " + p.getName() + " is too short.");
                }
                continue;
            }
        }
        if (errors.size() > 0) {
            throw new ClientException(String.join("<br>\n", errors));
        }
    }

    static class OneParam {
        private String name;
        private Object value;
        private int minLength = 1;
        private boolean email = false;

        public String getName() {
            return name;
        }

        public OneParam setName(String name) {
            this.name = name;
            return this;
        }

        public Object getValue() {
            return value;
        }

        public OneParam setValue(Object value) {
            this.value = value;
            return this;
        }

        public int getMinLength() {
            return minLength;
        }

        public OneParam setMinLength(int minLength) {
            this.minLength = minLength;
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
