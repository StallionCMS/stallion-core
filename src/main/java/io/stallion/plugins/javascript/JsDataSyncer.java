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

import io.stallion.dataAccess.ModelController;
import io.stallion.dataAccess.Model;
import io.stallion.exceptions.UsageException;

import java.util.HashSet;
import java.util.List;


public abstract class JsDataSyncer {

    public abstract long getBaseId();
    public abstract void onSyncObject(Object o);

    private HashSet<Long> usedIds = new HashSet<>();

    public long newId(Long i) {
        long id = getBaseId() + i;
        if (usedIds.contains(id)) {
            throw new UsageException("You used the id " + i + " twice!");
        }
        usedIds.add(id);
        return id;
    }

    public long getId(Long i) {
        return getBaseId() + i;
    }

    public void syncObjects(List<Model> models) {
        if (models.size() == 0) {
            return;
        }
        ModelController controller = models.get(0).getController();
        for (Model model: models) {
            onSyncObject(model);
            controller.save(model);
        }
    }
}
