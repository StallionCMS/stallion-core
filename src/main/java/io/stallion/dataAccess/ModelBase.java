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

package io.stallion.dataAccess;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.RestrictedViews;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;

import static io.stallion.utils.Literals.empty;

/**
 * Base implementation of the Model interface.
 */
public class ModelBase implements Model {

    public static Map<String, Object> meta = new HashMap<>();

    // Weird name to not conflict with JPA
    @Transient
    private Long _id;
    private Boolean deleted = false;
    private Boolean isNewInsert = false;
    private String _bucket;
    private Long lastModifiedMillis = 0L;


    public static Map<String, Object> getMeta() {
        return meta;
    }

    @JsonView(RestrictedViews.Public.class)
    public Long getId() {
        return _id;
    }

    public <Y extends Model> Y setId(Long id) {
        this._id = id;
        return (Y)this;
    }

    @JsonIgnore
    public ModelController getController() {
        return DataAccessRegistry.instance().get(getBucket());
    }


    @Column
    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @JsonIgnore
    @Transient
    public Boolean getIsNewInsert() {
        return isNewInsert;
    }

    public void setIsNewInsert(Boolean isNewInsert) {
        this.isNewInsert = isNewInsert;
    }

    /* And id made from concatenating the bucket and the object id */
    @JsonIgnore
    public String getCompoundId() {
        return GeneralUtils.slugify(getController().getBucket()) + "---" + GeneralUtils.slugify(getId().toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Model)) {
            return false;
        }
        Model m = (Model)obj;
        if (m.getId() == null || getId() == null) {
            return false;
        }
        return getId().equals(m.getId());
    }

    @Override
    public int hashCode() {
        if (empty(getId())) {
            return super.hashCode();
        } else {
            return getId().hashCode();
        }
    }

    @JsonIgnore
    @Transient
    public String getBucket() {
        return _bucket;
    }

    public Model setBucket(String bucket) {
        this._bucket = bucket;
        return this;
    }

    @Override
    public Long getLastModifiedMillis() {
        return lastModifiedMillis;
    }

    public <Y extends Model> Y setLastModifiedMillis(Long lastModifiedMillis) {
        this.lastModifiedMillis = lastModifiedMillis;
        return (Y)this;
    }
}
