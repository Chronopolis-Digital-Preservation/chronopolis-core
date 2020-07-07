package org.chronopolis.ingest.features;

import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptySet;
import static org.chronopolis.rest.models.enums.FixityAlgorithm.SHA_256;
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
import org.chronopolis.ingest.repository.dao.StagingDao;
import org.chronopolis.ingest.support.FileSizeFormatter;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.BagFile;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.entities.TokenStore;
import org.chronopolis.rest.entities.depositor.Depositor;
import org.chronopolis.rest.entities.storage.Fixity;
import org.chronopolis.rest.entities.storage.ReplicationConfig;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.enums.DataType;
import org.chronopolis.rest.models.enums.StorageType;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(TestBase.class);

    public static final String TEST_DEPOSITOR = "depositor-1";
    public static final String TEST_NODE = "test-node";

    private static final String TAG_FIXITY = "tag-fixity";
    private static final String TOKEN_FIXITY = "token-fixity";

    @Autowired
    private WebApplicationContext context;

    @PersistenceContext
    protected EntityManager entityManager;

    protected WebClient webClient;

    protected PagedDao dao;

    protected Node testNode;

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

        testNode = createNode(TEST_NODE, TEST_NODE, emptySet());
    }

    @After
    public void cleanup() {
        this.webClient.close();

        try {
            dao.delete(testNode);
        } catch (Exception ex) {
            log.warn("Error deleting test node: " + TEST_NODE, ex);
        }
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

    /*
     * Create StorageRegion
     * @return
     */
    protected StorageRegion createStorageRegion(DataType dataType, Node node) {
        StorageRegion region = new StorageRegion();
        region.setCapacity(1000000L);
        region.setStorageType(StorageType.LOCAL);
        region.setDataType(dataType);
        region.setNode(node);
        region.setNote("Storage Region Note");

        ReplicationConfig config = new ReplicationConfig(region,
                "/replication/path",
                "localhost",
                "user");
        region.setReplicationConfig(config);

        dao.save(region);
        return region;
    }

    /*
     * Create BagFile
     * @param bag
     * @return
     */
    protected BagFile bagFile(Bag bag) {
        BagFile bagFile = new BagFile();
        bagFile.setBag(bag);
        bagFile.setFilename("bag_file");
        bagFile.setDtype(StagingDao.DISCRIMINATOR_BAG);
        bagFile.addFixity(new Fixity(now(), bagFile, TAG_FIXITY, SHA_256.getCanonical()));

        dao.save(bagFile);
        return bagFile;
    }

    /*
     * Create TokenStore
     * @param bag
     * @return
     */
    protected TokenStore tokenStore(Bag bag) {
        TokenStore store = new TokenStore();
        store.setBag(bag);
        store.setFilename("token_file");
        store.setDtype(StagingDao.DISCRIMINATOR_TOKEN);
        store.addFixity(new Fixity(now(), store, TOKEN_FIXITY, SHA_256.getCanonical()));

        dao.save(store);
        return store;
    }

    /*
     * Create StagingStorage
     * @param bag
     * @param region
     * @param file
     * @return
     */
    protected StagingStorage stagingStorage(Bag bag, StorageRegion region, DataFile file) {
        StagingStorage storage = new StagingStorage();
        storage.setFile(file);
        storage.setBag(bag);
        storage.setRegion(region);
        storage.setSize(1L);
        storage.setTotalFiles(1L);
        storage.setPath(bag.getDepositor().getNamespace() + "/" + bag.getName());
        storage.setActive(true);
        region.getStorage().add(storage);
        bag.getStorage().add(storage);

        dao.save(bag);
        return storage;
    }

    /*
     * Format capacity
     * @param capacity
     * @return
     */
    protected String formatCapacity(long capacity) {
        FileSizeFormatter formatter = new FileSizeFormatter();
        return formatter.format(capacity);
    }
}
