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

package io.stallion.tests.unit;

import com.fasterxml.jackson.annotation.JsonView;
import io.stallion.utils.json.RestrictedViews;

import java.time.ZonedDateTime;
import java.util.List;


class MyFooBar {
    private String name;
    private long startingEpochMillis = 0;
    private String barMaterial;
    private int strengthFactor = 0;
    private ZonedDateTime creationDate;
    private List<Long> relatedProducts;
    private boolean inStock;

    @JsonView(RestrictedViews.Public.class)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonView(RestrictedViews.Internal.class)
    public long getStartingEpochMillis() {
        return startingEpochMillis;
    }

    public void setStartingEpochMillis(long startingEpochMillis) {
        this.startingEpochMillis = startingEpochMillis;
    }


    @JsonView(RestrictedViews.Member.class)
    public String getBarMaterial() {
        return barMaterial;
    }

    public void setBarMaterial(String barMaterial) {
        this.barMaterial = barMaterial;
    }

    @JsonView(RestrictedViews.Owner.class)
    public int getStrengthFactor() {
        return strengthFactor;
    }

    public void setStrengthFactor(int strengthFactor) {
        this.strengthFactor = strengthFactor;
    }

    @JsonView(RestrictedViews.Public.class)
    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @JsonView(RestrictedViews.Member.class)
    public List<Long> getRelatedProducts() {
        return relatedProducts;
    }

    public void setRelatedProducts(List<Long> relatedProducts) {
        this.relatedProducts = relatedProducts;
    }

    @JsonView(RestrictedViews.Public.class)
    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
