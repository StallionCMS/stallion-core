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

package io.stallion.dataAccess;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A Model is the basic interface by which all data access in Stallion is done.
 */
public interface Model {

    /**
     * The primary key of the item.
     * @return
     */
    public Long getId();                     ;

    public <Y extends Model> Y setId(Long id);

    /**
     * The controller used to save/retrieve/update this model.
     * @return
     */
    @JsonIgnore
    public ModelController getController();

    /**
     * Return true if the model is soft delete, false by default.
     * @return
     */
    public Boolean getDeleted();
    public void setDeleted(Boolean deleted);

    /**
     * Temporary Marker to indicate that this model should be inserted, not updated
     * @return
     */
    @JsonIgnore
    public Boolean getIsNewInsert();
    public void setIsNewInsert(Boolean isNewInsert);
    public String getCompoundId();

    /**
     * The datastore bucket associated with this model.
     * @return
     */
    public String getBucket();
    public Model setBucket(String bucket);

    /**
     * When this model was last changed.
     *
     * @return
     */
    public Long getLastModifiedMillis();
    public <Y extends Model> Y setLastModifiedMillis(Long millis);
}
