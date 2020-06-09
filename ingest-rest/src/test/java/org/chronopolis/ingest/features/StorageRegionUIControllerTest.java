package org.chronopolis.ingest.features;

import static org.fest.assertions.Assertions.assertThat;

import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;

import org.chronopolis.rest.entities.storage.QStorageRegion;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.enums.DataType;
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
        assertThat(elInput.getValueAttribute()).isEqualTo("977");

        elSelect = editForm.getSelectByName("storageUnit");
        assertThat(elSelect.getSelectedOptions().get(0).getValueAttribute())
                .isEqualTo("KiB");

        HtmlTextArea elTextArea = editForm.getTextAreaByName("note");
        assertThat(elTextArea.getText()).isEqualTo("Storage Region Note");
    }
}
