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

package io.stallion.boot;

/**
 * A way to implement executable actions that can be run via the command line.
 * When you run Stallion from the command line you pass in an action
 * as the first argument, such as "serve" or "new". Each action implements
 * this interface, and then is added to the list of actions in
 * io.stallion.Booter.
 *
 * @param <T>
 */
public interface StallionRunAction<T extends CommandOptionsBase> {

    /**
     * The name of the action, will be used on the command line to run the action.
     *
     * @return
     */
    public String getActionName();

    /**
     * A friendly description of what the action does, will be printed on the command-line
     * when help is asked for.
     *
     * @return
     */
    public String getHelp();

    /**
     *
     * Load the application context. Some actions just need the settings loaded. Other actions
     * need the settings plus all the services and data access layers. Other actions need
     * all the former plus need to start jobs and the async task coordinator. Call exactly
     * what you need to load in this method.
     *
     * @param options
     */
    public void loadApp(T options);

    public default String getSubActionName() {
        return "";
    }

    /**
     * Each action might have its own command-line arguments. Define your command-line
     * arguments by creating a subclass of BaseCommandOptions. Then override this method
     * to construct an instance of the subclass you created and return it.
     *
     * @param <T>
     * @return
     */
    public default <T extends CommandOptionsBase> T newCommandOptions() {
        return (T)new CommandOptionsBase();
    }

    /**
     * Actually do the action.
     *
     * @param options
     * @throws Exception
     */
    public void execute(T options) throws Exception;

}
