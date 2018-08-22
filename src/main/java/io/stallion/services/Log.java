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

package io.stallion.services;

import io.stallion.Context;
import io.stallion.exceptions.ConfigException;
import io.stallion.monitoring.HealthTracker;
import io.stallion.settings.Settings;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.*;

import static io.stallion.utils.Literals.empty;

public class Log {
    //private static Logger logger = LogManager.getLogManager().getLogger("stallion");
    private static Logger logger;
    private static Handler handler;
    private static Handler fileHandler;
    private static boolean alwaysIncludeLineNumber = true;

    static {



        logger = Logger.getLogger("io.stallion");
        logger.config("");

        //TODO: configure the logger from settingsp
        logger.setUseParentHandlers(false);
        handler = new ConsoleHandler();
        //Level defaultLevel = Level.INFO;
        //handler.setLevel(defaultLevel);
        //logger.setLevel(defaultLevel);

        handler.setFormatter(new LogFormatter());
        logger.addHandler(handler);
        // Hack to fix a bug where ScssStylesheet will reset the global logger
        System.setProperty("java.util.logging.config.file", "noop");
    }

    public static Logger getLogger() {
        return logger;
    }

    /**
     * Disable logging to the console
     */
    public static void disableConsoleHandler() {
        Log.info("Silencing console logger.");
        logger.removeHandler(handler);
        handler.close();
    }

    /**
     * Start logging to the file defined by "logFile" in settings files
     */
    public static void enableFileLogger() {
        logger.removeHandler(handler);
        handler.close();
        String logPath = Settings.instance().getLogFile();
        if (!new File(logPath).getParentFile().isDirectory()) {
            new File(logPath).getParentFile().mkdirs();
        }
        try {
            fileHandler = new FileHandler(logPath, 50000000, 7, true);
        } catch (IOException e) {
            throw new ConfigException("Invalid log file path: " + logPath);
        }
        fileHandler.setFormatter(new LogFormatter());
        handler.setLevel(logger.getLevel());
        logger.addHandler(fileHandler);
        System.out.println("----->  Logging to file " + logPath + " at level " + logger.getLevel() + " ---->");
    }



    public static void setLogLevel(Level level) {
        handler.setLevel(level);
        logger.setLevel(level);
        if (fileHandler != null) {
            fileHandler.setLevel(level);
        }
    }

    public static Level getLogLevel() {

        if (logger == null) {
            return Level.OFF;
        } else if (logger.getLevel() == null) {

            return Level.INFO;
        } else {
            return logger.getLevel();
        }
    }


    public static void setLogLevelFromSettings() {
        if (!empty(Context.getSettings().getLogLevel())) {
            Level level = Level.parse(Context.getSettings().getLogLevel());
            handler.setLevel(level);
            logger.setLevel(level);
            if (fileHandler != null) {
                fileHandler.setLevel(level);
            }
        }
        if (!empty(Context.getSettings().getPackageLogLevels())) {
            LogFilter filter = new LogFilter(logger.getLevel(), Context.getSettings().getPackageLogLevels());
            logger.setFilter(filter);
        }
    }

    public static void fine(String message, Object ... args) {
        if (getLogLevel().intValue() > Level.FINE.intValue()) {
            return;
        }
        //System.out.println(message);
        if (alwaysIncludeLineNumber) {
            Throwable t = new Throwable();
            t.getStackTrace()[2].toString();
            String clz = t.getStackTrace()[2].getClassName().replace("io.stallion.", "");
            String method = t.getStackTrace()[2].getMethodName();
            logger.logp(Level.FINE, clz, method, message, args);
        } else {
            logger.logp(Level.FINE, "", "", message, args);
        }

    }

    public static void finer(String message, Object ... args) {
        if (getLogLevel().intValue() > Level.FINER.intValue()) {
            return;
        }

        //System.out.println(message);

        Throwable t = new Throwable();
        t.getStackTrace()[2].toString();
        String clz = t.getStackTrace()[2].getClassName().replace("io.stallion.", "");
        String method = t.getStackTrace()[2].getMethodName();
        logger.logp(Level.FINER, clz, method, message, args);

    }

    public static void finest(String message, Object ... args) {
        if (getLogLevel().intValue() > Level.FINEST.intValue()) {
            return;
        }
        //System.out.println(message);
        Throwable t = new Throwable();
        t.getStackTrace()[2].toString();
        String clz = t.getStackTrace()[2].getClassName().replace("io.stallion.", "");
        String method = t.getStackTrace()[2].getMethodName();
        logger.logp(Level.FINEST, clz, method, message, args);

    }

