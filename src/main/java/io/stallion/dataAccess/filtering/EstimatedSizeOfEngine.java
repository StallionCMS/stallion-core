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

package io.stallion.dataAccess.filtering;

import io.stallion.services.Log;
import net.sf.ehcache.pool.Size;
import net.sf.ehcache.pool.SizeOfEngine;

import java.util.Collection;


public class EstimatedSizeOfEngine implements SizeOfEngine {
    public EstimatedSizeOfEngine() {
        Log.finest("EstimatedSizeOfEngine being loaded.");
    }

    @Override
    public Size sizeOf(Object key, Object value, Object container) {
        int itemCount = 0;
        if (value instanceof Pager) {
            itemCount = ((Pager) value).getItems().size();
        } else if (value instanceof Collection) {
            itemCount = ((Collection) value).size();
        }

        if (itemCount > 0) {
            return new Size(10000 * itemCount, false);
        } else {
            return new Size(10000, false);
        }

    }

    @Override
    public SizeOfEngine copyWith(int i, boolean b) {
        return new EstimatedSizeOfEngine();
    }
}
