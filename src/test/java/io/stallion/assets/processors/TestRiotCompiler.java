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

package io.stallion.assets.processors;

import io.stallion.services.Log;
import io.stallion.testing.AppIntegrationCaseBase;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class TestRiotCompiler extends AppIntegrationCaseBase{
    @BeforeClass
    public static void setUpClass() throws Exception {
        startApp("/a_minimal_site");
    }


    @Test
    public void testCompile() throws Exception {
        RiotCompiler.load();
        String source = RiotCompiler.transform("<comment>\n<h1>Hello {name}</h1>\n<script>var self = this;\nself.name = 'Peter';</script>\n</comment>");
        Log.info(source);
        assertContains(source, "riot.tag2('comment'");
    }
}
