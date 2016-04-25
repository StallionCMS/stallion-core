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

package io.stallion.dal.base;


public abstract class BasePersister<T extends Model> implements Persister<T> {
    private Class<T> modelClass;
    private ModelController<T> controller;
    private String bucket;
    private Stash<T> stash;

    @Override
    public void init(DalRegistration registration, ModelController<T> controller, Stash<T> stash) {
        this.bucket = registration.getBucket();
        this.controller = controller;
        this.stash = stash;
        this.modelClass = (Class<T>)registration.getModelClass();
    }

    @Override
    public Class<T> getModelClass() {
        return this.modelClass;
    }
    public Persister<T> setModelClass(Class<T> modelClass) {
        this.modelClass = modelClass;
        return this;
    }

    @Override
    public boolean isDbBacked() {
        return false;
    }


    @Override
    public String getBucket() {
        return this.bucket;
    }

    @Override
    public Persister setBucket(String bucket) {
        this.bucket = bucket; return this;
    }


    @Override
    public ModelController<T> getItemController() {
        return this.controller;
    }

    @Override
    public Persister<T> setItemController(ModelController<T> controller) {
        this.controller = controller;
        return this;
    }

    public Stash<T> getStash() {
        return stash;
    }

    public BasePersister setStash(Stash<T> stash) {
        this.stash = stash;
        return this;
    }

    public void onPreRead() {

    }

    public void handleFetchOne(T obj) {
        obj.setBucket(getBucket());
        onFetchOne();
    }

    public boolean reloadIfNewer(T obj) {
        return false;
    }

    public void onFetchOne() {

    }


}
