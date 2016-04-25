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

package io.stallion.hooks;

import io.stallion.exceptions.UsageException;
import io.stallion.monitoring.HealthTrackingHookHandler;
import io.stallion.restfulEndpoints.XSRFHooks;
import io.stallion.services.Log;
import io.stallion.settings.Settings;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;


public class HookRegistry {
    private Map<Class<? extends HookHandler>, List<HookHandler>> hooksByClass = new HashMap<>();
    private Map<Class<? extends ChainedHook>, List<ChainedHook>> chainsByClass = new HashMap<>();

    private static HookRegistry _instance;

    public static HookRegistry instance() {
        if (_instance == null) {
            throw new UsageException("HookRegistry ");
        }
        return _instance;
    }

    public static HookRegistry load() {
        _instance = new HookRegistry();
        registerDefaultHandlers();
        return _instance;
    }

    public static void shutdown() {
        _instance = null;
    }

    public static void registerDefaultHandlers() {
        XSRFHooks.register();
        instance().register(new HealthTrackingHookHandler());
    }

    public void register(HookHandler handler) {
        Class base = getAbstractBaseClass(handler.getClass());

        if (base == null) {
            throw new UsageException("You tried to register a handler that does not inherit from an abstract handler class. There is no way this handler can be triggered.");
        }

        if (!hooksByClass.containsKey(base)) {
            hooksByClass.put(base, list());
        }
        if (!hooksByClass.get(base).contains(handler)) {
            hooksByClass.get(base).add(handler);
        }
    }

    public void register(ChainedHook handler) {
        Class base = getAbstractBaseClass(handler.getClass());
        if (base == null) {
            throw new UsageException("You tried to register a handler that does not inherit from an abstract handler class. There is no way this handler can be triggered.");
        }
        if (!hooksByClass.containsKey(handler.getClass())) {
            chainsByClass.put(base, list());
        }
        if (!chainsByClass.get(base).contains(handler)) {
            chainsByClass.get(base).add(handler);
        }
    }

    private Class getAbstractBaseClass(Class cls) {
        Class base = cls.getSuperclass();
        if (base.isAssignableFrom(HookHandler.class)) {
            return null;
        }
        if (base.isAssignableFrom(ChainedHook.class)) {
            return null;
        }
        for (int x=0; x< 10;x++) {
            if (base == null) {
                return null;
            }
            if (Modifier.isAbstract(base.getModifiers())) {
                return base;
            }
            base = cls.getSuperclass();
        }
        return null;
    }

    public <T> void dispatch(Class<? extends HookHandler> handlerClass, T arg) {
        if (!Modifier.isAbstract(handlerClass.getModifiers())) {
            throw new UsageException("You can only dispatch events for an abstract class. Registered handlers will create implementations of this abstract class and then register them to be run when your event is dispatched.");
        }
        if (hooksByClass.containsKey(handlerClass)) {
            for (HookHandler handler : hooksByClass.get(handlerClass)) {
                try {
                    handler.handle(arg);
                } catch (Exception e) {
                    if (Settings.instance().isStrict()) {
                        throw new RuntimeException(e);
                    } else {
                        Log.exception(e, "Exception running handler {0} with arg {1}", handler.getClass().getName(), arg);
                    }
                }
            }
        }
    }

    public <T> T chain(Class<? extends ChainedHook> chainClass, T arg) {
        if (!Modifier.isAbstract(chainClass.getModifiers())) {
            throw new UsageException("You can only dispatch events for an abstract class. Registered handlers will create implementations of this abstract class and then register them to be run when your event is dispatched.");
        }
        if (chainsByClass.containsKey(chainClass)) {
            for (ChainedHook chain : chainsByClass.get(chainClass)) {
                arg = (T)chain.chain(arg);
            }
        }
        return arg;
    }
}
