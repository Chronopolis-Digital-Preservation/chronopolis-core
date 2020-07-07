package org.chronopolis.ingest.features;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.chronopolis.ingest.repository.dao.ReplicationDao;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.BagDistributionStatus;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.entities.QDataFile;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.QReplication;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.entities.depositor.Depositor;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.enums.BagStatus;
import org.chronopolis.rest.models.enums.DataType;
import org.chronopolis.rest.models.enums.ReplicationStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Feature tests for ReplicationUIController.
 * @author lsitu
 */
public class ReplicationUIControllerTest extends TestBase {

    private static final String REPLICATION_STATUS_PENDING = "PENDING";
    private static final String REPLICATION_STATUS_SUCCESS = "SUCCESS";
    private static final String TEST_BAG = "test-bag";
    private static final String TEST_CREATOR = "test-creator";

    private Depositor depositor;
    private StorageRegion  regionBag;
    private StorageRegion  regionToken;
    private Bag testBag;
    private Replication testRepl;

    @Before
    public void initTest() {
        List<Node> nodes = Arrays.asList(testNode);
        depositor = createDepositor(TEST_DEPOSITOR, "Organization Name",
                "Organization Address", nodes);

        regionBag = createStorageRegion(DataType.BAG, testNode);

        regionToken = createStorageRegion(DataType.TOKEN, testNode);

        testBag = createBagWithFiles(depositor, regionBag, regionToken, Arrays.asList(testNode));
    }

    @After
    public void done() {
        if (testRepl != null) {
            dao.delete(testRepl);
        }

        if (testBag != null) {
            Set<StagingStorage> storages = testBag.getStorage();

            testBag.getFiles().clear();
            testBag.getStorage().clear();
            dao.save(testBag);

            for (StagingStorage ss : storages) {
                dao.delete(ss);
            }

            List<DataFile> dataFiles = dao.findAll(QDataFile.dataFile);
            for (DataFile df : dataFiles) {
                dao.delete(df);
            }

            dao.delete(testBag);
        }

        dao.delete(regionBag);
        dao.delete(regionToken);
        dao.delete(depositor);
    }

    @Test
    public void createReplicationTest() throws IOException {
        // Load the form page
        HtmlPage formPage = webClient.getPage(getUrl("/replications/add"));

        //// validate breadcrumbs
        String xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor repsLink = (HtmlAnchor) formPage.getFirstByXPath(xpath);
        assertThat(repsLink.getTextContent()).isEqualTo("Replications");
        assertThat(repsLink.getAttribute("href").toString()).isEqualTo("/replications");

        xpath = "(//ol[@class='breadcrumb']/li)[2]";
        HtmlElement el = (HtmlElement) formPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).contains("Create Replication");

        //// Fill in the form

        HtmlForm form = formPage.getFormByName("replication");

