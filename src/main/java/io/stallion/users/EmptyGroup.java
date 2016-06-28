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

package io.stallion.users;

import java.util.ArrayList;
import java.util.List;


public class EmptyGroup implements IGroup {
    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getParentGroupId() {
        return "";
    }

    @Override
    public String getParentOrgId() {
        return "";
    }

    @Override
    public List<IUser> getUsers() {
        return new ArrayList<IUser>();
    }

    @Override
    public Boolean hasUser() {
        return false;
    }
}