    public static void info(String message, Object ... args) {
        if (getLogLevel().intValue() > Level.INFO.intValue()) {
            return;
        }
        // Info statements don't include the class and line number, since that kills performance
        //System.out.println(message);
        if (alwaysIncludeLineNumber) {
            Throwable t = new Throwable();
            StackTraceElement stackTraceElement = t.getStackTrace()[1];
            String clz = stackTraceElement.getClassName().replace("io.stallion.", "");
            String method = stackTraceElement.getMethodName() + ":" + t.getStackTrace()[1].getLineNumber();
            logger.logp(Level.INFO, clz, method, message, args);
        } else {
            logger.logp(Level.INFO, "", "", message, args);
        }

    }

    public static void warning(String message, Object ... args) {
        if (getLogLevel().intValue() > Level.WARNING.intValue()) {
            return;
        }
        Throwable t = new Throwable();
        StackTraceElement stackTraceElement = t.getStackTrace()[1];
        String clz = stackTraceElement.getClassName().replace("io.stallion.", "");
        String method = stackTraceElement.getMethodName() + ":" + t.getStackTrace()[1].getLineNumber();
        logger.logp(Level.WARNING, clz, method, message, args);

    }



    public static void warn(String message, Object ... args) {
        if (getLogLevel().intValue() > Level.WARNING.intValue()) {
            return;
        }
        Throwable t = new Throwable();
        StackTraceElement stackTraceElement = t.getStackTrace()[1];
        String clz = stackTraceElement.getClassName().replace("io.stallion.", "");
        String method = stackTraceElement.getMethodName() + ":" + t.getStackTrace()[1].getLineNumber();
        logger.logp(Level.WARNING, clz, method, message, args);

    }

    public static void warn(Throwable ex, String message, Object ... args) {
        if (getLogLevel().intValue() > Level.WARNING.intValue()) {
            return;
        }
        if (args.length > 0) {
            message = MessageFormat.format(message, args);
        }
        Throwable t = new Throwable();
        StackTraceElement stackTraceElement = t.getStackTrace()[1];
        String clz = stackTraceElement.getClassName().replace("io.stallion.", "");
        String method = stackTraceElement.getMethodName() + ":" + t.getStackTrace()[1].getLineNumber();
        logger.logp(Level.WARNING, clz, method, message, ex);
    }

    /**
     * Logs a message, setting the class, method and source line number using the stack frame index passed in.
     * This is useful if you are wrapping a logging class, and want to include the line number from where
     * the wrapper method is called.
     *
     * @param frame - the stack frame to use. Use '2' to log to one level above the caller
     * @param level
     * @param message
     * @param args
     */
    public static void logForFrame(int frame, Level level, String message, Object ...args) {
        if (getLogLevel().intValue() > level.intValue()) {
            return;
        }
        Throwable t = new Throwable();
        StackTraceElement stackTraceElement = t.getStackTrace()[frame];
        String clz = stackTraceElement.getClassName().replace("io.stallion.", "");
        String method = stackTraceElement.getMethodName() + ":" + t.getStackTrace()[frame].getLineNumber();
        logger.logp(level, clz, method, message, args);
    }


    public static void exception(Throwable ex, String message, Object ... args) {
        HealthTracker.instance().logException(ex);
        if (getLogLevel().intValue() > Level.SEVERE.intValue()) {
            return;
        }
        if (args.length > 0) {
            message = MessageFormat.format(message, args);
        }
        // TODO: WTF -- figure out how the hell the handler got removed;
        if (logger.getHandlers().length == 0) {
            logger.addHandler(handler);
        }
        Throwable t = new Throwable();
        StackTraceElement stackTraceElement = t.getStackTrace()[1];
        String clz = stackTraceElement.getClassName().replace("io.stallion.", "");
        String method = stackTraceElement.getMethodName() + ":" + t.getStackTrace()[1].getLineNumber();
        logger.logp(Level.SEVERE, clz, method, message, ex);
    }

    public static boolean isAlwaysIncludeLineNumber() {
        return alwaysIncludeLineNumber;
    }

    public static void setAlwaysIncludeLineNumber(boolean alwaysIncludeLineNumber) {
        Log.alwaysIncludeLineNumber = alwaysIncludeLineNumber;
    }
}
