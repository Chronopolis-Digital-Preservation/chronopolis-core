package org.chronopolis.ingest.features;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Feature tests for SiteController.
 * @author lsitu
 */
public class SiteControllerTest extends TestBase {

    @Test
    public void createUserTest() throws IOException {
        // load the user page
        HtmlPage usersPage = webClient.getPage(getUrl("/users"));

        // add user form
        String xpath = "//div[@class='card-header']/ul/li[3]/a[@href='#add']";
        HtmlAnchor addUserLink = (HtmlAnchor)usersPage.getByXPath(xpath).get(0);
        assertThat(addUserLink.getTextContent()).contains("Add user");
        addUserLink.click();

        //// Fill out the create user form
        HtmlForm addForm = usersPage.getForms().get(1);

        // Username
        xpath = "//div[@id='add']//div[@class='form-group row'][1]";
        HtmlElement el = (HtmlElement)usersPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Username");
        HtmlTextInput elInput = usersPage.getHtmlElementById("create-username");
        elInput.setValueAttribute("test-user");

        // Password
        xpath = "//div[@id='add']//div[@class='form-group row'][2]";
        el = (HtmlElement)usersPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Password");
        elInput = usersPage.getHtmlElementById("create-password");
        elInput.setValueAttribute("password");

        // Role
        xpath = "//div[@id='add']//div[@class='form-group row'][3]";
        el = (HtmlElement)usersPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Role");
        HtmlSelect elSelect = (HtmlSelect)usersPage.getHtmlElementById("create-role");
        assertThat(elSelect.getOptions().size()).isEqualTo(3);
        assertThat(elSelect.getOptions().get(0).getText()).isEqualTo("ROLE_SERVICE");
        assertThat(elSelect.getOptions().get(1).getText()).isEqualTo("ROLE_USER");
        assertThat(elSelect.getOptions().get(2).getText()).isEqualTo("ROLE_ADMIN");
        elSelect.setSelectedIndex(1);

        // Is a node
        xpath = "//div[@id='add']//div[@class='form-check'][1]";
        el = (HtmlElement)usersPage.getByXPath(xpath + "/label").get(0);
        assertThat(((HtmlElement)el).getTextContent()).contains("Is a node");
        HtmlCheckBoxInput elCheckBoxInput = usersPage.getElementByName("node");
        elCheckBoxInput.click();
        assertThat(elCheckBoxInput.isChecked()).isEqualTo(true);

        HtmlSubmitInput submit = addForm.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage newPage = submit.click();

        assertThat(newPage.getUrl().toString()).endsWith("/users");

        //// validate the properties

        // Table headers
        xpath = "//div[@id='info']/table/thead/tr/th";
        el = (HtmlElement)usersPage.getByXPath(xpath + "[1]").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Username");
        el = (HtmlElement)usersPage.getByXPath(xpath + "[2]").get(0);
        assertThat(((HtmlElement)el).getTextContent()).isEqualTo("Role");

        // Validate user record display
        xpath = "//div[@id='info']/table/tbody/tr";
        List<HtmlElement> els = newPage.getByXPath(xpath);
        el = (HtmlElement) els.get(els.size() - 1);
        assertThat(((HtmlElement)el.getByXPath("td[1]").get(0)).getTextContent()).isEqualTo("test-user");
        assertThat(((HtmlElement)el.getByXPath("td[2]").get(0)).getTextContent()).isEqualTo("ROLE_USER");
    }

