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

package io.stallion.forms;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import io.stallion.settings.Settings;
import io.stallion.utils.Encrypter;
import io.stallion.utils.GeneralUtils;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


public class SimpleFormTag implements Tag {

    public String interpret(TagNode tagNode, JinjavaInterpreter jinjavaInterpreter) {
        if (settings().getDisableFormSubmissions()) {
            return "Form submissions have been disabled in settings. Form tag will not show up.";
        }
        StringBuilder builder = new StringBuilder();

        String token = mils() + "|" + GeneralUtils.randomToken(16);
        String encryptedToken = Encrypter.encryptString(Settings.instance().getAntiSpamSecret(), token);

        builder.append("<form data-spam-token=\"" + encryptedToken
                + "\" id=\"stallion-contact-form\" class=\"pure-form st-contacts-form\">");
        for(Node node:tagNode.getChildren()) {
            builder.append(node.render(jinjavaInterpreter));
        }
        builder.append("</form>");
        return builder.toString();
    }


    public String getEndTagName() {
        return "endform";
    }


    public String getName() {
        return "simple_form";
    }
}

