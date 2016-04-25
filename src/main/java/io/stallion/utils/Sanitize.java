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

package io.stallion.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.stallion.Context;
import io.stallion.dal.base.Model;
import io.stallion.services.Log;
import io.stallion.users.Role;
import io.stallion.utils.json.JSON;
import io.stallion.utils.json.RestrictedViews;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;


import java.util.regex.Pattern;


public class Sanitize {

    public static final  PolicyFactory
            STANDARD_POLICY = new HtmlPolicyBuilder()

            .allowStandardUrlProtocols()
                    // Allow title="..." on any element.
            .allowAttributes("title").globally()
                    // Allow href="..." on <a> elements.
            .allowAttributes("href").onElements("a")
                    // Defeat link spammers.
            .requireRelNofollowOnLinks()
                    // Allow lang= with an alphabetic value on any element.
            .allowAttributes("lang").matching(Pattern.compile("[a-zA-Z]{2,20}"))
            .globally()
                    // The align attribute on <p> elements can have any value below.
            .allowAttributes("align")
            .matching(true, "center", "left", "right", "justify", "char")
            .onElements("p")
                    // These elements are allowed.
            .allowElements(
                    "a", "p", "div", "i", "b", "em", "blockquote", "tt", "strong",
                    "br", "ul", "ol", "li")
                    // Custom slashdot tags.
                    // These could be rewritten in the sanitizer using an ElementPolicy.
            .allowElements("quote", "ecode")

            .toFactory();

    public static final  PolicyFactory
            STANDARD_POLICY_WITH_IMAGES = new HtmlPolicyBuilder()

            .allowStandardUrlProtocols()
                    // Allow title="..." on any element.
            .allowAttributes("title").globally()
                    // Allow href="..." on <a> elements.
            .allowAttributes("href").onElements("a")
                    // Defeat link spammers.
            .requireRelNofollowOnLinks()
                    // Allow lang= with an alphabetic value on any element.
            .allowAttributes("lang").matching(Pattern.compile("[a-zA-Z]{2,20}"))
            .globally()
                    // The align attribute on <p> elements can have any value below.
            .allowAttributes("align")
            .matching(true, "center", "left", "right", "justify", "char")
            .onElements("p")
                    // These elements are allowed.
            .allowElements(
                    "a", "p", "div", "i", "b", "em", "blockquote", "tt", "strong",
                    "br", "ul", "ol", "li", "img")
                    // Custom slashdot tags.
                    // These could be rewritten in the sanitizer using an ElementPolicy.
            .allowAttributes("src", "alt").onElements("img")
            .allowElements("quote", "ecode")
            .toFactory();

    public static final PolicyFactory BLOCK_ALL_POLICY = new HtmlPolicyBuilder()
            .allowElements()
            .toFactory();


    public static Pattern stripTagsPattern = Pattern.compile("<[^>]*>");
    /** Strips all HTML */
    public static String stripAll(String s) {
        if (s == null) {
            return "";
        }
        return stripTagsPattern.matcher(s).replaceAll("");
    }

    /** Strips all dangerous javascript, all block HTML that could ruin the page
     *  Allows only a limited white list of tags
     * @param s
     * @return
     */
    public static String basicSanitize(String s) {
        if (s == null) {
            return "";
        }
        return STANDARD_POLICY.sanitize(s);
    }


    public static String basicSanitizeWithImages(String s) {
        if (s == null) {
            return "";
        }
        return STANDARD_POLICY_WITH_IMAGES.sanitize(s);
    }

    public static String escapeXml(String s) {
        return StringEscapeUtils.escapeXml11(s);
    }


    public static Object htmlSafeJson(Object obj) {

        String out = JSON.stringify(obj);
        out = out.replace("<", "\\u003c");
        return out;
    }

    /**
     * Gets the object in a JSON form that is safe for being outputted on a web page:
     * &lt;script&gt;
     *     var myObj = {{ utils.htmlSafeJson(obj, "member") }}
     * &lt;/script&gt;
     * @param obj
     * @param restrictionLevel - Uses the JsonView annotation to determine which properties of the object
     *                         should be outputed. Possible values are: unrestricted/public/member/owner/internal
     * @return
     */
    public static Object htmlSafeJson(Object obj, String restrictionLevel) {
        String out = "";
        try {
            restrictionLevel = restrictionLevel == null ? "public" : restrictionLevel.toLowerCase();
            if ("public".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Public.class, true);
            } else if ("unrestricted".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Unrestricted.class, false);
            } else if ("member".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Member.class, true);
            } else if ("owner".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Owner.class, true);
            } else if ("internal".equals(restrictionLevel)) {
                out = JSON.stringify(obj, RestrictedViews.Internal.class, true);
            } else {
                out = "Unknown restriction level: " + restrictionLevel;
            }
        } catch (JsonProcessingException ex) {
            String objId = obj.toString();
            if (obj instanceof Model) {
                objId = ((Model)obj).getId().toString();
            }
            String msg = "Error JSON.stringifying object {0}" + obj.getClass().getSimpleName() + ":" + objId;
            if (Context.getSettings().getDebug() || Context.getUser().isInRole(Role.ADMIN)) {
                out = msg + "\n\nStacktrace-----\n\n" + ExceptionUtils.getStackTrace(ex);
            }
            Log.exception(ex, msg);
        }
        out = out.replace("<", "\\u003c");
        return out;
    }
}