    @Test
    public void testRootPage() throws IOException {
        // load the application page
        HtmlPage page = webClient.getPage(getUrl("/"));
        assertThat(page.getTitleText()).isEqualTo("Chronopolis Ingestion Service");

        //// main menu
        //Brand link
        HtmlAnchor brand = page.getFirstByXPath("//nav/a[@class='navbar-brand']");
        assertThat(brand.getTextContent()).isEqualTo("Chronopolis");
        assertThat(brand.getHrefAttribute()).isEqualTo("/");

        // Logout link
        HtmlAnchor logout = page.getFirstByXPath("//nav/div/a[@class='navbar-text']");
        assertThat(logout.getTextContent()).contains("Log Out");
        assertThat(logout.getHrefAttribute()).isEqualTo("/logout");

        // Main menu xPath
        String xpath = "//nav/div[@id='navbarText']/ul/li";

        // Collections menu
        HtmlAnchor colsMenu = page.getFirstByXPath(xpath + "[1]/a");
        assertThat(colsMenu.getTextContent()).isEqualTo("Collections");
        assertThat(colsMenu.getHrefAttribute()).isEqualTo("#");

        List<HtmlAnchor> submenus = page.getByXPath(xpath + "[1]/div/a");

        HtmlAnchor submenu = submenus.get(0);
        assertThat(submenu.getTextContent()).isEqualTo("Overview");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/bags/overview");

        submenu = submenus.get(1);
        assertThat(submenu.getTextContent()).isEqualTo("List Collections");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/bags");

        submenu = submenus.get(2);
        assertThat(submenu.getTextContent()).isEqualTo("List Stuck Collections");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/bags/stuck");

        submenu = submenus.get(3);
        assertThat(submenu.getTextContent()).isEqualTo("Add Collection");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/bags/add");

        // Replications menu
        HtmlAnchor replsMenu  = page.getFirstByXPath(xpath + "[2]/a");
        assertThat(replsMenu.getTextContent()).isEqualTo("Replications");
        assertThat(replsMenu.getHrefAttribute()).isEqualTo("#");

        submenus = page.getByXPath(xpath + "[2]/div/a");

        submenu = submenus.get(0);
        assertThat(submenu.getTextContent()).isEqualTo("Add Replication");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/replications/add");

        submenu = submenus.get(1);
        assertThat(submenu.getTextContent()).isEqualTo("List Replications");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/replications");

        // Repairs menu
        HtmlAnchor repairsMenu = (HtmlAnchor)page.getFirstByXPath(xpath + "[3]/a");
        assertThat(repairsMenu.getTextContent()).isEqualTo("Repairs");
        assertThat(repairsMenu.getHrefAttribute()).isEqualTo("#");

        submenus = page.getByXPath(xpath + "[3]/div/a");

        submenu = submenus.get(0);
        assertThat(submenu.getTextContent()).isEqualTo("Request Repair");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/repairs/add");

        submenu = submenus.get(1);
        assertThat(submenu.getTextContent()).isEqualTo("Fulfill A Repair");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/repairs/fulfill");

        submenu = submenus.get(2);
        assertThat(submenu.getTextContent()).isEqualTo("List Repairs");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/repairs");

        // Admin menu
        HtmlAnchor adminMenu = page.getFirstByXPath(xpath + "[4]/a");
        assertThat(adminMenu.getTextContent()).isEqualTo("Admin");
        assertThat(adminMenu.getHrefAttribute()).isEqualTo("#");

        submenus = page.getByXPath(xpath + "[4]/div/a");

        submenu = submenus.get(0);
        assertThat(submenu.getTextContent()).isEqualTo("User Config");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/users");

        submenu = submenus.get(1);
        assertThat(submenu.getTextContent()).isEqualTo("Depositors");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/depositors");

        submenu = submenus.get(2);
        assertThat(submenu.getTextContent()).isEqualTo("All Depositors");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/depositors/list");

        submenu = submenus.get(3);
        assertThat(submenu.getTextContent()).isEqualTo("Storage Regions");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/regions");

        submenu = submenus.get(4);
        assertThat(submenu.getTextContent()).isEqualTo("Storage Region Create");
        assertThat(submenu.getHrefAttribute()).isEqualTo("/regions/create");

        //// Quick Stats
        xpath = "//div[@class='hr-sect w-75 mx-auto']";
        assertThat(((HtmlElement)page.getFirstByXPath(xpath)).getTextContent())
                .contains("Quick Stats");

        xpath = "//div[@class='w-75 mx-auto']/div/div";
        HtmlElement el = page.getFirstByXPath(xpath + "[1]/span[@class='subheading']");
        assertThat(el.getTextContent()).isEqualTo("preserved collections");

        el = page.getFirstByXPath(xpath + "[2]/span[@class='subheading']");
        assertThat(el.getTextContent()).isEqualTo("replicating collections");

        el = page.getFirstByXPath(xpath + "[3]/span[@class='subheading']");
        assertThat(el.getTextContent()).isEqualTo("active replications");

        HtmlAnchor stuckStats = page.getFirstByXPath(xpath + "[4]/a");
        assertThat(stuckStats.getTextContent()).isEqualTo("stuck replications");
        assertThat(stuckStats.getHrefAttribute()).isEqualTo("/bags/stuck");

        el = page.getFirstByXPath(xpath + "[4]/h3[1]/small");
        assertThat(el.getTextContent()).isEqualTo("≥ 1 week");
        el = page.getFirstByXPath(xpath + "[4]/h3[2]/small");
        assertThat(el.getTextContent()).isEqualTo("≥ 2 weeks");
    }
}
