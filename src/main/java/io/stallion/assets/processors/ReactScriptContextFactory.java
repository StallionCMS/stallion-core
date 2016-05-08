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

package io.stallion.assets.processors;

import io.stallion.utils.ResourceHelpers;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class ReactScriptContextFactory extends BasePooledObjectFactory<ScriptContext> {
    private ScriptEngine nashorn;
    public ReactScriptContextFactory(ScriptEngine engine) {
        nashorn = engine;
    }
    @Override
    public ScriptContext create() throws Exception {
        ScriptContext ctx = new SimpleScriptContext();

        nashorn.eval("var process = {env:{}}", ctx); // node-modules expect that
        nashorn.eval("var global = this;", ctx); // react expects that
        nashorn.eval(ResourceHelpers.loadAssetResource("stallion", "/jslib/jvm-npm.js"), ctx);
        nashorn.eval(ResourceHelpers.loadAssetResource("stallion", "/assets/admin/react.js"), ctx);
        nashorn.eval(ResourceHelpers.loadAssetResource("stallion", "/assets/admin/react-jsx-transform.js"), ctx);
        return ctx;
    }

    @Override
    public PooledObject<ScriptContext> wrap(ScriptContext scriptContext) {
        return new DefaultPooledObject(scriptContext);
    }
}
