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

package io.stallion.testing;

import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

import static io.stallion.utils.Literals.map;


public class Stubbing {
    private static Map<String, StubHandler> handlersMap = map();
    private static Map<String, Integer> callCounts = map();
    private static Map<String, StubbingInfo> callerToInfo = map();

    public static void reset() {
        handlersMap = map();
        callCounts = map();
        callerToInfo = map();
    }

    public static StubbingInfo stub(Class cls, String method) {
        return stub(cls, method, new SimpleStubHandler(null));
    }

    public static StubbingInfo stub(Class cls, String method, Object result) {
        return stub(cls, method, new SimpleStubHandler(result));
    }

    public static StubbingInfo stub(Class cls, String method, StubHandler handler) {
        if (handler == null) {
            throw new UsageException("You cannot stub out with a null IStubHandler handler!");
        }
        Boolean hasMethod = false;
        for(Method clsMethod: cls.getDeclaredMethods()){
            if (clsMethod.getName().equals(method)) {
                hasMethod = true;
                break;
            }
        }
        if (!hasMethod) {
            throw new UsageException("Class " + cls.getName() + " has no method: " + method);
        }
        String caller = cls.getName() + "." + method;
        handlersMap.put(caller, handler);
        StubbingInfo info = new StubbingInfo();
        info.setCaller(caller);
        callCounts.put(caller, 0);
        callerToInfo.put(caller, info);
        return info;
    }

    public static void checkExecuteStub(Object ...params) throws StubbedOut {
        Object self = null;
        Object[] args = params;
        if (params.length > 0) {
            self = params[0];
            args = Arrays.copyOfRange(params, 1, params.length);
        }
        String caller = getCallingClassAndMethod();
        Log.info("Checking for stub method for caller: {0}", caller);
        StubHandler handler = handlersMap.getOrDefault(caller, null);
        if (handler == null) {
            return;
        }
        callCounts.put(caller, callCounts.get(caller) + 1);
        Object result = null;
        try {
            result = handler.execute(args);
        } catch(CarryOnWithDefault e) {
            return;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        throw new StubbedOut(result);
    }

    private static String getCallingClassAndMethod() {
        Throwable t = new Throwable();
        t.getStackTrace()[2].toString();
        String clz = t.getStackTrace()[2].getClassName();
        String method = t.getStackTrace()[2].getMethodName();
        return clz + "." + method;
    }

    public static void verifyAndReset() {
        verify();
        reset();
    }

    public static void verify() {
        for(Map.Entry<String, StubbingInfo> entry: callerToInfo.entrySet()) {
            StubbingInfo info = entry.getValue();
            String caller = entry.getKey();
            int actual = callCounts.get(entry.getKey());
            if (actual < entry.getValue().getMinCount()) {
                throw new AssertionError(MessageFormat.format(
                        "Expected {0} to be called at least {1} times, was called {2} times",
                        caller, info.getMinCount(), actual));
            }
            if (actual > info.getMaxCount()) {
                throw new AssertionError(MessageFormat.format(
                        "Expected {0} to be called not more than {1} times, was called {2} times",
                        caller, info.getMaxCount(), actual));
            }
        }
    }

    public static class StubbedOut extends Exception {
        public final Object result;

        public StubbedOut(Object result) {
            this.result = result;
        }
    }

    public static class CarryOnWithDefault extends Exception {

    }
}
