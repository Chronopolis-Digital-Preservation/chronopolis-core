package org.chronopolis.ingest.features;

import static org.fest.assertions.Assertions.assertThat;

import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.depositor.Depositor;
import org.chronopolis.rest.entities.depositor.QDepositor;
import org.junit.After;
import org.junit.Test;

/**
 * Feature tests for DepositorUIController.
 * @author lsitu
 */
public class DepositorUIControllerTest extends TestBase {

    @After
    public void done() {
        // cleanup the depositor created by test
        QDepositor depositor = QDepositor.depositor;
        Depositor depositors = dao.findOne(depositor,
                depositor.namespace.eq(TEST_DEPOSITOR));
        dao.delete(depositors);
    }

    @Test
    public void createDepositorTest() throws IOException {
        // Load the form page
        HtmlPage formPage = webClient.getPage(getUrl("/depositors/add"));

        //// validate the breadcrumbs

        String xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor depositorsLink = (HtmlAnchor) formPage.getByXPath(xpath).get(0);
        assertThat(depositorsLink.getAttribute("href").toString()).isEqualTo("/depositors");

        xpath = "(//ol[@class='breadcrumb']/li)[2]";
        HtmlElement el = (HtmlElement) formPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Add Depositor");

        HtmlForm addForm = formPage.getFormByName("depositor");

        //// Fill out the create depositor form

        // Organization Name
        xpath = "//div[@class='form-group row'][1]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Organization Name");
        HtmlTextInput elInput = formPage.getHtmlElementById("sourceOrganization");
        elInput.setValueAttribute("Source Organization");

        // Organization address
        xpath = "//div[@class='form-group row'][2]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Organization Address");
        elInput = formPage.getHtmlElementById("org-address");
        elInput.setValueAttribute("Organization Address");

        // Namespace
        xpath = "//div[@class='form-group row'][3]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Namespace");
        elInput = formPage.getHtmlElementById("namespace");
        elInput.setValueAttribute(TEST_DEPOSITOR);

        // Replicating Nodes
        xpath = "//div[@class='form-group row'][4]";
        el = (HtmlElement)formPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Replicating Nodes");
        HtmlSelect elSelect = (HtmlSelect)formPage.getElementByName("replicatingNodes");
        elSelect.getOptionByValue(TEST_NODE).setSelected(true);

        HtmlSubmitInput submit = addForm.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage depositorPage = submit.click();

        //////// depositor page
        assertThat(depositorPage.getUrl().toString()).endsWith("/depositors/list/" + TEST_DEPOSITOR);

        //// validate breadcrumbs

        xpath = "(//ol[@class='breadcrumb']/li)[1]/a";
        HtmlAnchor replicationsLink = (HtmlAnchor)  depositorPage.getByXPath(xpath).get(0);
        assertThat(replicationsLink.getAttribute("href").toString()).isEqualTo("/depositors");

        xpath = "(//ol[@class='breadcrumb']/li)[2]/a";
        HtmlAnchor bagLink = (HtmlAnchor)  depositorPage.getByXPath(xpath).get(0);
        assertThat(bagLink.getAttribute("href").toString()).isEqualTo("/depositors/list");

        xpath = "(//ol[@class='breadcrumb']/li)[3]";
        el = (HtmlElement)  depositorPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo(TEST_DEPOSITOR);

        //// Validate the properties

        // ID
        xpath = "(//table//tr)[1]/td[1]";
        el = (HtmlElement)depositorPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Id");
        xpath = "(//table//tr)[1]/td[@class='bg-light']";
        el = (HtmlElement)  depositorPage.getByXPath(xpath).get(0);
        assertThat(Long.parseLong(el.getTextContent())).isPositive();

        // Source Organization
        xpath = "(//table//tr)[2]/td[1]";
        el = (HtmlElement)depositorPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Source Organization");
        xpath = "(//table//tr)[2]/td[@class='bg-light']";
        el = (HtmlElement)depositorPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Source Organization");

        // Organization Address
        xpath = "(//table//tr)[3]/td[1]";
        el = (HtmlElement)depositorPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Organization Address");
        xpath = "(//table//tr)[3]/td[@class='bg-light']";
        el = (HtmlElement)depositorPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Organization Address");

        // Replicates To
        xpath = "(//table//tr)[6]/td[1]";
        el = (HtmlElement)depositorPage.getByXPath(xpath).get(0);
        assertThat(el.getTextContent()).isEqualTo("Replicates To");
        xpath = "(//table//tr)[6]/td[@class='bg-light']/a";
        HtmlAnchor replicateLink = (HtmlAnchor)  depositorPage.getByXPath(xpath).get(0);
        assertThat(replicateLink.getTextContent().toString()).isEqualTo("test-node");
    }

    @Test
    public void addDepositorContactTest() throws IOException {
        // initiate the depositor for test
        List<Node> nodes = Arrays.asList(testNode);
        createDepositor(TEST_DEPOSITOR, "source organization", "Address", nodes);

        // Load the form page
        final String depositorUrl = "/depositors/list/" + TEST_DEPOSITOR;
        HtmlPage depositorPage = webClient.getPage(this.getUrl(depositorUrl));
        HtmlPage formPage = depositorPage.getAnchorByText("Add Contact").click();
        assertThat(formPage.getBaseURL().toString()).endsWith(TEST_DEPOSITOR + "/addContact");

        assertThat(formPage.getElementsByTagName("h4").get(0).getTextContent())
                .isEqualTo("Add Depositor Contact");

        HtmlForm form = formPage.getFormByName("contact");
        form.getInputByName("contactName").setValueAttribute("contact 1");
        form.getInputByName("contactEmail").setValueAttribute("contact1@example.com");
        form.getInputByName("contactPhone.number").setValueAttribute("858-534-2230");

        HtmlSubmitInput submit = form.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage newPage = submit.click();
        assertThat(newPage.getBaseURL().toString()).endsWith(TEST_DEPOSITOR);

        String xpath = "//div[@class='card-body p-2'][2]/table//tr/td";
        List<HtmlElement> contactEls = newPage.getByXPath(xpath);

        assertThat(contactEls.get(0).getTextContent()).isEqualTo("contact 1");
        assertThat(contactEls.get(1).getTextContent()).isEqualTo("contact1@example.com");
        assertThat(contactEls.get(2).getTextContent()).isEqualTo("+1 858-534-2230");

        HtmlElement removeEl = contactEls.get(3);
        assertThat(removeEl.getTextContent()).contains("Remove Contact");
        assertThat(removeEl.getAttribute("data-href"))
                .endsWith(TEST_DEPOSITOR + "/removeContact?email=contact1@example.com");
    }
}
