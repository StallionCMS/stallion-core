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

package io.stallion.tests.unit;

import io.stallion.dataAccess.db.SqlMigrationAction;
import io.stallion.services.Log;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class TransformJavascriptMultiline {
    @Test
    public void testTransform() throws IOException {
        String source = IOUtils.toString(getClass().getResource("/samples/javascript-with-multiline-string.js"), "UTF-8");
        SqlMigrationAction action = new SqlMigrationAction();
        String transformed = action.transformJavascript(source);
        Log.info("Transformed {0}", transformed);
        assertEquals("print(\"the start\")\n" +
                "\n" +
                "thing.run(\"a\\nline 'one'\\nline 'two'\\nline three\\n  z\");\n" +
                "\n" +
                "print(\"The end of the script\")", transformed);
    }
}
