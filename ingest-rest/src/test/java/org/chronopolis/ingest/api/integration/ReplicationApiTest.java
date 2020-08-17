package org.chronopolis.ingest.api.integration;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chronopolis.ingest.repository.dao.ReplicationDao;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.BagDistributionStatus;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.entities.QDataFile;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.entities.depositor.Depositor;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.enums.BagStatus;
import org.chronopolis.rest.models.enums.DataType;
import org.chronopolis.rest.models.enums.ReplicationStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * API integration tests for ReplicationController
 *
 * @author lsitu
 * 07/21/2020
 */

public class ReplicationApiTest extends ApiTestBase {
    protected static final String TEST_BAG = "test-bag";
    protected static final String TEST_CREATOR = "test-creator";

    private Depositor depositor;
    private StorageRegion  regionBag;
    private StorageRegion  regionToken;
    private Bag testBag;

    private List<Replication> replications = null;

    @Before
    public void initTest() {
        List<Node> nodes = Arrays.asList(testNode);
        depositor = createDepositor(TEST_DEPOSITOR, "Organization Name",
                "Organization Address", nodes);

        regionBag = createStorageRegion(DataType.BAG, testNode);

        regionToken = createStorageRegion(DataType.TOKEN, testNode);

        testBag = createBagWithFiles(TEST_BAG, TEST_CREATOR, depositor, regionBag, regionToken,
                Arrays.asList(testNode), BagStatus.REPLICATING);
    }

    @After
    public void done() {
        if (replications != null) {
            for (Replication repl : replications) {
                dao.delete(repl);
            }
        }

        if (testBag != null) {
            Set<StagingStorage> storages = testBag.getStorage();

            testBag.getFiles().clear();
            testBag.getStorage().clear();
            dao.save(testBag);

            for (StagingStorage ss : storages) {
                dao.delete(ss);
            }

            List<DataFile> dataFiles = dao.findAll(QDataFile.dataFile);
            for (DataFile df : dataFiles) {
                dao.delete(df);
            }

            dao.delete(testBag);
        }

        dao.delete(regionBag);
        dao.delete(regionToken);
        dao.delete(depositor);
    }

    @Test
    public void getReplicationsTest() throws Exception {
        // create two replications with status ACE_REGISTERED and PENDING
        ReplicationStatus[] replStatus = {ReplicationStatus.ACE_REGISTERED, ReplicationStatus.PENDING};
        replications = Stream.of(replStatus).map(this::createTestReplication).collect(Collectors.toList());

        assertEquals(2, replications.size());

        // verify only the replication with status ACE_REGISTERED will be retrieved with status ACE_REGISTERED
        String[] statusParam = {String.valueOf(ReplicationStatus.ACE_REGISTERED)};
        mockMvc.perform(get("/api/replications")
                .param("status", statusParam)
                .param("node", testNode.getUsername())
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("" + ReplicationStatus.ACE_REGISTERED))
                .andExpect(jsonPath("$.content[0].node").value(testNode.getUsername()))
                .andExpect(jsonPath("$.content[0].bag.name").value(testBag.getName()));
    }

    @Test
    public void getReplicationsWithMultipleStatusListTest() throws Exception {
        // create two replications with status ACE_REGISTERED and PENDING
        ReplicationStatus[] replStatus = {ReplicationStatus.ACE_REGISTERED, ReplicationStatus.PENDING};
        replications = Stream.of(replStatus).map(this::createTestReplication).collect(Collectors.toList());

        assertEquals(2, replications.size());

        // verify both replications created will be retrieved from API with their status
        List<String> statuses = Stream.of(replStatus).map(s -> s.toString()).collect(Collectors.toList());
        mockMvc.perform(get("/api/replications")
                .param("status", statuses.toArray(new String[statuses.size()]))
                .param("node", testNode.getUsername())
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.numberOfElements").value(2));
    }

    /*
     * Create test replication with ReplicationStatus
     * @param status
     * @return
     */
    private Replication createTestReplication(ReplicationStatus status) {
        return createTestReplication(status, testNode, testBag, regionBag, regionToken);
    }

    /*
     * Create collection with files
     * @param collectionName
     * @param creator
     * @param depositor
     * @param regionBag
     * @param regionToken
     * @param nodes
     * @param status - BagStatus
     * @return
     */
    protected Bag createBagWithFiles(String collectionName, String creator, Depositor depositor,
            StorageRegion regionBag, StorageRegion regionToken, List<Node> nodes, BagStatus status) {
        Bag bag = new Bag(collectionName, creator, depositor, 1L, 1L, status);
        for (Node n : nodes) {
            bag.addDistribution(n, BagDistributionStatus.DISTRIBUTE);
        }
        dao.save(bag);
 
        // create bag file and staging storage for bag
        DataFile bFile = bagFile(bag);
        stagingStorage(bag, regionBag, bFile);
        // create token file and staging storage for token
        DataFile tStore = tokenStore(bag);
        stagingStorage(bag, regionToken, tStore);

        return bag;
    }

    /*
     * Create replication
     * @param status
     * @param node
     * @param bag
     * @param  regionBag - StorageRegion for collection
     * @param regionToken - StorageRegion for Token
     * @return
     */
    protected Replication createTestReplication(ReplicationStatus status, Node node, Bag bag,
        StorageRegion regionBag, StorageRegion regionToken) {
        StagingStorage storageBag = regionBag.getStorage().iterator().next();
        StagingStorage storageToken = regionToken.getStorage().iterator().next();
        String bagDlLink = ReplicationDao.createReplicationString(storageBag, true);
        String tokenDlLink = ReplicationDao.createReplicationString(storageToken, false);

        Replication repl = new Replication(status, node, bag,
            bagDlLink, tokenDlLink, "rsync", null, null);

        dao.save(repl);

    	return repl;
    }
}