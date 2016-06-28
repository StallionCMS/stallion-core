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

package io.stallion.tests.integration.textSite;

import io.stallion.Context;
import io.stallion.dataAccess.file.TextItem;
import io.stallion.dataAccess.file.TextItemController;
import io.stallion.services.Log;
import io.stallion.testing.MockResponse;
import io.stallion.testing.AppIntegrationCaseBase;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;


public class TextFileTest extends AppIntegrationCaseBase {


    @BeforeClass
    public static void setUpClass() throws Exception {
        Log.setLogLevel(Level.FINEST);
        startApp("/text_site");
    }

    @Test
    public void testPageParsing() {
        MockResponse response = client.get("/toml-meta-things");
        Assert.assertEquals(200, response.getStatus());

        TextItemController controller = (TextItemController)Context.dal().get("pages");
        TextItem item = (TextItem)controller.filter("slug", "/toml-meta-things").all().get(0);
        assertEquals("Freakizoid", item.getAuthor());
        assertEquals("Peter", ((List<Map>) item.get("editors")).get(0).get("name"));
        assertEquals("onething", ((List<String>) item.get("listOfThings")).get(0));
        assertEquals("This is a multiline string\n", item.get("multiString").toString());
    }

    @Test
    public void testFootnotes() throws IOException {
        TextItemController controller = (TextItemController)Context.dal().get("pages");

        TextItem item = (TextItem)controller.filter("slug", "/pegdown-footnotes").all().get(0);
        String expected = IOUtils.toString(getClass().getResource("/text_site/assets/pegdown-footnotes.txt"));

        assertContains(item.getContent(), "<sup id='fnref:1'><a href='#fn:1' rel='footnote'>1</a></sup>");
        assertContains(item.getContent(), "<sup id='fnref:3'><a href='#fn:3' rel='footnote'>3</a></sup>");
        assertContains(item.getContent(), "<li id='fn:2'> <p>This is the very long one.</p><p>That&rsquo;s the second paragraph.</p> <a href='#fnref:2'>&#8617;</a></li>");
        //assertContains(item.getContent(), expected);

        item = (TextItem)controller.filter("slug", "/footnoting").all().get(0);
        Log.info(item.getContent());
        assertContains(item.getContent(), "<sup id='fnref:3'><a href='#fn:3' rel='footnote'>3</a></sup>");
        assertContains(item.getContent(), "<li id='fn:2'> <p>multi</p><p>Second <em>para</em>. <a href=\"http://stallion.io/\">With link</a></p><p>Third para.</p> <a href='#fnref:2'>&#8617;</a></li>");

    }

    @Test
    public void testRawHtml() {
        MockResponse response = client.get("/with-raw-html");
        assertResponseContains(response, expectedRawHtml);
        assertResponseContains(response, secondExpectedRawHtml);
        assertResponseContains(response, "To wash my face clean after the dust of the road, and to drink, so the dry bread will not stick in my throat.");
    }




    @Test
    public void testOnePage()
    {
        MockResponse response = client.get("/faq");
        assertResponseContains(response, "<h1>Frequently Asked Questions</h1>");

        response = client.get("/");
        assertResponseContains(response, "This is the home page for the text_site page collection.");

    }


    @Test
    public void testPageVisible()
    {
        MockResponse response = client.get("/faq");
        assertResponseContains(response, "<h1>Frequently Asked Questions</h1>");

        response = client.get("/draft-page");
        assertResponseDoesNotContain(response, "The Draft Page", 404);

        response = client.get("/future-dated");
        assertResponseDoesNotContain(response, "The Future Page", 404);


    }


    @Test
    public void testListing()
    {
        MockResponse response = client.get("/posts");
        assertResponseContains(response, "<a href=\"/sane-blogging\">Sane Blogging</a>");
    }

    protected static String secondExpectedRawHtml = "A second rawHtml section.\n" +
            "\n" +
            "<script>\n" +
            "  /*\n" +
            "  The commenting stuff\n" +
            "              Blah\n" +
            "            */\n" +
            "</script>";

    protected static String expectedRawHtml = "$dollarSignsBreakThings = 12; /* $17 */;\n" +
            "<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+\"://platform.twitter.com/widgets.js\";fjs.parentNode.insertBefore(js,fjs);}}(document,\"script\",\"twitter-wjs\");</script>\n" +
            "<div class=\"indicates-required\"><span class=\"asterisk\">*</span> indicates required</div>\n" +
            "  <div>An indented block></div>\n" +
            "This should not turn into a blockquote.\n" +
            "\n" +
            "  Neither should this.\n" +
            "\n" +
            "Once more.\n" +
            "\n" +
            "\n" +
            "<link href=\"\" rel=\"stylesheet\" type=\"text/css\">\n" +
            "<style type=\"text/css\">\n" +
            "\t#an_embed_signup{ }\n" +
            "\t/* This is some crazy code similiar to markdown breaking code seen in the wild.\n" +
            "\t   This is more of the comment that broke us. */\n" +
            "</style>\n" +
            "<div id=\"mc_embed_signup\">\n" +
            "<form action=\"\" method=\"post\" id=\"mc-embedded-subscribe-form\" name=\"mc-embedded-subscribe-form\" class=\"validate\" target=\"_blank\" novalidate>\n" +
            "    <div id=\"mc_embed_signup_scroll\">\n" +
            "\n" +
            "<div class=\"indicates-required\"><span class=\"asterisk\">*</span> indicates required</div>\n" +
            "<div class=\"mc-field-group\">\n" +
            "\t<label for=\"mce-EMAIL\">Email Address  <span class=\"asterisk\">*</span>\n" +
            "</label>\n" +
            "\t<input type=\"email\" value=\"\" name=\"EMAIL\" class=\"required email\" id=\"mce-EMAIL\">\n" +
            "</div>\n" +
            "\n" +
            "\t<div id=\"mce-responses\" class=\"clear\">\n" +
            "\t\t<div class=\"response\" id=\"mce-error-response\" style=\"display:none\"></div>\n" +
            "\t\t<div class=\"response\" id=\"mce-success-response\" style=\"display:none\"></div>\n" +
            "\t</div>    <!-- real people should not fill this in and expect good things - do not remove this or risk form bot signups-->\n" +
            "\n" +
            "</form>";
}
