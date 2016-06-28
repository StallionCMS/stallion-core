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

import io.stallion.services.Log;
import org.pegdown.PegDownProcessor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;


class MarkdownFootnotesProcessor {
    private String content = "";
    public MarkdownFootnotesProcessor(String content) {
        this.content = content;
    }




    private static final Pattern markerPattern = Pattern.compile("\\[\\^([^\\]]+)\\](?!:)");
    private static final Pattern notePattern = Pattern.compile("\\[\\^([^\\]]+)\\]:([^\n]*(\\n[ ]+[^\\n]*|\\n[ \t]*)*)"); //(\\Z|\\^\\S)



    public String process() {
        if (!content.contains("[^")) {
            return content;
        }
        if (content.contains("\r")) {
            content = content.replace("\r", "");
        }
        PegDownProcessor pegDownProcessor = new PegDownProcessor();
        StringBuffer builder = new StringBuffer();
        Matcher matcher = notePattern.matcher(content);
        Map<String, Footnote> footnoteByName = new HashMap<>();
        while (matcher.find()) {
            //Log.info("Found: \n<0>{0}</0>\n<1>{1}</1>\n<2>{2}</2>\n<3>{3}</3>", matcher.group(0), matcher.group(1), matcher.group(2), matcher.group(3));
            //matcher.
            String name = matcher.group(1);
            String note = matcher.group(2).trim();
            note = note.replaceAll("\n[ \t]+", "\n");
            footnoteByName.put(name,
                    new Footnote().setName(name).setNote(note)
            );
            matcher.appendReplacement(builder, "");
        }
        matcher.appendTail(builder);
        String content = builder.toString();

        builder = new StringBuffer();
        matcher = markerPattern.matcher(content);
        List<String> names = new ArrayList<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
            int num = names.size();
            // <sup id='fnref:1'><a href='#fn:1' rel='footnote'>1</a></sup>
            String anchor = MessageFormat.format("<sup id=''fnref:{0}''><a href=''#fn:{0}'' rel=''footnote''>{0}</a></sup>", num);
            matcher.appendReplacement(builder, anchor);
        }
        matcher.appendTail(builder);
        content = builder.toString();

        builder = new StringBuffer();
        builder.append(content);

        builder.append("<div class='footnotes'><hr /><ol>");

        for(int i = 0; i< names.size(); i++) {
            String name = names.get(i);
            int num = i + 1;
            Footnote footnote = footnoteByName.getOrDefault(name, null);
            if (emptyInstance(footnote)) {
                continue;
            }
            /**
             <li id='fn:1'>
             <p>And that&#8217;s the footnote. This is second sentence (same paragraph).</p>
             <a href='#fnref:1' rev='footnote'>&#8617;</a></li>
             * @return
             */
            String noteHtml = pegDownProcessor.markdownToHtml(footnote.getNote());
            //Log.info("NOTE: \n{0}\n\nHTML:\n{1}", footnote.getNote(), noteHtml);
            builder.append(MessageFormat.format("<li id=''fn:{0}''>\n{1}\n<a href=''#fnref:{0}''>&#8617;</a></li>", num, noteHtml));
        }
        builder.append("</ol></div>");
        return builder.toString();

    }

    static class Footnote {
        private String note;
        private String name;

        public String getNote() {
            return note;
        }

        public Footnote setNote(String note) {
            this.note = note;
            return this;
        }

        public String getName() {
            return name;
        }

        public Footnote setName(String name) {
            this.name = name;
            return this;
        }
    }
}
