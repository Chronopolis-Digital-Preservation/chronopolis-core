package org.chronopolis.ingest.features;

import static org.fest.assertions.Assertions.assertThat;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.chronopolis.rest.entities.storage.QStorageRegion;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.enums.DataType;
import org.chronopolis.rest.models.enums.StorageUnit;
import org.junit.After;
import org.junit.Test;

/**
 * Feature tests for StorageRegionUIController
 * @author lsitu
 */
public class StorageRegionUIControllerTest extends TestBase {

    private StorageRegion testRegion = null;

    @After
    public void done() {
        if (testRegion != null) {
            dao.delete(testRegion);
        }

        List<StorageRegion> regions = dao.findAll(QStorageRegion.storageRegion);
        for (StorageRegion region : regions) {
            if (region.getNode().getUsername().equalsIgnoreCase(TEST_NODE))
                dao.delete(region);
        }
    }

    @Test
    public void getRegionsTest() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        // initiate a StorageRegion for test
        StorageRegion region = createStorageRegion(DataType.BAG, testNode);
        HtmlPage regionsPage = webClient.getPage(getUrl("/regions"));

        //// validate breadcrumbs
        String xpath = "(//ol[@class='breadcrumb']/li)[1]";
        HtmlElement el = (HtmlElement) regionsPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Storage Regions");

        // search button
        xpath = "(//ol[@class='breadcrumb']/li)[2]/a";
        HtmlAnchor regionsLink = (HtmlAnchor)regionsPage.getFirstByXPath(xpath);
        assertThat(regionsLink.getAttribute("data-target").toString()).isEqualTo("#filter-body");
        assertThat(regionsLink.getTextContent()).contains("Search");

