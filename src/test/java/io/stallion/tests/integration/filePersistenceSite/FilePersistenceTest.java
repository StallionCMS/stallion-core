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

package io.stallion.tests.integration.filePersistenceSite;

import io.stallion.Context;
import io.stallion.dataAccess.MappedModel;
import io.stallion.dataAccess.MappedModelBase;
import io.stallion.dataAccess.ModelBase;
import io.stallion.dataAccess.ModelController;
import io.stallion.dataAccess.file.TextItem;
import io.stallion.testing.AppIntegrationCaseBase;
import io.stallion.utils.DateUtils;
import org.junit.*;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

public class FilePersistenceTest extends AppIntegrationCaseBase {


    @BeforeClass
    public static void setUpClass() throws Exception {
        cleanup();
        startApp("/file_persistence_site");
    }

    @After
    public void tearDown() throws Exception {
        cleanup();
    }

    public static void cleanup() throws Exception {
        URL resourceUrl = AppIntegrationCaseBase.class.
                getResource("/file_persistence_site/dossiers/jesse-james.json");
        if (resourceUrl != null) {
            Paths.get(resourceUrl.toURI()).toFile().delete();
        }
        resourceUrl = AppIntegrationCaseBase.class.
                getResource("/file_persistence_site/dossiers/100.json");
        if (resourceUrl != null) {
            Paths.get(resourceUrl.toURI()).toFile().delete();
        }


        resourceUrl = AppIntegrationCaseBase.class.
                getResource("/file_persistence_site/crimes/carmen-sandiego.json");
        if (resourceUrl != null) {
            Paths.get(resourceUrl.toURI()).toFile().delete();
        }
        resourceUrl = AppIntegrationCaseBase.class.
                getResource("/file_persistence_site/bios/lincoln.txt");
        if (resourceUrl != null) {
            Paths.get(resourceUrl.toURI()).toFile().delete();
        }


    }

    /**
     * Test the basic create/retrive/update methods for JSON Java Map based controllers
     * @throws Exception
     */
    @Test
    public void testJsonMapCrud() throws Exception {
        ModelController dossiers = Context.dal().get("dossiers");
        List<MappedModelBase> items = dossiers.filter("birth_year", 1867).all();
        Assert.assertEquals(1, items.size());
        MappedModelBase item = (MappedModelBase)items.get(0);
        Assert.assertEquals("Harry", item.get("first_name"));

        Long bounty = new Date().getTime();
        String lastName = "Long" + new Date().getTime();
        item.put("last_name", lastName);
        item.put("bounty", bounty);
        dossiers.save(item);

        // Reload from file and test again for the updates
        dossiers.reset();
        items = dossiers.filter("birth_year", 1867).all();
        MappedModel harry = (MappedModel)items.get(0);
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(lastName, harry.get("last_name"));
        Assert.assertEquals(bounty, harry.get("bounty"));

        // Add a new dossier
        MappedModelBase jesse = new MappedModelBase();
        jesse.setId(100L);
        jesse.put("first_name", "jesse");
        jesse.put("last_name", "James");
        jesse.put("birth_year", 1867);
        jesse.put("bounty", bounty);
        dossiers.save(jesse);

        // Now we should have two people born in 1867, test finding jesse
        items = dossiers.filter("birth_year", 1867).all();
        Assert.assertEquals(2, items.size());
        items = dossiers.filter("birth_year", 1867).filter("first_name", "jesse").all();
        Assert.assertEquals(1, items.size());
        jesse = (MappedModelBase)items.get(0);
        Assert.assertEquals("James", jesse.get("last_name"));
        Assert.assertEquals(bounty, jesse.get("bounty"));

        // Reset the controller to reload from file, test finding Jesse again
        dossiers.reset();
        items = dossiers.filter("birth_year", 1867).all();
        Assert.assertEquals(2, items.size());
        items = dossiers.filter("birth_year", 1867).filter("first_name", "jesse").all();
        Assert.assertEquals(1, items.size());
        jesse = (MappedModelBase)items.get(0);
        Assert.assertEquals("James", jesse.get("last_name"));
        Assert.assertEquals(bounty, jesse.get("bounty"));


        // Delete Jesse
        dossiers.hardDelete(jesse);

        // Verify Jesse was deleted
        items = dossiers.filter("birth_year", 1867).all();
        Assert.assertEquals(1, items.size());
        dossiers.reset();
        items = dossiers.filter("birth_year", 1867).all();
        Assert.assertEquals(1, items.size());


    }


