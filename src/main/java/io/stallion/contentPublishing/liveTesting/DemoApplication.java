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

package io.stallion.contentPublishing.liveTesting;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.StallionApplication;
import io.stallion.boot.StallionRunAction;
import io.stallion.jobs.JobCoordinator;
import io.stallion.jobs.JobDefinition;
import io.stallion.services.Log;
import net.sf.ehcache.TransactionController;
import org.glassfish.jersey.server.ResourceConfig;


public class DemoApplication  extends StallionApplication {
    @Override
    public String getName() {
        return "demoapp";
    }


    @Override
    public List<? extends StallionRunAction> getExtraActions() {
        return list();
    }


    @Override
    public void onBuildJerseyResourceConfig(ResourceConfig rc) {

        // Register Endpoints

        List<Class> resources = list(
                DemoEndpoints.class
        );
        for (Class resource: resources) {
            rc.register(resource);

        }
    }

    @Override
    public void onRegisterAll() {

    }
}

