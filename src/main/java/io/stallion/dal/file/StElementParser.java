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

package io.stallion.dal.file;

import org.pegdown.PegDownProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StElementParser {
    public static HashMap <String, Pattern> elementIdToPattern = new HashMap <String, Pattern>();

    public static Pattern stElementPattern = Pattern.compile("<st\\-(?<id>[\\w\\-]+)(?<attrs>[^>]*)>(?<innerContent>[\\s\\S]*?)</st\\-\\k<id>>");
    public static Pattern dqAttributePattern = Pattern.compile("\\s(?<key>\\w+)=\"(?<value>[^\"]*)\"");
    public static Pattern sqAttributePattern = Pattern.compile("\\s(?<key>\\w+)='(?<value>[^']*)'");
    public static PegDownProcessor pegdownProcessor = new PegDownProcessor();

    public static String removeTags(String rawContent) {
        return stElementPattern.matcher(rawContent).replaceAll("");
    }

    public static Pattern getPatternForId(String id)
    {
        if (elementIdToPattern.containsKey(id)) {
            Pattern pattern = elementIdToPattern.get(id);
            if (pattern != null) {
                return pattern;
            }
        }
        Pattern pattern = Pattern.compile("<st\\-" + id + "[^>]*>[\\s\\S]*</st\\-" + id + ">");
        elementIdToPattern.put(id, pattern);
        return pattern;
    }

    public static ArrayList<StElement> parseElements(String content, Boolean isMarkdown) {
        //println("MATCH CONTENT " + content);
        Matcher matcher = stElementPattern.matcher(content + "    \n   \n   \nabc\n");
        ArrayList <StElement> elements = new ArrayList <StElement>();
        while (matcher.find()) {
            StElement ele = new StElement();
            elements.add(ele);

            ele.setRawInnerContent(matcher.group("innerContent"));
            ele.setTagAttributesString(matcher.group("attrs"));
            ele.setId(matcher.group("id"));
            if (isMarkdown) {
                ele.setContent(pegdownProcessor.markdownToHtml(ele.getRawInnerContent()));
            } else {
                ele.setContent(ele.getRawInnerContent());
            }
            ArrayList< Matcher > attrMatchers = new ArrayList< Matcher >();
            attrMatchers.add(sqAttributePattern.matcher(ele.getTagAttributesString()));
            attrMatchers.add(dqAttributePattern.matcher(ele.getTagAttributesString()));
            for (Matcher attrMatcher: attrMatchers) {
                while (attrMatcher.find()) {
                    String key = attrMatcher.group("key");
                    String value = attrMatcher.group("value");
                    ele.getAttributes().put(key, value);
                    if (key == "tag") {
                        ele.setTag(value);

                    }
                }
            }
            //println("FOUND ELE " + ele.id);
            //println("ATTRIBUTES " + ele.attributes.size() + " OBJ " + ele.attributes);
            //println("TAG " + ele.tag)  ;
            //println("CONTENT " + ele.content);

        }
        return elements;
    }
}
