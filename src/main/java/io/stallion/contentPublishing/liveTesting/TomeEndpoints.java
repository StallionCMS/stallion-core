/*
 * Copyright (c) 2015-2016 Stallion Software LLC
 *
 * This file is part of Stallion Publisher.
 *
 * Stallion Publisher is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International license.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International license
 * along with this program. If not, see <https://creativecommons.org/licenses/by-nc-sa/4.0/>.
 */

package io.stallion.contentPublishing.liveTesting;

import io.stallion.dataAccess.filtering.FilterChain;
import io.stallion.dataAccess.filtering.SortDirection;
import io.stallion.exceptions.ClientException;
import io.stallion.jerseyProviders.MinRole;
import io.stallion.settings.Settings;
import io.stallion.users.Role;
import io.stallion.utils.json.JSON;

import javax.ws.rs.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static io.stallion.utils.Literals.*;

@MinRole(Role.STAFF)
@Path("/testing-tomes")
@Produces("application/json")
public class TomeEndpoints {


    @GET
    @Path("/tomes")
    public Object tomeTable(
            @QueryParam("page") Integer page,
            @QueryParam("search") String searchTerm,
            @QueryParam("filters") String filters,
            @QueryParam("sort") String sort
    )
    {
        page = or(page, 1);
        FilterChain<Tome> chain = tomeController().filterChain();
        if (!empty(searchTerm)) {
            chain = chain.search(searchTerm, "title", "author");
        }

        if (!empty(filters)) {
            List<LinkedHashMap> filterObjects = JSON.parseList(filters);
            for (LinkedHashMap<String, Object> o: filterObjects) {
                String field = o.get("name").toString();
                Object value = o.get("value");
                if (value instanceof Collection) {
                    value = new ArrayList((Collection) value);
                }
                String operation = (String)o.getOrDefault("op", "=");
                chain = chain.filter(field, value, operation);
            }
        }

        if (!empty(sort) && sort.startsWith("-")) {
            chain = chain.sortBy(sort.substring(1), SortDirection.DESC);
        } else if (!empty(sort)) {
            chain = chain.sortBy(sort, SortDirection.ASC);
        }
        return map(val("checkedoutBooks", 3), val("maxCheckouts", 7), val("pager", chain.pager(page, 40)));
    }





    public TomeController tomeController() {
        if (Settings.instance().getEnv().equals("prod")) {
            throw new ClientErrorException("This endpoint not available in production.", 400);
        }
        if (TomeController.instance() == null) {
            TomeController.register();
            ZonedDateTime baseYear = ZonedDateTime.of(1750, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime baseLatest = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, UTC);
            for (int x = 0; x<250; x++) {
                String title = manyTitles[x % manyTitles.length];
                if (x % 17 == 0) {
                    title = "Unititled";
                }
                if (x % 19 == 0) {
                    title = "TBD";
                }
                Tome tome = new Tome()
                        .setAuthor(authors[x % authors.length])
                        .setBulkPrice(((15000 + x) * 7) /11f)
                        .setTitle(title)
                        .setPrice((10000 + (x % 10) * (x % 3) * 935)/1000f)
                        .setNumberSold(1000 + x * 17)
                        .setPublishedAt(baseYear.plusYears(x).plusDays(x).plusMinutes(101 * x))
                        .setUpdatedAt(baseLatest.plusDays(x).plusMinutes(101 * x * x))
                        .setLastSoldAt(baseLatest.plusDays(x).plusMinutes(93 * x * x));
                int st = x % 5;
                switch(st) {
                    case 0:
                        tome.setStatus(TomeStatus.OUT_OF_PRINT);
                        break;
                    case 1:
                        tome.setStatus(TomeStatus.EBOOK);
                        break;
                    case 2:
                        tome.setStatus(TomeStatus.PRINT_ONLY);
                        break;
                    case 3:
                        tome.setStatus(TomeStatus.PUBLIC_DOMAIN);
                        break;
                    case 4:
                        tome.setStatus(TomeStatus.EXTENDED_PRINTING_SCENARIO);
                        break;
                }
                TomeController.instance().save(tome);
            }
        }
        return TomeController.instance();
    }