        // Collection Name
        xpath = "//div[@class='form-group row'][1]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Collection");
        HtmlSelect elSelect = formPage.getHtmlElementById("bagId");
        elSelect.getOptionByValue("" + testBag.getId()).setSelected(true);

        // Replication node
        xpath = "//div[@class='form-group row'][2]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("To");
        elSelect = formPage.getHtmlElementById("nodeId");
        elSelect.getOptionByValue("" + testNode.getId()).setSelected(true);

        HtmlSubmitInput submit = form.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage replPage = submit.click();

        String replUrl = replPage.getUrl().toString();
        long replId = Long.parseLong(replUrl.substring(replUrl.lastIndexOf("/") + 1));

        testRepl = dao.findOne(QReplication.replication, QReplication.replication.id.eq(replId));
        assertNotNull(testRepl);

        assertThat(replUrl).contains("/replications/");
        assertThat(replId).isPositive();

        //// validate breadcrumbs
        xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor replicationsLink = (HtmlAnchor) replPage.getFirstByXPath(xpath);
        assertThat(replicationsLink.getAttribute("href").toString()).isEqualTo("/replications");

        xpath = "(//ol[@class='breadcrumb']/li)[2]/a";
        HtmlAnchor bagLink = (HtmlAnchor) replPage.getFirstByXPath(xpath);
        assertThat(bagLink.getAttribute("href").toString())
                .isEqualTo("/replications?bag=" + TEST_BAG);

        xpath = "(//ol[@class='breadcrumb']/li)[3]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).containsIgnoringCase("" + replId);

        //// Validate properties

        // Node
        xpath = "(//table//tr)[1]/td[1]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Node");
        xpath = "(//table//tr)[1]/td[@class='bg-light']";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo(TEST_NODE);

        // Collection
        xpath = "(//table//tr)[2]/td[1]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Collection");
        xpath = "(//table//tr)[2]/td[@class='table-clickable']/a";
        HtmlAnchor elAnchor = (HtmlAnchor)replPage.getFirstByXPath(xpath);
        assertThat(elAnchor.getAttribute("href").toString())
                .isEqualTo("/bags/" + testBag.getId());

        // Collection DL Link
        xpath = "(//table//tr)[3]/td[1]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Collection DL Link");
        xpath = "(//table//tr)[3]/td[@class='bg-light']";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);

        StagingStorage storageBag = regionBag.getStorage().iterator().next();
        String colDlLink = ReplicationDao.createReplicationString(storageBag, true);
        assertThat(el.getTextContent()).contains(colDlLink);

        // Tagmanifest Fixity
        xpath = "(//table//tr)[4]/td[1]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Tagmanifest Fixity");
        xpath = "(//table//tr)[4]/td[@class='bg-light']";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEmpty();

        // Token DL Link
        xpath = "(//table//tr)[5]/td[1]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Token DL Link");
        xpath = "(//table//tr)[5]/td[@class='bg-light']";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);

        StagingStorage storageToken = regionBag.getStorage().iterator().next();
        String tokenDlLink = ReplicationDao.createReplicationString(storageToken, false);
        assertThat(el.getTextContent()).contains(tokenDlLink);

        // Token Fixity
        xpath = "(//table//tr)[6]/td[1]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Token Fixity");
        xpath = "(//table//tr)[6]/td[@class='bg-light']";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEmpty();

        // Status
        xpath = "(//table//tr)[7]/td[1]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Status");
        xpath = "(//table//tr)[7]/td[@class='bg-light']";
        HtmlElement statusEl = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(statusEl.getTextContent()).isEqualTo(REPLICATION_STATUS_PENDING);

        // Created At
        xpath = "(//table//tr)[8]/td[1]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Created At");
        xpath = "(//table//tr)[8]/td[@class='bg-light']";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent())
                .isEqualTo(testRepl.getCreatedAt().toString());

        // Updated At
        xpath = "(//table//tr)[9]/td[1]";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Updated At");
        xpath = "(//table//tr)[9]/td[@class='bg-light']";
        el = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent())
                .isEqualTo(testRepl.getUpdatedAt().toString());
    }

    @Test
    public void createReplicationAndListReplicationsPageTest() throws IOException {
        // Load the form page
        String formUrl = getUrl("/replications/create?bag=" + testBag.getId());
        HtmlPage formPage = webClient.getPage(formUrl);

        //// validate breadcrumbs
        String xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor repsLink = (HtmlAnchor) formPage.getFirstByXPath(xpath);
        assertThat(repsLink.getTextContent()).isEqualTo("Replications");
        assertThat(repsLink.getAttribute("href").toString()).isEqualTo("/replications");

        xpath = "(//ol[@class='breadcrumb']/li)[2]";
        HtmlElement el = (HtmlElement) formPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).contains("Create Replication");

        //// Fill in the form

        HtmlForm form = formPage.getFormByName("replication");

        // Replicating Nodes
        xpath = "//div[@class='form-group row'][1]";
        el = (HtmlElement)formPage.getFirstByXPath(xpath + "/label");
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Replicating Nodes");
        HtmlSelect elSelect = formPage.getHtmlElementById("nodes");
        elSelect.getOptionByValue("" + testNode.getId()).setSelected(true);

        HtmlSubmitInput submit = form.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage replPage = submit.click();

        // Redirect to the replication list page
        assertThat(replPage.getUrl().toString()).endsWith("/replications/");

        //// verify the replication in list replications page

        xpath = "//div[@class='card w-75 filterable']/table/tbody/tr[1]/td";
        List<HtmlElement> replTds = replPage.getByXPath(xpath);

        // ID with table-clickable link
        el = replTds.get(0);
        long replId = Long.parseLong(el.getTextContent());
        assertThat(replId).isPositive();
        assertThat(el.getAttribute("data-href").toString()).isEqualTo("/replications/" + replId);

        testRepl = dao.findOne(QReplication.replication, QReplication.replication.id.eq(replId));
        assertNotNull(testRepl);

        // Collection with table-clickable link
        el = replTds.get(1);
        assertThat(el.getTextContent()).isEqualTo(testBag.getName());
        assertThat(el.getAttribute("data-href").toString()).isEqualTo("/bags/" + testBag.getId());
   
        // Replication Node
        assertThat(replTds.get(2).getTextContent()).isEqualTo(TEST_NODE);
        // Status
        assertThat(replTds.get(3).getTextContent()).isEqualTo(REPLICATION_STATUS_PENDING);
        // Created At
        assertThat(replTds.get(4).getTextContent())
                .isEqualTo(testRepl.getCreatedAt().toLocalDate().toString());
        // Updated At
        assertThat(replTds.get(5).getTextContent())
                .isEqualTo(testRepl.getUpdatedAt().toLocalDate().toString());
    }

    @Test
    public void updateReplicationStatusTest() throws IOException {
        // create test replication
        testRepl = createTestReplication(ReplicationStatus.PENDING, testNode, testBag);

        // load the edit form
        HtmlPage formPage = webClient.getPage(getUrl("/replications/" + testRepl.getId() + "/edit"));

        //// validate breadcrumbs
        String xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor link = (HtmlAnchor) formPage.getFirstByXPath(xpath);
        assertThat(link.getTextContent()).isEqualTo("Replications");
        assertThat(link.getAttribute("href").toString()).isEqualTo("/replications");

        xpath = "(//ol[@class='breadcrumb']/li)[2]/a";
        link = (HtmlAnchor) formPage.getFirstByXPath(xpath);
        assertThat(link.getTextContent()).isEqualTo(TEST_BAG);
        assertThat(link.getAttribute("href").toString())
                .isEqualTo("/replications?bag=" + TEST_BAG);

        xpath = "(//ol[@class='breadcrumb']/li)[3]/a";
        link = (HtmlAnchor) formPage.getFirstByXPath(xpath);
        assertThat(link.getTextContent()).isEqualTo("" + testRepl.getId());
        assertThat(link.getAttribute("href").toString())
                .isEqualTo("/replications/" + testRepl.getId());

        xpath = "(//ol[@class='breadcrumb']/li)[4]";
        HtmlElement el = (HtmlElement) formPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).contains("Edit");

        //// Validate the form

        // form title
        xpath = "//div[@class='card-body']/h6";
        el = formPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Update Chronopolis Reolication");

        HtmlForm form = formPage.getFormByName("replication");

        // Collection
        el = formPage.getFirstByXPath("//label[@for='collection']");
        assertThat(el.getTextContent()).isEqualTo("Collection");
        HtmlInput elInput = form.getInputByName("collection");
        assertThat(elInput.getValueAttribute()).isEqualTo(TEST_BAG);

        // Replicating Nodes
        el = formPage.getFirstByXPath("//label[@for='node']");
        assertThat(el.getTextContent()).isEqualTo("Replicating Node");
        HtmlSelect elSelect = form.getSelectByName("node");
        HtmlOption option = elSelect.getOptionByText(TEST_NODE);
        assertThat(option.isSelected()).isEqualTo(true);

        // Replicating Status
        el = formPage.getFirstByXPath("//label[@for='status']");
        assertThat(el.getTextContent()).isEqualTo("Replicating Status");
        elSelect = form.getSelectByName("status");
        option = elSelect.getOptionByValue(REPLICATION_STATUS_PENDING);
        assertThat(option.isSelected()).isEqualTo(true);

        // change replicating status from PENDING to SUCCESS
        elSelect.getOptionByValue(REPLICATION_STATUS_SUCCESS).setSelected(true);

        HtmlSubmitInput submit = form.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage replPage = submit.click();

        assertThat(replPage.getUrl().toString()).endsWith("/replications/" + testRepl.getId());

        // verify the new replication status: SUCCESS
        xpath = "(//table//tr)[7]/td[@class='bg-light']";
        HtmlElement statusEl = (HtmlElement) replPage.getFirstByXPath(xpath);
        assertThat(statusEl.getTextContent()).isEqualTo(REPLICATION_STATUS_SUCCESS);
    }

    @Test
    public void deleteReplicationTest() throws IOException {
        // create test replication
        testRepl = createTestReplication(ReplicationStatus.PENDING, testNode, testBag);

        // load the edit form
        HtmlPage formPage = webClient.getPage(getUrl("/replications/" + testRepl.getId() + "/edit"));

        //// Validate the form

        // from title
        String xpath = "//div[@class='card-body']/h6";
        HtmlElement el = formPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Update Chronopolis Reolication");

        HtmlForm form = formPage.getFormByName("replication");

        // Collection
        el = formPage.getFirstByXPath("//label[@for='collection']");
        assertThat(el.getTextContent()).isEqualTo("Collection");
        HtmlInput elInput = form.getInputByName("collection");
        assertThat(elInput.getValueAttribute()).isEqualTo(TEST_BAG);

        // Replicating Nodes
        el = formPage.getFirstByXPath("//label[@for='node']");
        assertThat(el.getTextContent()).isEqualTo("Replicating Node");
        HtmlSelect elSelect = form.getSelectByName("node");
        HtmlOption option = elSelect.getOptionByText(testNode.getUsername());
        assertThat(option.isSelected()).isEqualTo(true);

        // Replicating Status
        el = formPage.getFirstByXPath("//label[@for='status']");
        assertThat(el.getTextContent()).isEqualTo("Replicating Status");
        elSelect = form.getSelectByName("status");
        option = elSelect.getOptionByValue(REPLICATION_STATUS_PENDING);
        assertThat(option.isSelected()).isEqualTo(true);

        // find the delete button
        HtmlAnchor deleteButton = formPage.getAnchorByText("Delete Replication");
        HtmlPage confirmPage = deleteButton.click();
        // confirm delete
        HtmlPage page = confirmPage.getAnchorByText("Delete").click();

        // redirect to list collections page
        assertThat(page.getUrl().toString()).endsWith("/replications");

        // verify that the replication is gone: no table rows
        xpath = "//div[@class='card w-75 filterable']/table/tbody/tr";
        List<HtmlElement> replTrs = page.getByXPath(xpath);

        assertThat(replTrs.size()).isEqualTo(0);

        // reset to avoid cleanup for deleted replication 
        testRepl = null;
    }

    /*
     * Create collection with files.
     * @param depositor
     * @param regionBag
     * @param regionToken
     * @param nodes
     * @param dao
     * @return
     */
    private Bag createBagWithFiles(Depositor depositor, StorageRegion regionBag,
            StorageRegion regionToken, List<Node> nodes) {
        Bag bag = new Bag(TEST_BAG, TEST_CREATOR, depositor, 1L, 1L, BagStatus.REPLICATING);
        for (Node n : nodes) {
            bag.addDistribution(n, BagDistributionStatus.DISTRIBUTE);
        }
        dao.save(bag);
 
        // create bag file and staging storage for bag
        DataFile bFile = bagFile(bag);
        stagingStorage(bag, regionBag, bFile);
        // create token file and staging storage for token
        DataFile tStore = tokenStore(bag);
        stagingStorage(bag, regionToken, tStore);

        return bag;
    }

    /*
     * Create replication
     * @param status
     * @param node
     * @param bag
     * @return
     */
    private Replication createTestReplication(ReplicationStatus status, Node node, Bag bag) {
        StagingStorage storageBag = regionBag.getStorage().iterator().next();
        StagingStorage storageToken = regionToken.getStorage().iterator().next();
        String bagDlLink = ReplicationDao.createReplicationString(storageBag, true);
        String tokenDlLink = ReplicationDao.createReplicationString(storageToken, false);

        Replication repl = new Replication(status, node, bag,
            bagDlLink, tokenDlLink, "rsync", null, null);

        dao.save(repl);

    	return repl;
    }
}
