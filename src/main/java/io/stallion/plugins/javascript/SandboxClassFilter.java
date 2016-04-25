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


import jdk.nashorn.api.scripting.ClassFilter;


public class SandboxClassFilter implements ClassFilter {
    private Sandbox box;
    private boolean disabled = false;

    public SandboxClassFilter(Sandbox box) {
        this.box = box;
    }


    @Override
    public boolean exposeToScripts(String s) {
        if (disabled) {
            return true;
        }
        if (box.getWhitelist().getClasses().contains(s)) {
            return true;
        };
        if (SandboxedClassLoader.DEFAULT_WHITE_LIST.contains(s)) {
            return true;
        }
        return false;
    }


    public boolean isDisabled() {
        return disabled;
    }

    public SandboxClassFilter setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

}
