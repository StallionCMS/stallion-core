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

package io.stallion.tools;

import io.stallion.boot.CommandOptionsBase;
import org.kohsuke.args4j.Option;


class DeployCommandOptions extends CommandOptionsBase {

    @Option(name="-user", usage="The name of the unix user with whom to connect to the server")
    private String user;

    @Option(name="-force", usage = "If true, will ignore all checks and publish the site anyway.")
    private boolean force = false;

    @Option(name="-fullRebuild", usage = "If true, will do a full rebuild of the site even if no files have changed.")
    private boolean fullRebuild = false;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getUser() {
        return user;
    }

    public DeployCommandOptions setUser(String user) {
        this.user = user;
        return this;
    }

    public boolean isFullRebuild() {
        return fullRebuild;
    }

    public DeployCommandOptions setFullRebuild(boolean fullRebuild) {
        this.fullRebuild = fullRebuild;
        return this;
    }
}


