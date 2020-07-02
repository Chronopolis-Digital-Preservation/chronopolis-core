package org.chronopolis.ingest.features;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.BagDistributionStatus;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.depositor.Depositor;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.enums.BagStatus;
import org.chronopolis.rest.models.enums.DataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Feature tests for BagUIController.
 * @author lsitu
 */
public class BagUIControllerTest extends TestBase {

    private static final String NOT_A_DEPOSITOR = "NOT A DEPOSITOR";
    private static final String STUCK_COLLECTION = "STUCK COLLECTION";

    private Depositor depositor;
    private StorageRegion  regionBag;
    private StorageRegion  regionToken;

    private Bag testBag = null;
    private Bag stuckBag = null;

    @Before
    public void initTest() {
        List<Node> nodes = Arrays.asList(testNode);
        depositor = createDepositor(TEST_DEPOSITOR, "Organization Name",
                "Organization Address", nodes);

        regionBag = createStorageRegion(DataType.BAG, testNode);

        regionToken = createStorageRegion(DataType.TOKEN, testNode);

    }

    @After
    public void done() {
        if (testBag != null) {
            dao.delete(testBag);
        }

        if (stuckBag != null) {
            dao.delete(stuckBag);
        }

        dao.delete(regionBag);
        dao.delete(regionToken);
        dao.delete(depositor);
    }

    @Test
    public void createBagTest() throws IOException {

        // load the form page
        HtmlPage formPage = webClient.getPage(getUrl("/bags/add"));

        // breadcrumbs
        String xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor depositorsLink = (HtmlAnchor) formPage.getFirstByXPath(xpath);
        assertThat(depositorsLink.getAttribute("href").toString()).isEqualTo("/bags/overview");

        xpath = "(//ol[@class='breadcrumb']/li)[2]";
        HtmlElement el = (HtmlElement) formPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Deposit Collection");

        //// Fill in the form

        HtmlForm form = formPage.getFormByName("ingest");

        // Collection Name
        xpath = "//div[@class='form-group row'][1]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Collection Name");
        HtmlTextInput elInput = formPage.getHtmlElementById("bag-name");
        elInput.setValueAttribute("test-bag");

