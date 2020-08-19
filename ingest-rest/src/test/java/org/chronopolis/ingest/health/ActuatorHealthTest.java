package org.chronopolis.ingest.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.chronopolis.ingest.IntegrationTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for the Actuator health check endpoint:
 * /actuator/health
 *
 * @author lsitu
 */
@AutoConfigureMockMvc
public class ActuatorHealthTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void healthCheckAnonymousTest() throws Exception {
        mockMvc.perform(get("/actuator/health")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.details.db.status").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void healthCheckAuthenticatedTest() throws Exception {
        mockMvc.perform(get("/actuator/health")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.details.db.status").value("UP"));
    }
}