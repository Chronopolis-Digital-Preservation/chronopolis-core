package org.chronopolis.ingest.api.integration;

import org.chronopolis.ingest.IntegrationTestBase;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Abstract base class for API test
 * @author lsitu
 * 07/21/2020
 */

@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
public abstract class ApiTestBase extends IntegrationTestBase {

	@Autowired
    protected MockMvc mockMvc;

    @Before
    public void setup() {
        super.setup();
    }

    @After
    public void cleanup() {
        super.cleanup();
    }
}