        // Depositor
        xpath = "//div[@class='form-group row'][2]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Depositor");
        elInput = formPage.getHtmlElementById("depositor-name");
        elInput.setValueAttribute(TEST_DEPOSITOR);

        // Location
        xpath = "//div[@class='form-group row'][3]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Location");
        elInput = formPage.getHtmlElementById("location");
        elInput.setValueAttribute(TEST_DEPOSITOR + "/test-bag");

        // Storage Region
        xpath = "//div[@class='form-group row'][4]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).contains("Region");
        HtmlSelect elSelect = formPage.getHtmlElementById("storageRegion");
        elSelect.getOptionByValue("1").setSelected(true);

        // Size
        xpath = "//div[@class='form-group row'][5]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Size (In Bytes)");
        elInput = formPage.getHtmlElementById("size");
        elInput.setValueAttribute("1000");

        // Total Number of Files
        xpath = "//div[@class='form-group row'][6]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).contains("Files");
        elInput = formPage.getHtmlElementById("totalFiles");
        elInput.setValueAttribute("3");

        // Replication nodes
        xpath = "//div[@class='form-group row'][7]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).contains("To");
        elSelect = formPage.getHtmlElementById("replicatingNodes");
        elSelect.getOptionByValue(TEST_NODE).setSelected(true);

        HtmlSubmitInput submit = form.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage colPage = submit.click();

        String colUrl = colPage.getUrl().toString();
        long colId = Long.parseLong(colUrl.substring(colUrl.lastIndexOf("/") + 1));
        assertThat(colId).isPositive();

        testBag = dao.findOne(QBag.bag, QBag.bag.id.eq(colId));
        assertNotNull(testBag);

        //// new collection page

        //// validate breadcrumbs
        xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor collectionsLink 
          = (HtmlAnchor) colPage.getFirstByXPath(xpath);
        assertThat(collectionsLink.getAttribute("href").toString()).isEqualTo("/bags/overview");

        xpath = "(//ol[@class='breadcrumb']/li)[4]";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("test-bag");

        //// Validate properties

        // ID
        xpath = "(//table//tr)[1]/td[1]";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Id");
        xpath = "(//table//tr)[1]/td[@class='bg-light']";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo(String.valueOf(colId));

        // Total Files
        xpath = "(//table//tr)[2]/td[1]";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Total Files");
        xpath = "(//table//tr)[2]/td[@class='bg-light']";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("3");

        // Total Size
        xpath = "(//table//tr)[3]/td[1]";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Total Size");
        xpath = "(//table//tr)[3]/td[@class='bg-light']";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("1000 B");

        // Registered Tokens
        xpath = "(//table//tr)[4]/td[1]";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Registered Tokens");
        xpath = "(//table//tr)[4]/td[@class='bg-light']";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("0");

        // Status
        xpath = "(//table//tr)[5]/td[1]";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Status");
        xpath = "(//table//tr)[5]/td[@class='bg-light']";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("DEPOSITED");

        // Created By
        xpath = "(//table//tr)[6]/td[1]";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Created By");
        xpath = "(//table//tr)[6]/td[@class='bg-light']";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("user");

        // Created At
        xpath = "(//table//tr)[7]/td[1]";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Created At");
        xpath = "(//table//tr)[7]/td[@class='bg-light']";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent())
                .isEqualTo(testBag.getCreatedAt().toString());

        // updated At
        xpath = "(//table//tr)[8]/td[1]";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Updated At");
        xpath = "(//table//tr)[8]/td[@class='bg-light']";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent())
                .isEqualTo(testBag.getUpdatedAt().toString());
    }

    @Test
    public void listCollectionsTest() throws IOException {
        // initiate test collection
        testBag = createTestCollection();

        // load the list collections page
        HtmlPage colsPage = webClient.getPage(getUrl("/bags"));

        //// validate breadcrumbs, page size, search icon etc.

        String xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor collectionsLink = (HtmlAnchor) colsPage.getFirstByXPath(xpath);
        assertThat(collectionsLink.getAttribute("href").toString()).isEqualTo("/bags/overview");

        xpath = "(//ol[@class='breadcrumb']/li)[2]";
        HtmlElement el = (HtmlElement) colsPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("All");

        // items per page
        xpath = "(//ol[@class='breadcrumb']/li)[3]/label";
        el = (HtmlElement) colsPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("per page");

        HtmlSelect elSelect = (HtmlSelect)colsPage.getElementById("pageSize");
        assertThat(elSelect.getOptionByValue("25").isSelected()).isEqualTo(true);

        // search icon
        xpath = "(//ol[@class='breadcrumb']/li)[4]/a";
        HtmlAnchor searchLink = (HtmlAnchor) colsPage.getFirstByXPath(xpath);
        assertThat(searchLink.getAttribute("data-target").toString()).isEqualTo("#filter-body");

        //// verify collection properties

        xpath = "//table[@class='table table-hover']/tbody/tr[1]/td";
        List<HtmlElement> tds = colsPage.getByXPath(xpath);
        // ID
        assertThat(tds.get(0).getTextContent()).isEqualTo("" + testBag.getId());
        // Depositor
        assertThat(tds.get(1).getTextContent()).isEqualTo(TEST_DEPOSITOR);
        // Collection Name
        assertThat(tds.get(2).getTextContent()).isEqualTo("test-bag");
        // Collection Status
        assertThat(tds.get(3).getTextContent()).isEqualTo("" + BagStatus.DEPOSITED);
        // Created At
        assertThat(tds.get(4).getTextContent())
                .isEqualTo(testBag.getCreatedAt().toLocalDate().toString());
        // updated At
        assertThat(tds.get(4).getTextContent())
                .isEqualTo(testBag.getUpdatedAt().toLocalDate().toString());
    }

    @Test
    public void updateBagStatusTest() throws IOException {
        // initiate test collection
        testBag = createTestCollection();

        // Load the form page
        HtmlPage formPage = webClient.getPage(getUrl("/bags/" + testBag.getId() + "/edit"));

        String xpath = "//div[@class='w-50']/h4";
        HtmlElement el = formPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Update Collection Status");

        HtmlForm form = formPage.getFormByName("collectionStatus");

        el = form.getFirstByXPath("//div[@class='form-group row']");
        assertThat(el.getTextContent()).contains(testBag.getName());

        HtmlSelect elSelect = form.getSelectByName("status");
        assertThat(elSelect.getOptionByValue("" + BagStatus.DEPOSITED).isSelected()).isEqualTo(true);

        // set new status to TOKENIZED and submit the form
        elSelect.getOptionByValue("" + BagStatus.TOKENIZED).setSelected(true);

        HtmlSubmitInput submit = form.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage colPage = submit.click();

        // verify new collection status: TOKENIZED
        xpath = "(//table//tr)[5]/td[@class='bg-light']";
        el = (HtmlElement) colPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("" + BagStatus.TOKENIZED);
    }

    @Test
    public void createBagWithNullDepositorTest() throws IOException {

        // Load the form page
        HtmlPage formPage = webClient.getPage(getUrl("/bags/add"));

        //// Fill in the form

        // Collection Name
        String xpath = "//div[@class='form-group row'][1]";
        HtmlElement el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Collection Name");
        HtmlTextInput elInput = formPage.getHtmlElementById("bag-name");
        elInput.setValueAttribute("test-bag");

        // Depositor
        xpath = "//div[@class='form-group row'][2]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Depositor");
        elInput = formPage.getHtmlElementById("depositor-name");
        elInput.setValueAttribute(NOT_A_DEPOSITOR);

        // Location
        xpath = "//div[@class='form-group row'][3]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Location");
        elInput = formPage.getHtmlElementById("location");
        elInput.setValueAttribute(TEST_DEPOSITOR + "/test-bag");

        // Storage Region
        xpath = "//div[@class='form-group row'][4]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Region");
        HtmlSelect elSelect = formPage.getHtmlElementById("storageRegion");
        elSelect.getOptionByValue("1").setSelected(true);

        // Size
        xpath = "//div[@class='form-group row'][5]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Size (In Bytes)");
        elInput = formPage.getHtmlElementById("size");
        elInput.setValueAttribute("1000");

        // Total Number of Files
        xpath = "//div[@class='form-group row'][6]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Files");
        elInput = formPage.getHtmlElementById("totalFiles");
        elInput.setValueAttribute("3");

        HtmlForm form = formPage.getFormByName("ingest");
        HtmlSubmitInput submit = form.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage newPage = submit.click();

        // fall back to the create collection form page
        assertThat(newPage.getUrl().toString()).endsWith("/bags/add");

        // error message
        String expectedError = "Depositor does not exist: " + NOT_A_DEPOSITOR;
        el = newPage.getFirstByXPath("//div[@class='error']");
        assertThat(el.getTextContent()).isEqualTo(expectedError);

        //// Validate properties are retained in the form

        // Collection Name
        elInput = newPage.getHtmlElementById("bag-name");
        assertThat(elInput.getValueAttribute()).isEqualTo("test-bag");

        // Depositor
        elInput = newPage.getHtmlElementById("depositor-name");
        assertThat(elInput.getValueAttribute()).isEqualTo(NOT_A_DEPOSITOR);

        // Location
        elInput = newPage.getHtmlElementById("location");
        assertThat(elInput.getValueAttribute()).isEqualTo(TEST_DEPOSITOR + "/test-bag");

        // Storage Region
        elSelect = newPage.getHtmlElementById("storageRegion");
        assertThat(elSelect.getOptionByValue("1").isSelected()).isEqualTo(true);

        // Size
        elInput = newPage.getHtmlElementById("size");
        assertThat(elInput.getValueAttribute()).isEqualTo("1000");

        // Total Number of Files
        elInput = newPage.getHtmlElementById("totalFiles");
        assertThat(elInput.getValueAttribute()).isEqualTo("3");
    }

    @Test
    public void stuckCollectionsPageSizeTest() throws IOException {
        // create a stuck collection
        stuckBag = createStuckCollection();

        // create a test collection
        testBag = createTestCollection();

        // load the stuck collections page
        HtmlPage colsPage = webClient.getPage(getUrl("/bags/stuck"));

        // page size: items per page
        String xpath = "(//ol[@class='breadcrumb']/li)[4]/label";
        HtmlElement el = (HtmlElement) colsPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("per page");

        HtmlSelect elSelect = (HtmlSelect)colsPage.getElementById("pageSize");
        assertThat(elSelect.getOptionByValue("25").isSelected()).isEqualTo(true);

        // verify only one row /collection
        xpath = "//table[@class='table table-hover']/tbody/tr";
        assertThat(colsPage.getByXPath(xpath).size()).isEqualTo(1);

        //// verify collection properties
        xpath = "//table[@class='table table-hover']/tbody/tr[1]/td";
        List<HtmlElement> tds = colsPage.getByXPath(xpath);
        // ID
        assertThat(tds.get(0).getTextContent()).isEqualTo("" + stuckBag.getId());
        // Depositor
        assertThat(tds.get(1).getTextContent()).isEqualTo(TEST_DEPOSITOR);
        // Collection Name
        assertThat(tds.get(2).getTextContent()).isEqualTo(STUCK_COLLECTION);
        // Collection Status
        assertThat(tds.get(3).getTextContent()).isEqualTo("" + BagStatus.DEPOSITED);

        //// now change the page size and validate it's selected in the new page loaded
        HtmlPage newPage = (HtmlPage) elSelect.getOptionByValue("20").setSelected(true);

        elSelect = (HtmlSelect)newPage.getElementById("pageSize");
        assertThat(elSelect.getOptionByValue("20").isSelected()).isEqualTo(true);

        // verify there is only one row in the new page
        xpath = "//table[@class='table table-hover']/tbody/tr";
        assertThat(newPage.getByXPath(xpath).size()).isEqualTo(1);

        // verify it's the stuck collection in the new page
        xpath = "//table[@class='table table-hover']/tbody/tr[1]/td";
        tds = newPage.getByXPath(xpath);
        assertThat(tds.get(0).getTextContent()).isEqualTo("" + stuckBag.getId());
        assertThat(tds.get(2).getTextContent()).isEqualTo(STUCK_COLLECTION);
    }

    /*
     * Create test collection.
     * @return
     */
    private Bag createTestCollection() {
        Bag bag = new Bag("test-bag", "test-creator", depositor, 1L, 1L, BagStatus.DEPOSITED);
        bag.addDistribution(testNode, BagDistributionStatus.DISTRIBUTE);

        dao.save(bag);

        return bag;
    }

    /*
     * Create stuck collection.
     * @return
     */
    private Bag createStuckCollection() {
        Bag bag = new Bag(STUCK_COLLECTION, "test-creator", depositor, 1L, 1L, BagStatus.DEPOSITED);
        bag.addDistribution(testNode, BagDistributionStatus.DISTRIBUTE);

        ZonedDateTime dateTime = ZonedDateTime.now();
        dateTime = dateTime.minusDays(15);
        bag.setCreatedAt(dateTime);
        bag.setUpdatedAt(dateTime);

        dao.save(bag);

        return bag;
    }
}