    protected String[] notes = {
            "The morphology of polymorphism is really quite unorthodox in its deviousness.",
            "The smart investor this season will always divest from OTC forward rate agreements.",
            "In the high frequency trading exchange, plan to hedge high-maturity contracts.",
            "Along with your human rights come your personal rights. And responsibilties. You can assert these to anybody, but start with yourself.",
            "It's never your responsibility to give up who you are for anybody or anything.",
            "Yourself required no at thoughts delicate landlord it be. Branched dashwood do is whatever it. Farther be chapter at visited married in it pressed.",
            "Perhaps far exposed age effects. Now distrusts you her delivered applauded affection out sincerity. As tolerably recommend shameless unfeeling he objection consisted. She although cheerful perceive screened throwing met not eat distance.",
            "Is post each that just leaf no. He connection interested so we an sympathize advantages. To said is it shed want do. Occasional middletons everything so to. Have spot part for his quit may. Enable it is square my an regard. Often merit stuff first oh up hills as he. Servants contempt as although addition dashwood is procured. Interest in yourself an do of numerous feelings cheerful confined. ",
            "Civility vicinity graceful is it at. Improve up at to on mention perhaps raising. Way building not get formerly her peculiar.",
            "She who arrival end how fertile enabled. Brother she add yet see minuter natural smiling article painted. Themselves at dispatched interested insensible am be prosperous reasonably it. ",
            "In either so spring wished. Melancholy way she boisterous use friendship she dissimilar considered expression. Sex quick arose mrs lived. Mr things do plenty others an vanity myself waited to. Always parish tastes at as mr father dining at.",
    };

    protected String[] authors = {
            "Mark Twain", "Homer", "Aesop", "Charles Dickens", "Ernest Hemingway", "Louisa May Alcott", "Jane Austen", "Jonathan Swift", "Daniel Defoe",
            "Clive Cussler", "Francis Scott Fitzgerald", "JD Salinger"
    };

