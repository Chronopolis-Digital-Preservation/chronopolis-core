package org.chronopolis.ingest.documentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.chronopolis.ingest.IntegrationTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for SpringFox Swagger documentation access from:
 * /v2/api-docs
 * /swagger-ui.html
 *
 * @author lsitu
 */
@AutoConfigureMockMvc
public class SpringFoxSwagger2Test extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BuildProperties buildProperties;

    @Test
    public void SwaggerApiDocsTest() throws Exception {
        String version = buildProperties.getVersion();

        mockMvc.perform(get("/v2/api-docs")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swagger").value("2.0"))
                .andExpect(jsonPath("$.info.version").value(version));
    }

    @Test
    public void SwaggerUiDocsTest() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")
                .contentType("text/html"))
                .andExpect(status().isOk());
    }
}