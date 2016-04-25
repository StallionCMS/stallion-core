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

import java.io.PrintStream;
import java.util.List;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class TestResults {
    private String file = "";
    private String name = "";
    private List<TestResult> results = list();
    private int erroredCount = 0;
    private int succeededCount = 0;
    private int failedCount = 0;

    public TestResults(String name) {
        this.name = name;
    }

    public boolean hasAnyErrors() {
        return (erroredCount > 0) || (failedCount > 0);
    }

    public void printResults() {
        PrintStream err = System.err;
        err.println("\n\n\n---------------------------\n");
        err.println("Test results for file: '" + file + "' suite: '" + name + "'");
        err.printf("%s succeeded, %s errored, %s failed\n", succeededCount, erroredCount, failedCount);
        for (TestResult result: results) {
            if (result.errored) {
                err.printf("Exception: %s\t\tReason: %s\n", result.getName(), result.getMessage());
            } else if (!result.isSucceeded()) {
                err.printf("Failed:    %s\t\tReason: %s\n", result.getName(), result.getMessage());
            } else {
                err.printf("Succeeded: %s\n", result.getName());
            }
        }
        err.println("\n---------------------------\n\n\n");
    }

    public TestResults addResult(String name, boolean succeeded, boolean errored, Throwable e) {
        String msg = "";
        if (e != null) {
            msg = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        results.add(
                new TestResult()
                        .setName(name)
                        .setSucceeded(succeeded)
                        .setErrored(errored)
                        .setMessage(msg)
        );
        if (succeeded) {
            succeededCount++;
        } else if (errored) {
            erroredCount++;
        } else {
            failedCount++;
        }
        return this;
    }


    public List<TestResult> getResults() {
        return results;
    }

    public int getErroredCount() {
        return erroredCount;
    }

    public int getSucceededCount() {
        return succeededCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public String getFile() {
        return file;
    }

    public TestResults setFile(String file) {
        this.file = file;
        return this;
    }

    public String getName() {
        return name;
    }

    public TestResults setName(String name) {
        this.name = name;
        return this;
    }

    public static class TestResult {
        private String name = "";
        private boolean succeeded = true;
        private boolean errored = false;
        private String message = "";

        public String getName() {
            return name;
        }

        public TestResult setName(String name) {
            this.name = name;
            return this;
        }

        public boolean isSucceeded() {
            return succeeded;
        }

        public TestResult setSucceeded(boolean succeeded) {
            this.succeeded = succeeded;
            return this;
        }

        public boolean isErrored() {
            return errored;
        }

        public TestResult setErrored(boolean errored) {
            this.errored = errored;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public TestResult setMessage(String message) {
            this.message = message;
            return this;
        }
    }
}
