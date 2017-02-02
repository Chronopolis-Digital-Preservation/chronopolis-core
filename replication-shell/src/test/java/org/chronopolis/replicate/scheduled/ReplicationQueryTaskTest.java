package org.chronopolis.replicate.scheduled;

import org.chronopolis.common.settings.IngestAPISettings;
import org.chronopolis.replicate.batch.Submitter;
import org.chronopolis.replicate.support.CallWrapper;
import org.chronopolis.replicate.test.TestApplication;
import org.chronopolis.rest.api.IngestAPI;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.RStatusUpdate;
import org.chronopolis.rest.models.Replication;
import org.chronopolis.rest.models.ReplicationStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import retrofit2.Call;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.when;


/**
 * Tests for the ReplicationQueryTask
 * The main goal is to make sure we filter properly when checking against the
 * JobExplorer. To do this we mock a few objects (the IngestAPI and JobStarter)
 * in order to ensure our task handles the returns gracefully.
 *
 * In addition we use the @FixMethodOrder annotation because we want to test
 * when a Job is running, but only after we test when no jobs are running. This
 * is used to ensure the order of execution from JUnit.
 *
 * Todo: These tests are somewhat out of date now that {@link Submitter} handles
 * the filtering of replications
 *
 * Created by shake on 3/30/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringApplicationConfiguration(classes = TestApplication.class)
public class ReplicationQueryTaskTest {

    @Mock IngestAPI ingestAPI;
    @Mock IngestAPISettings settings;

    @Autowired Submitter submitter;
    @InjectMocks ReplicationQueryTask task;

    private Bag b;
    private Replication replication;

    private Call<org.chronopolis.rest.models.Bag> bagCall;
    private Call<PageImpl<Replication>> replications;

    @BeforeClass
    public static void before() {
        // Make sure we don't try to write in to /var/log by default
        // I'm not sure why that's happening but it's something we'll have to look in to
        System.setProperty("logging.file", "test.log");
        System.setProperty("spring.profiles.active", "test");
        System.setProperty("smtp.send", "false");
    }

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);

        // Make sure the autowired JobExplorer gets used
        ArrayList<Replication> replicationList = new ArrayList<>();
        Node n = new Node("test", "test");
        b = new Bag()
                .setName("test-name")
                .setDepositor("test-depositor")
                .setSize(0L)
                .setTotalFiles(0L);

        replication = new Replication()
                .setStatus(ReplicationStatus.PENDING)
                .setBag(b)
                .setNode(n.getUsername());
        replication.setId(1L);
        for (int i = 0; i < 5; i++) {
            replicationList.add(replication);
        }

        PageImpl<Replication> page = new PageImpl<>(replicationList);

        replications = new CallWrapper<>(page);
        bagCall = new CallWrapper<>(b);

        task = new ReplicationQueryTask(settings, ingestAPI, submitter);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckForReplications() throws Exception {
        when(ingestAPI.getReplications(anyMap())).thenReturn(replications);

        // Ok so this is kind of bad behavior, but our bag has a null id so...
        when(ingestAPI.getBag(b.getId())).thenReturn(bagCall);
        when(ingestAPI.updateReplicationStatus(anyLong(), any(RStatusUpdate.class)))
                .thenReturn(new CallWrapper<>(replication));
        task.checkForReplications();

        // We should have only executed our job starter once
        // verify(jobStarter).addJobFromRestful(replication);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckForReplicationsFilter() throws Exception {
        // Create a job that only sleeps in the same way we build our replication
        // jobs to make sure we filter properly when querying the job explorer
        when(ingestAPI.getReplications(anyMap())).thenReturn(replications);
        when(ingestAPI.getBag(b.getId())).thenReturn(bagCall);
        when(ingestAPI.updateReplicationStatus(anyLong(), any(RStatusUpdate.class)))
                .thenReturn(new CallWrapper<>(replication));

        task.checkForReplications();
    }

}