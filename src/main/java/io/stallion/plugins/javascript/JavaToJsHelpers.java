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

import io.stallion.exceptions.*;
import io.stallion.settings.Settings;
import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ScriptObject;
import jline.console.ConsoleReader;
import jline.internal.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class JavaToJsHelpers {
    private boolean sandboxed = false;
    private Sandbox box;

    public JavaToJsHelpers(Sandbox box) {
        if (box != null) {
            sandboxed = true;
            this.box = box;
        }
    }

    public String readFileToString(String file) {
        if (!sandboxed) {
            try {
                return FileUtils.readFileToString(new File(file), "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new UsageException("You cannot read a file from a sandboxed plugin.");
    }


    public Object toJava(Object obj) {
        if (!(obj instanceof ScriptObjectMirror)) {
            return obj;
        }
        ScriptObjectMirror so = (ScriptObjectMirror)obj;
        if (so.isArray()) {
            List items = list();
            for (String key: so.getOwnKeys(false)) {
                items.add(so.get(key));
            }
            return items;
        } else {
            Map<String, Object> o = map();
            for(String key: so.getOwnKeys(false)) {
                Object val = so.get(key);
                if (val instanceof ScriptObjectMirror && ((ScriptObjectMirror) val).isArray()) {
                    val = toJava((ScriptObjectMirror)val);
                }
                o.put(key, val);
            }
            return o;
        }
    }

    public boolean isClass(Object cls) {
        return cls instanceof Class;
    }

    public boolean isStatic(Object cls) {
        if (cls == null) {
            return false;
        }
        //jdk.internal.dynalink.beans.StaticClass
        if (cls instanceof StaticClass) {
            return true;
        }
        return false;
    }

    public void raiseRedirect(String msg, Integer status) {
        status = or(status, 302);
        throw new RedirectException(msg, status);

    }

    public void raiseNotFound(String msg) {
        throw new NotFoundException(msg);
    }



    public void raiseClientException(String msg, Integer status) {
        status = or(status, 400);
        throw new ClientException(msg, status);
    }

    public void raiseServerException(String msg, Integer status) {
        status = or(status, 500);
        throw new WebException(msg, status);
    }

    public void raiseAssertionError(String msg) {
        throw new AssertionError(msg);
    }

    public String getInput(String prompt) throws Exception {
        if (!Settings.instance().getLocalMode()) {
            Log.error("You can only call 'getInput' in local mode");
            return "";
        }

        ConsoleReader reader = new ConsoleReader();
        return reader.readLine(prompt);
    }

    public Object inspect(Object object) throws Exception {
        if (!Settings.instance().getLocalMode()) {
            Log.error("You can only call 'inspect' in local mode");
            return "";
        }
        Class<?> c = Class.forName("jdk.nashorn.internal.runtime.DebuggerSupport");//full package name
        //note: getConstructor() can return only public constructors,
        //you need to use

        Method method = c.getDeclaredMethod("valueInfos", Object.class, boolean.class);
        //valueInfos(Object object, boolean all)
        //Constructor<?> constructor = c.getDeclaredConstructor();
        method.setAccessible(true);
        Object[] objects = (Object[])method.invoke(null, object, true);
        //List<Map> returnObjects = new ArrayList<>();
        Map desc = new HashMap<>();
        for(Object o: objects) {
            Map map = new HashMap<>();

            Field field = o.getClass().getDeclaredField("key");
            field.setAccessible(true);
            Object key = field.get(o);

            Field field2 = o.getClass().getDeclaredField("valueAsString");
            field2.setAccessible(true);
            Object val = field2.get(o);
            desc.put(key.toString(), val.toString());

        }
        return desc;
    }

    public Object eval(ScriptObject o, Object scope, String source, boolean returnStuff) {
        if (!Settings.instance().getLocalMode()) {
            Log.error("You can only call 'eval' in local mode");
            return "";
        }
        try {
            //new jdk.nashorn.internal.runtime.DebugLogger().
            Class<?> c = Class.forName("jdk.nashorn.internal.runtime.DebuggerSupport");//full package name
            //note: getConstructor() can return only public constructors,
            //you need to use

            Method method = c.getDeclaredMethod("eval", ScriptObject.class, Object.class, String.class, boolean.class);

            //Constructor<?> constructor = c.getDeclaredConstructor();
            method.setAccessible(true);
            return method.invoke(null, o, scope, source, returnStuff);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    private Object getDebugSupport() {


        //return new NashornDebugHelper();
        try {
            Class<?> c = Class.forName("jdk.nashorn.internal.runtime.DebuggerSupport");//full package name
            //note: getConstructor() can return only public constructors,
            //you need to use
            Method method = c.getDeclaredMethod("eval", ScriptObject.class, Object.class, String.class, boolean.class);

            //Constructor<?> constructor = c.getDeclaredConstructor();
            method.setAccessible(true);
            return method;
            //constructor.setAccessible(true);
            //Object o = constructor.newInstance(null);
            //return o;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }


}
