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

package io.stallion.utils;


import io.stallion.services.Log;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.stallion.utils.Literals.UTF8;
import static io.stallion.utils.Literals.empty;

/**
 * A helper class for running external programs.
 */
public class ProcessHelper {
    private String[] args;
    private String directory = null;
    private String input;
    private Boolean showDotsWhileWaiting = true;
    private boolean inheritIO = true;
    private boolean quietMode = false;


    public static CommandResult run(String...args) {
        return new ProcessHelper(args).run();
    }


    public ProcessHelper(String...args) {
        this.args = args;
    }

    public ProcessHelper withDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    public CommandResult run() {

        String cmdString = String.join(" ", args);
        System.out.printf("----- Execute command: %s ----\n", cmdString);
        ProcessBuilder pb = new ProcessBuilder(args);
        if (!empty(directory)) {
            pb.directory(new File(directory));
        }
        Map<String, String> env = pb.environment();
        CommandResult commandResult = new CommandResult();
        Process p = null;
        try {
            if (showDotsWhileWaiting == null) {
                showDotsWhileWaiting = !inheritIO;
            }

            if (inheritIO) {
                p = pb.inheritIO().start();
            } else {
                p = pb.start();
            }

            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));

            if (!empty(input)) {
                Log.info("Writing input to pipe {0}", input);
                IOUtils.write(input, p.getOutputStream(), UTF8);
                p.getOutputStream().flush();
            }

            while (p.isAlive()) {
                p.waitFor(1000, TimeUnit.MILLISECONDS);
                if (showDotsWhileWaiting == true) {
                    System.out.printf(".");
                }
            }

            commandResult.setErr(IOUtils.toString(err));
            commandResult.setOut(IOUtils.toString(out));
            commandResult.setCode(p.exitValue());

            if (commandResult.succeeded()) {
                info("\n---- Command execution completed ----\n");
            } else {
                Log.warn("Command failed with error code: " + commandResult.getCode());
            }

        } catch (IOException e) {
            Log.exception(e, "Error running command: " + cmdString);
            commandResult.setCode(999);
            commandResult.setEx(e);
        } catch (InterruptedException e) {
            Log.exception(e, "Error running command: " + cmdString);
            commandResult.setCode(998);
            commandResult.setEx(e);
        }
        Log.fine("\n\n----Start shell command result----:\nCommand:  {0}\nexitCode: {1}\n----------STDOUT---------\n{2}\n\n----------STDERR--------\n{3}\n\n----end shell command result----\n",
                cmdString,
                commandResult.getCode(),
                commandResult.getOut(),
                commandResult.getErr());
        return commandResult;
    }

    protected void info(String msg) {
        if (quietMode == true) {

        }
        System.out.printf(msg + "\n");
    }

    public static class CommandResult {
        private int code;
        private String out;
        private String err;
        private Throwable ex;

        public boolean succeeded() {
            return code == 0;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getOut() {
            return out;
        }

        public void setOut(String out) {
            this.out = out;
        }

        public String getErr() {
            return err;
        }

        public void setErr(String err) {
            this.err = err;
        }

        public Throwable getEx() {
            return ex;
        }

        public void setEx(Throwable ex) {
            this.ex = ex;
        }
    }


    public String getInput() {
        return input;
    }

    public ProcessHelper setInput(String input) {
        this.input = input;
        return this;
    }

    public boolean isInheritIO() {
        return inheritIO;
    }

    public ProcessHelper setInheritIO(boolean inheritIO) {
        this.inheritIO = inheritIO;
        return this;
    }

    public boolean isShowDotsWhileWaiting() {
        return showDotsWhileWaiting;
    }

    public ProcessHelper setShowDotsWhileWaiting(boolean showDotsWhileWaiting) {
        this.showDotsWhileWaiting = showDotsWhileWaiting;
        return this;
    }
}
