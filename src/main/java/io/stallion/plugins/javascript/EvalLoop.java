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

import io.stallion.services.Log;
import jdk.nashorn.internal.lookup.Lookup;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.runtime.Context;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class EvalLoop {
    private static Global global;
    private static Context context;


    private static MethodHandle CALC = null;
    public static MethodHandle getCalcHandle() {
        if (CALC == null) {
            try {
                CALC = MethodHandles.publicLookup().findStatic(
                        EvalLoop.class, "calc", MethodType.methodType(Object.class, new Class[]{Object.class, String.class, Object.class}));
                //CALC = MethodHandles.publicLookup().findStatic(EvalLoop.class, "calc",
                        //
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        return CALC;
    }

    public static MethodHandle getCalcHandle2(Global global, Context context) {
        EvalLoop.global = global;
        EvalLoop.context = context;
        return Lookup.MH.findStatic(
                MethodHandles.lookup(),
                EvalLoop.class, "calc",
                Lookup.MH.type(Object.class, new Class[]{Object.class, String.class, Object.class}
                ));
    }
    static int test() throws Throwable {
        return 140;
    }
    public static Object calc(Object x, String s, Object c) {
        Log.info("Object 1 {0} O2: {1} c: {2}", x, s, c);
        return global.eval(x, s);
    }
}