    @Test
    public void testJsonClassCrud() throws Exception {
        List<Crime> crimes = Crime.dal().filter("state", "IL").all();
        Assert.assertEquals(2, crimes.size());

        Crime theft = Crime.dal().filter("city", "Chicago").all().get(0);
        Assert.assertEquals("The Hope Diamond Theft", theft.getName());
        Assert.assertEquals("Nermal", theft.getCriminals().get(0));
        Assert.assertEquals(12500000, theft.getCost());
        Assert.assertEquals("Baffling. Just baffling.", theft.getExtra().get("detective_notes"));
        Assert.assertEquals(18, theft.getExtra().get("victim_id"));
        Assert.assertEquals(CaseStatus.COLD, theft.getCaseStatus());
        Assert.assertEquals(1401080164229L, theft.getCreated());
        // Update theft crime
        theft.getCriminals().add("Jon");
        String technique = "Insider " + new Date().getTime();
        theft.setTechnique(technique);
        Crime.dal().save(theft);

        // Add a new crime
        Crime fraud = new Crime();
        fraud.setId(101L);
        fraud.setCity("New York");
        fraud.setState("NY");
        fraud.setCreated(DateUtils.mils());
        fraud.setCaseStatus(CaseStatus.COLD);
        //ZonedDateTime fraudDate = ZonedDateTime.of(2010, 10, 20, 10, 30, 45, 0, ZoneId.of("America/New York"));
        Date fraudDate = new Date(1419475885550L);
        fraud.setDate(fraudDate);
        fraud.setCost(1777000222);
        fraud.getCriminals().add("Carmen Sandiego");
        fraud.getExtra().put("victim_id", 21);
        Crime.dal().save(fraud);

        Crime.dal().reset();

        crimes = Crime.dal().filter("state", "IL").all();
        Assert.assertEquals(2, crimes.size());

        theft = Crime.dal().filter("city", "Chicago").all().get(0);
        Assert.assertEquals("Jon", theft.getCriminals().get(2));
        Assert.assertEquals(technique, theft.getTechnique());

        fraud = Crime.dal().filter("city", "New York").all().get(0);
        Assert.assertEquals(fraudDate, fraud.getDate());
        Assert.assertEquals(1777000222, fraud.getCost());
        Assert.assertEquals("Carmen Sandiego", fraud.getCriminals().get(0));
        Assert.assertEquals(21, fraud.getExtra().get("victim_id"));

        // Soft delete
        Crime.dal().softDelete(fraud);
        Crime.dal().reset();
        Crime fraud2 = Crime.dal().forIdWithDeleted(fraud.getId());
        Assert.assertEquals(true, fraud2.getDeleted());
        Assert.assertNull(Crime.dal().forId(fraud.getId()));
        List<Crime> frauds = Crime.dal().filter("city", "New York").all();
        Assert.assertEquals(0, frauds.size());

        // Hard delete
        Crime.dal().hardDelete(fraud);
        Assert.assertEquals(0, Crime.dal().filter("city", "New York").all().size());
        Crime.dal().reset();
        Assert.assertEquals(0, Crime.dal().filter("city", "New York").all().size());


    }

    @Test
    public void testMarkdownFileCrud() throws Exception {
        ModelController<TextItem> bios = (ModelController<TextItem>)Context.dal().get("bios");
        TextItem washington = bios.filter("title", "George Washington").first();
        Assert.assertEquals("Washington Irving", washington.getAuthor());
        Assert.assertEquals("1732-02-22", washington.get("birthDate"));

        // Update Washington
        String washBody = "Washington was the first president and leader of the continental army. Randomize " + new Date().getTime();
        String metaDescription = "the father of our nation" + new Date().getTime();
        washington.setMetaDescription(metaDescription);
        washington.setOriginalContent(washBody);
        bios.save(washington);

        // Add Lincoln
        TextItem lincoln = new TextItem();
        lincoln.setTitle("Abe Lincoln");
        lincoln.setId(103L);
        lincoln.put("birthState", "IL");
        String lincolnContent = "Lincoln was president during the Civil War. " + new Date().getTime();
        lincoln.setOriginalContent(lincolnContent);
        bios.save(lincoln);

        Assert.assertEquals(1, bios.filter("birthState", "IL").all().size());

        bios.reset();

        lincoln = bios.filter("birthState", "IL").first();
        Assert.assertNotNull(lincoln);
        Assert.assertEquals(lincolnContent, lincoln.getOriginalContent());
        Assert.assertEquals("Abe Lincoln", lincoln.getTitle());

        washington = bios.filter("title", "George Washington").first();
        Assert.assertEquals(washBody, washington.getOriginalContent());
        Assert.assertEquals(lincolnContent, lincoln.getOriginalContent());

        // Delete Lincoln
        bios.hardDelete(lincoln);
        Assert.assertEquals(0, bios.filter("birthState", "IL").all().size());
        bios.reset();
        Assert.assertEquals(0, bios.filter("birthState", "IL").all().size());

    }


}