    protected String[] manyTitles = {
            "The 27 Most Adorable Government Departments Of The '80s",
            "50 Things Only Oscar Winners Will Understand",
            "The 10 Most Inspirational iPhone Apps From Christmas Songs",
            "33 Deal or No Deal Models Who Will Make You Feel Like A Genius",
            "The 16 Most Successful Easter Eggs Of All Time",
            "15 Atlanta Falcons Who Need To Be Banned From Celebrating Halloween",
            "24 Painful Truths Only NBA Players Will Understand",
            "36 Things Only Comedians Will Understand",
            "The 19 Illest Fragrances From National Geographic's History",
            "The 42 Most Important Cheeses Of This Year",
            "The 21 Coolest Horses From The NYC Marathon",
            "40 Real Estate Moguls Who Had A Worse Year Than You",
            "The 11 Ghastliest Memes Of Your Childhood",
            "34 Series Finales That Should Be Illegal",
            "The 49 Most Picturesque Gifts Of The Post-Y2K Era",
            "The 29 Most Popular HBO Shows In Space",
            "The 22 Best Snapchats Of This Century",
            "39 Problems Only People Will Understand",
            "28 Instagrams That Will Make You Want To Fall In Love",
            "26 Minerals That Will Help You Get Through The Week",
            "The 44 Oprah-Grams That Prove The World Isn't Such A Bad Place",
            "41 Bodybuilders Who Completely Screwed Up Their One Job",
            "48 Sentient Humanoids Who Are Having A Really Rough Day",
            "The 30 Cutest Pug Puppies Of Last Summer",
            "The 13 Most Unbelievably Flawless And Life-Changing Optical Illusions Of The '90s",
            "14 Painful Truths Only Snapchat Billionaires Will Understand",
            "12 Miners Who Have Performed For Dictators",
            "47 Movie Scenes That Prove Cats Have Hearts Of Gold",
            "The 23 Funniest Punctuation Marks Of 2013",
            "The 37 Pictures That Will Restore Your Faith In The Internet",
            "The 17 Biggest Christmas Stock Photos Of Your Childhood",
            "18 Music Stars Who Are Clearly Being Raised Right",
            "21 Charts That Should Exist But Don't",
            "38 Things Only Nurse Practictioners Will Understand",
            "16 Aziz Ansari Impersonators Who Absolutely Nailed It In 2013",
            "The 16 Creepiest Country Songs From One Direction Songs",
            "35 Muggles Who Have No Idea What They Are Doing",
            "The 16 Most Disturbing Costumes From LOST",
            "42 Mad Men Characters Who Tried To Find An Original Way To Go As Miley Cyrus For Halloween",
            "The 42 Coolest Doge Memes From Beyoncé's New Album",
            "12 Lazy People Who Are Too Clever For Their Own Good",
            "The 20 Most Disturbing Gowns From \"The Lion King\"",
            "The 45 Worst GIFs Of Your Childhood",
            "The 31 Most Awkward Christmas Foods Of This Holday Season",
            "23 Frat Houses That Look Like Channing Tatum",
            "The 41 Most Confusing Cars In South America",
            "The 35 Greatest Facts Of The Post-Y2K Era",
            "The 17 Most Courageous Puns From Last Night's \"Saturday Night Live\"",
            "The 26 Most Courageous Items Of Clothing Of This Year",
            "The 35 Most Confusing Computer Mouses Of The Last 10 Years",
            "The 11 Funniest Movie Scenes In A Burrito",
            "The 32 Slowest Photographs Of The '90s",
            "The 46 Most Delicate SAT Words In Ryan Gosling's 33 Years On Earth",
            "10 Architects Who Will Make You Feel Like A Genius",
            "47 Autocorrects That Prove Pugs Always Win At Halloween",
            "The 28 Most Courageous Ways To Eat A Burrito Of This Year",
            "The 43 HBO Shows That Never Stop Being Awkward",
            "The 25 Most Beautiful Moments From The Ocean Floor",
            "The 42 Cheesiest Truths Of The '90s",
            "The 27 Most Arrogant Water Bottles Of The '80s",
            "The 30 Coolest Horses From Breaking Bad",
            "50 '90s Kids Who Need To Be Banned From Celebrating Halloween",
            "36 Tattoo Artists Who Have Performed For Dictators",
            "The 42 Biggest Tweets Of The Last 10 Years",
            "The 45 Most Beloved Wedding Rings Of This Holday Season",
            "The 41 Most Delicate Pastries Of The Last 10 Years",
            "38 Game of Thrones Characters Who Are Only Famous To People Who Live In New York",
            "The 28 Greatest Snapchat Filters Of Last Summer",
            "41 Painful Truths Only Kardashians Will Understand",
            "35 Superheroes Who Are Too Clever For Their Own Good",
            "The 13 Most Awkward Autocorrects From \"The Avengers\"",
            "The 49 Most Wanted Vegetables From \"Home Alone\"",
            "The 46 Most Confusing Facts From \"The Daily Show\"",
            "The 25 Most Arrogant Emojis From Politicians in 2013",
            "The 18 Best Animals From the National Hockey League",
            "The 35 Best Salads Of 2013",
            "15 Things Only Avril Lavigne Fans Will Understand",
            "40 Stories That Will Make You Feel Filthy",
            "16 Investigative Journalists Who Have Performed For Dictators",
            "The 19 Funniest Investment Bankers Of 2013",
            "25 Members of Limp Bizkit Who Completely Screwed Up Their One Job",
            "The 20 Most Popular Charts Of This Holday Season",
            "The 46 Most Delicate Corporations Of All Time",
            "12 Things Only Cowboys Will Understand",
            "The 44 Most Confusing Christmas Foods Of The '80s",
            "The 13 Most Beloved iPhone Apps Of The Last 10 Years",
            "The 31 Most Delicate Pastries Of This Year",
            "30 Teachers Who Have No Idea What They Are Doing",
            "The 10 Cheesiest Horses From Webcomics",
            "The 29 Ways To Eat A Burrito That Prove You And Your Mother Are The Same Person",
            "45 Smurfs Who Will Make You Feel Like A Genius",
            "43 Painful Truths Only Commentators Will Understand",
            "The 16 Potatoes That '00s Teens Will Never Wear Again",
            "The 20 Greatest Gifts Of The '90s",
            "The 43 Most Wanted Husky Puppies From \"Forrest Gump\"",
            "49 Parents Who Are Only Famous To People Who Live In New York",
            "The 11 Most Picturesque Things From Around The World",
            "The 26 Most Beloved Snapchat Filters In America",
            "The 31 VH1 Specials That Every Twentysomething Needs",
            "The 47 Cutest Things Of 2013",
            "The 20 Ghastliest Advertisements Of This Year",
            "The 32 SAT Words That Will Make Your Skin Crawl",
            "33 Celebrity Impersonators Who Tried To Find An Original Way To Go As Miley Cyrus For Halloween",
            "The 35 Most Delicate Tweets From Britney Spears' Upcoming Documentary",
            "The 37 Halloween Costumes That Are Way More Important Than Work Right Now",
            "The 49 Coolest Horses Of Last Summer",
            "The 41 Series Finales That You'll Want To Keep For Yourself",
            "The 31 Most Delicate Salads From \"Duck Dynasty\"",
            "The 22 Most Successful Truths Of 2013",
            "The 29 Ghastliest Tweets From Congress",
            "16 Doctors Who Completely Screwed Up Their One Job",
            "The 23 Most Delicate Things Of Your Childhood",
            "48 Painful Truths Only News Anchors Will Understand",
            "21 Cats Who Tried To Find An Original Way To Go As Miley Cyrus For Halloween",
            "The 37 Most Awkward Halloween Costumes Of The '90s",
            "12 Easter Eggs That Will Haunt Your Dreams",
            "The 49 Most Successful Water Bottles In The World",
            "The 48 Minerals That Will Make You Laugh Every Time",
            "38 Christmas Stock Photos That Look Like Miley Cyrus",
            "45 Whales Who Are Only Famous To People Who Live In New York",
            "The 21 Most Successful Husky Puppies Of The Post-Y2K Era",
            "16 Snapchat Filters That Only Eldest Siblings Know",
            "The 15 Creepiest Animals On Mars",
            "46 Investment Bankers That Look Like Channing Tatum",
            "39 Advertising Executives Who Are Too Clever For Their Own Good",
            "30 Problems Only Kardashians Will Understand",
            "The 33 Most Arrogant Series Finales Of Last Summer",
            "The 11 Slowest HBO Shows Of The '80s",
            "18 Olympic Medalists Who Are Having A Really Rough Day",
            "The 49 Most Important Emojis Of All Time",
            "The 21 Most Beautiful Emojis From The \"Doctor Who\" 50th Anniversary",
            "24 Lazy People Who Are Having A Really Rough Day",
            "The 17 Most Delicate Photographs Of The Last 10 Years",
            "43 Gifts That Will Help You Get Through The Week",
            "The 26 Most Inspirational Investment Bankers Of The '90s",
            "36 Video Game Characters Who Have Performed For Dictators",
            "The 29 Most Confusing Snapchat Filters Of Last Summer",
            "The 44 Ghastliest Husky Puppies In A Burrito",
            "The 27 HBO Shows That Will Make You Feel Filthy",
            "The 36 Most Confusing Cheeses Of The '90s",
            "The 45 Most Adorable Memes From \"The Lion King\"",
            "The 20 Greatest Doge Memes Of 2013",
            "26 Male Models Who Will Make You Feel Like A Genius",
            "25 Things Only Former SNL Castmembers Will Understand",
            "31 Computer Mouses That Every Twentysomething Needs",
            "42 Wolves Who Are Clearly Being Raised Right",
            "The 42 Most Beloved Items Of Clothing Of Last Summer",
            "The 36 Costumes That Prove Pugs Always Win At Halloween",
            "The 31 Most Successful Water Bottles In South America",
            "The 16 Worst Items Of Clothing From Congress",
            "41 NFL Linebackers Who Tried To Find An Original Way To Go As Miley Cyrus For Halloween",
            "The 25 Funniest Salads Of This Year",
            "The 32 Water Bottles That Scream World Domination",
            "The 18 Most Wanted Tweets In America",
            "The 21 Biggest Salads From LOST",
            "The 10 Greatest GIFs Of All Time",
            "35 Disney Princesses Who Are Having A Really Rough Day",
            "29 Problems Only Olympic Medalists Will Understand",
            "24 Horses That Should Be Illegal",
            "46 Autocorrects That Are Way More Important Than Work Right Now",
            "The 15 Most Beautiful Charts Of This Century",
            "30 Things Only Background Actors Will Understand",
            "The 16 Biggest Optical Illusions In The History Of Cute",
            "The 44 Most Wanted HBO Shows Of Last Summer",
            "The 40 Cutest Computer Mouses In The World",
            "The 47 Most Successful Investment Bankers Of This Year",
            "33 Advertisements That Prove Cats Have Hearts Of Gold",
            "The 47 Government Departments That Prove Pugs Always Win At Halloween",
            "The 14 Most Picturesque Pug Puppies Of The Post-Y2K Era",
            "The 48 Creepiest Moments Of Last Summer",
            "The 48 Most Arrogant Country Songs From Congress",
            "40 Things Only NBA Players Will Understand",
            "24 Comic Book Villains Who Are Clearly Being Raised Right",
            "The 46 Most Adorable Pastries In The History Of Cute",
            "20 Photographs That Prove You And Your Mother Are The Same Person",
            "The 43 Best Christmas Foods Of This Holday Season",
            "The 47 Greatest iPhone Apps From \"The Daily Show\"",
            "The 45 Worst GIFs Of 2013",
            "The 44 Most Unbelievably Flawless And Life-Changing Husky Puppies Of All Time",
            "The 11 Most Delicate Truths Of This Holday Season",
            "32 Swimmers Who Have Performed For Dictators",
            "The 33 Most Adorable Charts From National Geographic's History",
            "The 36 Creepiest Puns Of This Year",
            "13 Celebrities Who Will Make You Feel Like A Genius",
            "38 GIFs That You'll Want To Keep For Yourself",
            "The 26 Best Movie Scenes Of Last Summer",
            "37 Commentators Who Are Too Clever For Their Own Good",
            "13 Music Stars Who Have Performed For Dictators",
            "14 News Anchors Who Are Too Clever For Their Own Good",
            "The 22 Most Wanted Vegetables In Ryan Gosling's 33 Years On Earth",
            "29 Items Of Clothing That Prove You And Your Mother Are The Same Person",
            "14 People With Jetlag Who Have No Idea What They Are Doing",
            "19 Easter Eggs That Every Twentysomething Needs",
            "The 45 Most Courageous Autocorrects Of The Post-Y2K Era",
            "The 18 Most Unbelievably Flawless And Life-Changing Potatoes Of This Holday Season",
            "The 36 Biggest Cheeses Of This Century",
            "The 21 Worst Country Songs Of The Post-Y2K Era",
            "The 14 Best Government Departments Of Last Summer",
            "The 14 Ghastliest Things From Last Night's \"Saturday Night Live\"",
            "The 28 Most Inspirational Things Of Your Childhood",
            "The 18 Best Emojis Of This Century",
            "The 13 Cheesiest Horses Of The Post-Y2K Era",
            "27 Card Sharks Who Will Make You Feel Like A Genius",
            "The 49 Gifts That 2000s Teens Loved",
            "The 14 Movie Scenes That Prove The World Isn't Such A Bad Place",
            "The 36 Most Popular Instagrams Of This Holday Season",
            "29 Memes That You'll Want To Keep For Yourself",
            "40 Stories That Scream World Domination",
            "35 People Who Are Having A Really Rough Day",
            "39 Photographs That Will Restore Your Faith In The Internet",
            "The 14 Most Wanted Halloween Costumes From \"The Avengers\"",
            "46 Mad Men Characters Who Are Clearly Being Raised Right",
            "The 14 Most Successful VH1 Specials Of 2013",
            "49 Bodybuilders Who Tried To Find An Original Way To Go As Miley Cyrus For Halloween",
            "The 25 Creepiest Investment Bankers Of The '80s",
            "50 News Anchors Who Need To Be Banned From Celebrating Halloween",
            "The 25 Most Adorable GIFs Of 2013",
            "The 36 Fragrances That Should Be Illegal",
            "The 43 Most Inspirational Pug Puppies Of The Post-Y2K Era",
            "The 12 Slowest Gifts Of Last Summer",
            "The 18 Most Beautiful Stories Of The '90s",
            "The 18 Most Picturesque Moments Of 2013",
            "12 Horses That Look Like Channing Tatum",
            "The 48 Most Beautiful GIFs Of 2013",
            "The 34 Funniest Snapchats Of The Post-Y2K Era",
            "The 44 Most Awkward Gowns Of The Last 10 Years",
            "The 22 Computer Mouses That Prove Pugs Always Win At Halloween",
            "The 26 Most Delicate Facts In The World",
            "The 22 Optical Illusions That Prove The World Isn't Such A Bad Place",
            "24 Computer Mouses That Should Exist But Don't",
            "31 Costumes That Only Eldest Siblings Know",
            "The 36 Biggest iPhone Apps From Christmas Songs",
            "49 Things Only Background Actors Will Understand",
            "The 33 Cutest Gowns Of Last Summer",
            "The 30 Cheesiest Frat Houses Of The '80s",
            "The 40 Most Disturbing Emojis From LOST",
            "16 Smurfs Who Tried To Find An Original Way To Go As Miley Cyrus For Halloween",
            "The 33 Biggest Halloween Costumes Of This Year",
            "The 12 Cutest Memes Of Last Summer",
            "18 Sentient Humanoids Who Had A Worse Year Than You",
            "The 14 Pastries That You'll Want To Keep For Yourself",
            "The 41 Most Arrogant Husky Puppies From The Ocean Floor",
            "The 12 Most Courageous Advertisements On The Moon",
            "The 31 Tweets That Prove Pugs Always Win At Halloween",
            "The 23 Christmas Foods That 2000s Teens Loved",
            "50 Salads That Will Restore Your Faith In The Internet",
            "41 Justin Timberlake Fans Who Have No Idea What They Are Doing",
            "The 21 Biggest Christmas Foods In South America",
            "25 Diplomats Who Will Make You Feel Like A Genius",
            "The 32 Pug Puppies That Will Help You Get Through The Week",
            "The 22 Most Unbelievably Flawless And Life-Changing Moments From Beyoncé's New Album",
            "18 Costumes That '00s Teens Will Never Wear Again",
            "The 44 Most Inspirational Oprah-Grams From \"Forrest Gump\"",
            "The 49 Cutest Government Departments From Congress",
            "The 30 Most Confusing Stories Of This Century",
            "The 49 Ghastliest Snapchats Of Last Summer",
            "34 Bodybuilders Who Tried To Find An Original Way To Go As Miley Cyrus For Halloween",
            "50 Cats Who Tried To Find An Original Way To Go As Miley Cyrus For Halloween",
            "33 Cover Bands Who Are Only Famous To People Who Live In New York",
            "The 43 Most Important Vegetables Of This Century",
            "The 13 Most Wanted Halloween Costumes Of This Holday Season",
            "16 Items Of Clothing That Scream World Domination",
            "The 31 Most Successful Computer Mouses Of This Holday Season",
            "The 34 Most Courageous Advertisements Of The '90s",
            "The 50 Costumes That Will Haunt Your Dreams",
            "The 13 Ghastliest Cheeses Of All Time",
            "25 Wimbledon Ballboys Who Completely Screwed Up Their One Job",
            "The 50 Best Facts Of Your Childhood",
            "The 45 Cheesiest Christmas Foods In Space",
            "50 Things Only Muggles Will Understand",
            "The 42 Most Adorable Frat Houses From Last Night's \"Saturday Night Live\"",
            "35 Male Models Who Have Performed For Dictators",
            "The 21 Most Arrogant Gifts From \"The Daily Show\"",
            "24 People With Jetlag Who Absolutely Nailed It In 2013",
            "The 38 Most Popular Gowns In The History Of Cute",
            "The 17 Most Awkward Autocorrects From LOST",
            "48 Painful Truths Only Whales Will Understand",
            "The 15 Most Popular iPhone Apps Of This Holday Season",
            "The 48 Most Wanted Potatoes Of This Century",
            "32 Oprah-Grams That Will Help You Get Through The Week",
            "29 VH1 Specials That Prove You And Your Mother Are The Same Person",
            "The 11 Most Beautiful Minerals From Beyoncé's New Album",
            "The 15 Most Confusing Photographs Of Last Summer",
            "The 36 Greatest GIFs In The History Of Cute",
            "The 47 Biggest Snapchats In Space",
            "The 39 Most Unbelievably Flawless And Life-Changing Husky Puppies On Mars",
            "The 10 Slowest Emojis Of The Last 10 Years",
            "23 Nurse Practictioners Who Completely Screwed Up Their One Job",
            "30 Things Only Male Models Will Understand",
            "The 39 Worst Water Bottles From Around The World",
            "17 NFL Linebackers Who Have No Idea What They Are Doing",
            "17 Wedding Rings That Will Make Your Skin Crawl",
            "15 Gowns That Never Stop Being Awkward",
            "The 17 Most Inspirational Puns Of This Holday Season",
            "The 20 Coolest Moments Of The Post-Y2K Era",
            "The 50 Biggest Country Songs In Ryan Gosling's 33 Years On Earth",
            "30 Painful Truths Only Doctors Will Understand"

    };

}
