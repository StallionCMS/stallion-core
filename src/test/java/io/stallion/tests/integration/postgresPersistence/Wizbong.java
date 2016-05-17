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

package io.stallion.tests.integration.postgresPersistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stallion.dataAccess.AlternativeKey;
import io.stallion.dataAccess.ModelBase;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


//@MappedSuperclass
@Entity
@Table(name="wizbongs")
public class Wizbong extends ModelBase {

    private String name = "";

    @Column
    private ZonedDateTime _publishedAt;


    private LocalDateTime locallyAt;


    private Long age = 12L;

    private List<String> _data = new ArrayList<String>();

    @Column
    public String getName() {
        return name;
    }

    public Wizbong setName(String name) {
        this.name = name;
        return this;
    }

    @Column
    public ZonedDateTime getPublishedAt() {
        return _publishedAt;
    }

    public void setPublishedAt(ZonedDateTime publishedAt) {
        this._publishedAt = publishedAt;
    }

    /*
    public void setPublishedAt(Timestamp ts) {
        this._publishedAt = ZonedDateTime.ofInstant(ts.toInstant(), ZoneId.of("UTC"));;
    }
    */

    /*
    @Column
    public LocalDateTime getLocallyAt() {
        return locallyAt;
    }

    public void setLocallyAtDt(LocalDateTime locallyAt) {
        this.locallyAt = locallyAt;
    }

    public void setLocallyAt(Timestamp ts) {
        this.locallyAt = LocalDateTime.ofInstant(ts.toInstant(), ZoneId.of("UTC"));
    }
    */

    @Column
    @AlternativeKey
    public Long getAge() {
        return age;
    }

    public Wizbong setAge(Long age) {
        this.age = age;return this;
    }

    public List<String> data() {
        return _data;
    }

    @JsonProperty(value="data")
    public void setDataObj(List<String> data) {
        this._data = data;
    }

    @JsonProperty(value="data")
    public List<String> getDataObj() {
        return this._data;
    }

    @JsonIgnore
    @Column
    public String getData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this._data);
    }


    @JsonIgnore
    public void setData(String data)  {
        if (StringUtils.isBlank(data)) {
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            this._data = mapper.readValue(data, new TypeReference<ArrayList<String>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