        xpath = "//div[@class='card w-75 filterable']/table/tbody/tr";
        el = (HtmlElement) regionsPage.getFirstByXPath(xpath);
        assertThat(el.getAttribute("data-href").toString()).isEqualTo("/regions/" + region.getId());
        List<HtmlElement> tds = el.getElementsByTagName("td");
        // id
        assertThat(tds.get(1).getTextContent()).isEqualTo("" + region.getId());
        // Owner
        assertThat(tds.get(2).getTextContent()).isEqualTo(region.getNode().getUsername());
        // Storage type
        assertThat(tds.get(3).getTextContent()).isEqualTo(region.getStorageType().toString());
        // Data type
        assertThat(tds.get(4).getTextContent()).isEqualTo(region.getDataType().toString());
        // Capacity
        assertThat(tds.get(5).getTextContent()).contains(formatCapacity(region.getCapacity()));
    }

    @Test
    public void createRegionTest() throws IOException {

        // Load the form page
        HtmlPage formPage = webClient.getPage(getUrl("/regions/create"));

        //// validate breadcrumbs
        String xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor regionsLink = (HtmlAnchor)formPage.getByXPath(xpath).get(0);
        assertThat(regionsLink.getAttribute("href").toString()).isEqualTo("/regions");

        xpath = "(//ol[@class='breadcrumb']/li)[2]";
        HtmlElement el = (HtmlElement) formPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Create Storage Region");

        //// Fill out the create region form

        HtmlForm regionForm = (HtmlForm)formPage.getByXPath("//form[@action='/regions']").get(0);

        // Owning Node
        xpath = "//div[@class='form-group row'][1]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Owning Node");
        HtmlSelect elSelect = formPage.getHtmlElementById("node");
        elSelect.getOptionByValue(TEST_NODE).setSelected(true);

        // Data Type
        xpath = "//div[@class='form-group row'][2]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Data Type");
        elSelect = formPage.getHtmlElementById("dataType");
        assertThat(elSelect.getOptions().size()).isEqualTo(2);
        assertThat(elSelect.getOptions().get(0).getText()).isEqualTo("BAG");
        assertThat(elSelect.getOptions().get(1).getText()).isEqualTo("TOKEN");
        elSelect.setSelectedIndex(0);

        // Storage Type
        xpath = "//div[@class='form-group row'][3]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Storage Type");
        elSelect = formPage.getHtmlElementById("storageType");
        assertThat(elSelect.getOptions().size()).isEqualTo(1);
        assertThat(elSelect.getOptions().get(0).getText()).isEqualTo("LOCAL");
        elSelect.setSelectedIndex(0);

        // Total Capacity
        xpath = "//div[@class='form-group row'][4]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Total Capacity");
        HtmlInput elInput = (HtmlInput)formPage.getHtmlElementById("capacity");
        elInput.setValueAttribute("10000");

        // Storage Unit
        xpath = "//div[@class='form-group row'][5]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Storage Unit");
        elSelect = formPage.getHtmlElementById("storageUnit");
        assertThat(elSelect.getOptions().size()).isEqualTo(7);
        assertThat(elSelect.getOptions().get(0).getText()).isEqualTo("B");
        elSelect.setSelectedIndex(0);

        // Storage Region Information
        xpath = "//div[@class='form-group row'][6]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Information");
        HtmlTextArea elTextArea = (HtmlTextArea)formPage.getHtmlElementById("note");
        elTextArea.setText("Test Region: BAG");

        // Replication Server
        xpath = "//div[@class='form-group row'][7]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Replication Server");
        elInput = formPage.getHtmlElementById("replication-server");
        elInput.setValueAttribute("localhost");

        // Replication Path
        xpath = "//div[@class='form-group row'][8]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Path");
        elInput = formPage.getHtmlElementById("replication-path");
        elInput.setValueAttribute("/replication/path");

        // Replication Username
        xpath = "//div[@class='form-group row'][9]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Username");
        elInput = formPage.getHtmlElementById("replication-username");
        elInput.setValueAttribute("username");

        HtmlSubmitInput submit = regionForm.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage regionPage = submit.click();

        //////// StorageRegion page
        String regionUrl = regionPage.getUrl().toString();
        long regionId = Long.parseLong(regionUrl.substring(regionUrl.lastIndexOf("/") + 1));
        assertThat(regionUrl).contains("/regions/");
        assertThat(regionId).isPositive();

        // validate breadcrumbs
        xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        regionsLink = (HtmlAnchor)regionPage.getByXPath(xpath).get(0);
        assertThat(regionsLink.getTextContent()).isEqualTo("Storage Regions");
        assertThat(regionsLink.getAttribute("href").toString()).isEqualTo("/regions");

        xpath = "(//ol[@class='breadcrumb']/li)[2]/a";
        regionsLink = (HtmlAnchor)regionPage.getByXPath(xpath).get(0);
        assertThat(regionsLink.getTextContent()).isEqualTo("test-node");
        assertThat(regionsLink.getAttribute("href").toString()).isEqualTo("/regions?node=test-node");

        xpath = "(//ol[@class='breadcrumb']/li)[3]";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo(regionId + "");

        //// Validate properties

        // Owner
        xpath = "(//table//tr)[1]/td[1]";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Owner");
        xpath = "(//table//tr)[1]/td[@class='bg-light']";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("test-node");

        // Storage Type
        xpath = "(//table//tr)[2]/td[1]";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Storage Type");
        xpath = "(//table//tr)[2]/td[@class='bg-light']";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("LOCAL");

        // Data Type
        xpath = "(//table//tr)[3]/td[1]";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Data Type");
        xpath = "(//table//tr)[3]/td[@class='bg-light']";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("BAG");

        // Capacity
        xpath = "(//table//tr)[4]/td[1]";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent().trim()).isEqualTo("Capacity");
        xpath = "(//table//tr)[4]/td[2]/div[@class='progress']/span";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        Double capacity = 10000 * Math.pow(1000, StorageUnit.B.getPower());
        assertThat(el.getTextContent()).isEqualTo("0 B / " + formatCapacity(capacity.longValue()));

        // Information
        xpath = "(//table//tr)[7]/td[1]";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Information");
        xpath = "(//table//tr)[7]/td[@class='bg-light']";
        el = (HtmlElement)regionPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Test Region: BAG");
    }

    @Test
    public void editRegionFormTest() throws IOException {
        // initiate test StorageRegion
        testRegion = createStorageRegion(DataType.TOKEN, testNode);
        Long regionId = testRegion.getId();
        HtmlPage regionPage = webClient.getPage(getUrl("/regions/" + regionId));

        String editUrl = "/regions/" + regionId + "/edit";
        HtmlAnchor editLink = (HtmlAnchor)regionPage.getAnchorByHref(editUrl);
        HtmlPage editPage = editLink.click();

        String xpath = "//div[@class='card-body p-2']/h4";
        HtmlElement el = (HtmlElement)editPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Edit StorageRegion");

        // find the edit form
        HtmlForm editForm = (HtmlForm)editPage.getFirstByXPath("//form[@action='edit']");

        // verify the fields and values
        HtmlSelect elSelect = editForm.getSelectByName("dataType");
        assertThat(elSelect.getSelectedOptions().get(0).getValueAttribute())
                .isEqualTo("TOKEN");

        elSelect = editForm.getSelectByName("storageType");
        assertThat(elSelect.getSelectedOptions().get(0).getValueAttribute())
                .isEqualTo("LOCAL");

        HtmlInput elInput = editForm.getInputByName("capacity");
        long capacity = Math.round(Double.valueOf(formatCapacity(testRegion.getCapacity()).split(" ")[0]));
        assertThat(elInput.getValueAttribute().toString()).isEqualTo("" + capacity);

        elSelect = editForm.getSelectByName("storageUnit");
        assertThat(elSelect.getSelectedOptions().get(0).getValueAttribute())
                .isEqualTo("KiB");

        HtmlTextArea elTextArea = editForm.getTextAreaByName("note");
        assertThat(elTextArea.getText()).isEqualTo("Storage Region Note");
    }

    @Test
    public void editRegionTest() throws IOException {
        // initiate test StorageRegion
        testRegion = createStorageRegion(DataType.TOKEN, testNode);
        Long regionId = testRegion.getId();
        HtmlPage regionPage = webClient.getPage(getUrl("/regions/" + regionId));

        String editUrl = "/regions/" + regionId + "/edit";
        HtmlAnchor editLink = (HtmlAnchor)regionPage.getAnchorByHref(editUrl);
        HtmlPage editPage = editLink.click();

        String xpath = "//div[@class='card-body p-2']/h4";
        HtmlElement el = (HtmlElement)editPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Edit StorageRegion");

        // find the edit form
        HtmlForm editForm = (HtmlForm)editPage.getFirstByXPath("//form[@action='edit']");

        // Set edit fields and values
        HtmlSelect elSelect = editForm.getSelectByName("dataType");
        elSelect.getOptionByValue("BAG").setSelected(true);

        HtmlInput elInput = editForm.getInputByName("capacity");
        elInput.setValueAttribute("10");

        elSelect = editForm.getSelectByName("storageUnit");
        elSelect.getOptionByValue("MiB").setSelected(true);

        HtmlTextArea elTextArea = editForm.getTextAreaByName("note");
        elTextArea.setText("Storage Region Note Updated");

        HtmlSubmitInput submit = editForm.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage updatedRegionPage = submit.click();

        //// Validate updated fields

        // Data Type
        xpath = "(//table//tr)[3]/td[@class='bg-light']";
        el = (HtmlElement)updatedRegionPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("BAG");

        // Capacity
        xpath = "(//table//tr)[4]/td[2]/div[@class='progress']/span";
        el = (HtmlElement)updatedRegionPage.getFirstByXPath(xpath);
        Double capacity = 10 * Math.pow(1000, StorageUnit.MiB.getPower());
        assertThat(el.getTextContent()).contains("0 B / " + formatCapacity(capacity.longValue()));

        // Information
        xpath = "(//table//tr)[7]/td[@class='bg-light']";
        el = (HtmlElement)updatedRegionPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Storage Region Note Updated");

        // load the edit form to verify the consistency of the capacity field
        editLink = (HtmlAnchor)updatedRegionPage.getAnchorByHref(editUrl);
        editPage = editLink.click();

        // find the edit form
        editForm = (HtmlForm)editPage.getFirstByXPath("//form[@action='edit']");

        elInput = editForm.getInputByName("capacity");
        assertThat(elInput.getValueAttribute()).isEqualTo("10");
    }

    @Test
    public void updateReplicationConfigurationTest() throws IOException {
        // initiate StorageRegion for test
        StorageRegion region = createStorageRegion(DataType.BAG, testNode);
        Long regionId = region.getId();
        HtmlPage regionPage = webClient.getPage(getUrl("/regions/" + regionId));

        String xpath = "//div[@class='card-header']/h4";
        HtmlElement el = (HtmlElement)regionPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Replication Configuration");

        // update fields in the replication configuration form
        String actionUrl = "/regions/" + regionId + "/config";
        HtmlForm configForm = (HtmlForm)regionPage.getFirstByXPath("//form[@action='" + actionUrl + "']");

        HtmlInput input = configForm.getInputByName("server");
        assertThat(input.getValueAttribute()).isEqualTo("localhost");
        input.setValueAttribute("test-server");

        input = configForm.getInputByName("path");
        assertThat(input.getValueAttribute()).isEqualTo("/replication/path");
        input.setValueAttribute("/replication/path/test");

        input = configForm.getInputByName("username");
        assertThat(input.getValueAttribute()).isEqualTo("user");
        input.setValueAttribute("tester");

        HtmlSubmitInput submit = configForm.getOneHtmlElementByAttribute("input", "type", "submit");
        submit.click();

        // verify the updated replication configuration fields
        regionPage = webClient.getPage(getUrl("/regions/" + regionId));
        xpath = "//div[@class='card-header']/h4";
        el = (HtmlElement)regionPage.getFirstByXPath(xpath);
        assertThat(el.getTextContent()).isEqualTo("Replication Configuration");

        assertThat(configForm.getInputByName("server").getValueAttribute())
                .isEqualTo("test-server");

        assertThat(configForm.getInputByName("path").getValueAttribute())
                .isEqualTo("/replication/path/test");

        assertThat(configForm.getInputByName("username").getValueAttribute())
                .isEqualTo("tester");
    }
}
