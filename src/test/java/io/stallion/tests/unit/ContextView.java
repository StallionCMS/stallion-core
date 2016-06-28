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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonView;
import io.stallion.users.IUser;
import io.stallion.users.User;
import io.stallion.utils.json.RestrictedViews;

import java.util.List;


class ContextView {
    private User clerk;
    private MyFooBar featuredProduct;
    private List<MyFooBarItemView> fooBarItems;
    @JsonIgnore
    private String me;

    @JsonView(RestrictedViews.Public.class)
    public List<MyFooBarItemView> getFooBarItems() {
        return fooBarItems;
    }

    public void setFooBarItems(List<MyFooBarItemView> fooBarItems) {
        this.fooBarItems = fooBarItems;
    }

    @JsonView(RestrictedViews.Public.class)
    public User getClerk() {
        return clerk;
    }

    public void setClerk(User clerk) {
        this.clerk = clerk;
    }

    @JsonView(RestrictedViews.Public.class)
    public MyFooBar getFeaturedProduct() {
        return featuredProduct;
    }

    public void setFeaturedProduct(MyFooBar featuredProduct) {
        this.featuredProduct = featuredProduct;
    }

    @JsonView(RestrictedViews.Public.class)
    @JsonRawValue
    @JsonProperty
    public String getMe() {
        return me;
    }

    @JsonIgnore
    public void setMe(Object me) {
        this.me = me.toString();
    }

}
