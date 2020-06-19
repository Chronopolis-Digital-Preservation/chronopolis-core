package org.chronopolis.ingest.features;

import static org.fest.assertions.Assertions.assertThat;

import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;

import org.chronopolis.rest.entities.storage.StorageRegion;
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

    private StorageRegion  regionBag;
    private StorageRegion  regionToken;

    @Before
    public void initTest() {
        regionBag = createStorageRegion(DataType.BAG, testNode);

        regionToken = createStorageRegion(DataType.TOKEN, testNode);

    }

    @After
    public void done() {
        dao.delete(regionBag);
        dao.delete(regionToken);
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
}
