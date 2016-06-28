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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;

/**
 * A helper class for building interactive, command-line prompts.
 */
public class Prompter {
    protected Scanner scanner;
    protected String message;
    protected List<String> choices;
    protected Pattern validPattern;
    protected boolean isPassword = false;

    protected String lastErrorMessage = "";
    protected int minLength = 1;

    public Prompter(String message) {
        this.scanner = new Scanner(System.in);
        this.message = message;
    }

    public Prompter(Scanner scanner, String message) {
        this.scanner = scanner;
        this.message = message;
    }

    public static String prompt(String msg) {
        return new Prompter(msg).prompt();
    }

    public boolean validate(String line) {
        return true;
    }

    public String failAndPromptAgain() {


        if (!empty(lastErrorMessage)) {
            System.out.println(lastErrorMessage);
            lastErrorMessage = "";
        } else {
            System.out.println("Sorry, that was not a valid response.");
        }
        return prompt();
    }

    public boolean yesNo() {
        setChoices("Y", "yes", "y", "n", "N", "no");
        String line = prompt();
        if (line.equals("Y") || line.equals("yes") || line.equals("y")) {
            return true;
        } else {
            return false;
        }
    }

    public String prompt() {
        System.out.print(message);
        String line = "";
        if (isPassword && System.console() != null) {
            line = new String(System.console().readPassword());
        } else {
            line = this.scanner.nextLine();
        }
        line = line.trim();
        if (line.length() < minLength && line.length() == 0) {
            lastErrorMessage = "Sorry, your response must be at least " + minLength + " character(s) long. Please make a choice.";
            return failAndPromptAgain();
        }
        if (!empty(choices)) {
            if (!choices.contains(line)) {
                lastErrorMessage = "Sorry, that was not one of the allowed choices.\n" +
                        "The allowed choices are: " + StringUtils.join(choices, ", ");

                return failAndPromptAgain();
            }
        }
        if (validPattern != null) {
            if (!validPattern.matcher(line).matches()) {
                lastErrorMessage = "Sorry, that was not a valid entry.";
                return failAndPromptAgain();
            }
        }
        return line;
    }

    public Prompter setValidPattern(String patternString) {
        this.validPattern = Pattern.compile(patternString);
        return this;
    }

    public Prompter setChoices(String...choices) {
        this.choices = Arrays.asList(choices);
        return this;
    }

    public Prompter setChoices(List<String> choices) {
        this.choices = choices;
        return this;
    }


    public Prompter setAllowEmpty(boolean allow) {
        if (allow) {
            this.minLength = 0;
        } else {
            this.minLength = 1;
        }
        return this;
    }

    public Prompter setMinLength(int minLength) {
        this.minLength = minLength;
        return this;
    }


    public Prompter setIsPassword(boolean isPassword) {
        this.isPassword = isPassword;
        return this;
    }


    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public Prompter setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
        return this;
    }


}
