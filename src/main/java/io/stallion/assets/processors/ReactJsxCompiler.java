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

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.script.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


public class ReactJsxCompiler {

    private static boolean loaded = false;
    private static ScriptEngine nashorn;
    private static ReactScriptContextFactory contextFactory;
    private static ObjectPool<ScriptContext> pool;
    private static final ReentrantLock lock = new ReentrantLock();

    public static void load() throws Exception {
        if (loaded) {
            return;
        }
        boolean locked = lock.tryLock(10000, TimeUnit.MILLISECONDS);
        try {
            if (loaded) {
                return;
            }
            if (locked) {
                ScriptEngineManager mgr = new ScriptEngineManager();
                nashorn = mgr.getEngineByName("nashorn");
                contextFactory = new ReactScriptContextFactory(nashorn);
                pool = new GenericObjectPool(contextFactory);
                loaded = true;
            }
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    public static String transform(String source)  {
        try {
            load();
            String code = "/*Error compiling react code*/";
            ScriptContext ctx = pool.borrowObject();
            try {
                JSObject jsxTransformer = (JSObject) nashorn.eval("JSXTransformer", ctx);
                Invocable invocable = (Invocable) nashorn;
                String componentName = "Component";
                ScriptObjectMirror jsxMirror = (ScriptObjectMirror) invocable.invokeMethod(jsxTransformer, "transform", source);
                code = (String) jsxMirror.getMember("code");
            } finally {
                pool.returnObject(ctx);
            }

            return code;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
