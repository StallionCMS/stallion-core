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


import io.stallion.StallionApplication;
import io.stallion.plugins.StallionJavaPlugin;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.ws.rs.core.Feature;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static io.stallion.utils.Literals.empty;


public abstract class AppIntegrationCaseBase extends JerseyIntegrationBaseCase {


    static {
        System.setProperty("java.awt.headless", "true");
    }





    public static void startApp(String folderName) throws Exception {
        startApp(folderName,  null, new Feature[0], true);
    }

    public static void startApp(String folderName, StallionApplication app) throws Exception {
        startApp(folderName,  app, new Feature[0], true);
    }



}
