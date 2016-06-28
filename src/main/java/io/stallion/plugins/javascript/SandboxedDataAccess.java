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

package io.stallion.plugins.javascript;

import io.stallion.Context;
import io.stallion.dataAccess.DataAccessRegistry;
import io.stallion.dataAccess.ModelController;
import io.stallion.exceptions.UsageException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


public class SandboxedDataAccess implements Map<String, ModelController> {

    private Sandbox sandbox;

    public SandboxedDataAccess(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    @Override
    public int size() {
        return DataAccessRegistry.instance().size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return DataAccessRegistry.instance().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public ModelController get(Object key) {
        if (sandbox.isCanWriteAllData()) {
            return DataAccessRegistry.instance().get(key);
        }
        if (key.equals("users") && sandbox.getUsers().isCanWriteDb()) {
            return Context.getUserController();
        }
        if (key.equals("users") && sandbox.getUsers().isCanReadDb()) {
            return Context.getUserController().getReadonlyWrapper();
        }
        if (sandbox.getWhitelist().getWriteBuckets().contains(key)) {
            return DataAccessRegistry.instance().get(key);
        }
        if (sandbox.isCanReadAllData()) {
            return DataAccessRegistry.instance().get(key).getReadonlyWrapper();
        }
        if (sandbox.getWhitelist().getReadBuckets().contains(key)) {
            return DataAccessRegistry.instance().get(key).getReadonlyWrapper();
        }
        throw new UsageException("You do not have access to the data bucket " + key);
    }

    @Override
    public ModelController put(String key, ModelController value) {
        return null;
    }

    @Override
    public ModelController remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends ModelController> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<String> keySet() {
        return null;
    }

    @Override
    public Collection<ModelController> values() {
        return null;
    }

    @Override
    public Set<Entry<String, ModelController>> entrySet() {
        return null;
    }

    @Override
    public ModelController getOrDefault(Object key, ModelController defaultValue) {
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super ModelController> action) {

    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super ModelController, ? extends ModelController> function) {

    }

    @Override
    public ModelController putIfAbsent(String key, ModelController value) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(String key, ModelController oldValue, ModelController newValue) {
        return false;
    }

    @Override
    public ModelController replace(String key, ModelController value) {
        return null;
    }

    @Override
    public ModelController computeIfAbsent(String key, Function<? super String, ? extends ModelController> mappingFunction) {
        return null;
    }

    @Override
    public ModelController computeIfPresent(String key, BiFunction<? super String, ? super ModelController, ? extends ModelController> remappingFunction) {
        return null;
    }

    @Override
    public ModelController compute(String key, BiFunction<? super String, ? super ModelController, ? extends ModelController> remappingFunction) {
        return null;
    }

    @Override
    public ModelController merge(String key, ModelController value, BiFunction<? super ModelController, ? super ModelController, ? extends ModelController> remappingFunction) {
        return null;
    }
}
