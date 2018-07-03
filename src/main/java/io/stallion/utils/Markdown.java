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
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

/**
 * Default markdown processor for most stallion uses.
 * Includes support for fenced code blocks, footnotes, smart quotes, tables, and rawHtml tags.
 */
public class Markdown {
    private static Markdown _instance = null;

    public static Markdown instance() {
        if (_instance == null) {
            _instance = new Markdown();
        }
        return _instance;
    }

    protected Markdown() {

    }

    public String process(String itemContent) {
        itemContent = new MarkdownFootnotesProcessor(itemContent).process();
        ParsedContent parsed = parseOutRawHtml(itemContent);
        PegDownProcessor pegdownProcessor = new PegDownProcessor(
                Extensions.FENCED_CODE_BLOCKS |
                        Extensions.AUTOLINKS |
                        Extensions.STRIKETHROUGH |
                        Extensions.TABLES |
                        Extensions.QUOTES |
                        Extensions.SMARTS
        );
        itemContent = pegdownProcessor.markdownToHtml(parsed.content);
        itemContent = swapInRawHtml(itemContent, parsed);
        return itemContent;
    }

    private static class ParsedContent {
        private String content = "";
        private List<String> rawHtmls = list();
    }

    private static Pattern rawHtmlPattern = Pattern.compile("<rawHtml>([\\s|\\S]*?)</rawHtml>");
    private static String RAW_HTML_HOLDER = "@!RaWhTmL-FMLI5z0Ua9Hto-Qeum9b5UL3TCj=%s!@";

    protected ParsedContent parseOutRawHtml(String content) {
        ParsedContent parsed = new ParsedContent();
        Matcher matcher = rawHtmlPattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        for (Integer index: safeLoop(100)) {
            boolean found = matcher.find();
            if (!found) {
                break;
            }
            parsed.rawHtmls.add(matcher.group(1));
            matcher.appendReplacement(sb, String.format(RAW_HTML_HOLDER, index));
        }
        matcher.appendTail(sb);
        parsed.content = sb.toString();
        return parsed;
    }

    protected static Pattern rawHtmlHolderPattern = Pattern.compile("@!RaWhTmL=(\\d+)!@");
    protected String swapInRawHtml(String content, ParsedContent parsed) {
        if (parsed.rawHtmls.size() == 0) {
            return content;
        }
        Matcher matcher = rawHtmlHolderPattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        Log.info("Swapping in RawHtml to {0}", content);
        for (Integer index: safeLoop(100)) {
            boolean found = matcher.find();
            if (!found) {
                break;
            }
            Log.info("Match was: {0} {1} {2}", matcher.toString(), matcher.groupCount(), matcher.group(0));
            int pIndex = Integer.parseInt(matcher.group(1));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(parsed.rawHtmls.get(pIndex)));
            //matcher.appendReplacement(sb, parsed.rawHtmls.get(pIndex));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
