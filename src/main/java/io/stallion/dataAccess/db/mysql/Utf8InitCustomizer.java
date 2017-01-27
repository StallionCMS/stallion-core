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

package io.stallion.dataAccess.db.mysql;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.services.Log;

import java.sql.*;
import com.mchange.v2.log.*;
import com.mchange.v2.c3p0.AbstractConnectionCustomizer;

public class Utf8InitCustomizer extends AbstractConnectionCustomizer
{


    public void onCheckOut( Connection c, String parentDataSourceIdentityToken  ) throws Exception
    {
        Statement stmt = null;
        try
        {
            stmt = c.createStatement();
            int num = stmt.executeUpdate("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        finally
        {
            if (stmt != null) {
                stmt.close();
            }
        }
    }
}
