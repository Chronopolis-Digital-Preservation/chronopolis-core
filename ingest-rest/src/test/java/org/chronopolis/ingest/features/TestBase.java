package org.chronopolis.ingest.features;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.chronopolis.ingest.IntegrationTestBase;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Abstract base class for integration test
 * @author lsitu
 * 06/03/2020
 */

@WithMockUser(roles = "ADMIN")
public abstract class TestBase extends IntegrationTestBase {
    @Autowired
    private WebApplicationContext context;

    protected WebClient webClient;

    @Before
    public void setup() {
        super.setup();

        webClient = MockMvcWebClientBuilder
            .webAppContextSetup(context, springSecurity())
            .contextPath("")
            .build();

        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
    }

    @After
    public void cleanup() {
        super.cleanup();

        this.webClient.close();
    }
}
