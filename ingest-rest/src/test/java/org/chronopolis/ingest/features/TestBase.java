package org.chronopolis.ingest.features;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.chronopolis.ingest.IngestTest;
import org.chronopolis.ingest.JpaContext;
import org.chronopolis.ingest.repository.dao.PagedDao;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.entities.depositor.Depositor;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Abstract base class for integration test
 * @author lsitu
 * 06/03/2020
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WithMockUser(roles = "ADMIN")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = JpaContext.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public abstract class TestBase extends IngestTest {

    public static final String TEST_DEPOSITOR = "depositor-1";
    public static final String TEST_NODE = "test-node";

    @Autowired
    private WebApplicationContext context;

    @PersistenceContext
    protected EntityManager entityManager;

    protected WebClient webClient;

    protected PagedDao dao;

    @LocalServerPort
    private int serverPort;

    @Before
    public void setup() {
        webClient = MockMvcWebClientBuilder
            .webAppContextSetup(context, springSecurity())
            .contextPath("")
            .build();

        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);

        dao = new PagedDao(entityManager);
    }

    @After
    public void cleanup() {
        this.webClient.close();
    }

    protected String getUrl(String path) {
        return "http://localhost:" + serverPort + path;
    }

    /*
     * Create Node
     * @return
     */
    protected Node createNode(String userName, String password, Set<Replication> repls) {
        Node node = new Node();
        node.setUsername(userName);
        node.setPassword(password);
        node.setReplications(repls);
        node.setEnabled(true);

        dao.save(node);
        return node;
    }

    /**
     * Create Depositor
     * @param namespace
     * @param sourceOrganization
     * @param organizationAddress
     * @param nodes
     * @return
     */
    protected Depositor createDepositor(String namespace, String sourceOrganization, 
            String organizationAddress, List<Node> nodes) {
        Depositor depositor = new Depositor();
        // handle late inits for contact list
        depositor.setContacts(new HashSet<>());
        depositor.setNodeDistributions(new HashSet<>());
        depositor.setNamespace(namespace);
        depositor.setSourceOrganization(sourceOrganization);
        depositor.setOrganizationAddress(organizationAddress);
        nodes.forEach(depositor::addNodeDistribution);

        dao.save(depositor);
        return depositor;
    }
}
