package org.chronopolis.replicate.scheduled;

import com.google.common.collect.ImmutableSet;

import org.chronopolis.common.ace.AceCollections;
import org.chronopolis.common.ace.AceConfiguration;
import org.chronopolis.common.ace.GsonCollection;
import org.chronopolis.common.storage.Posix;
import org.chronopolis.replicate.batch.Submitter;
import org.chronopolis.rest.api.BagService;
import org.chronopolis.rest.api.IngestApiProperties;
import org.chronopolis.rest.api.ReplicationService;
import org.chronopolis.rest.api.ServiceGenerator;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.Replication;
import org.chronopolis.rest.models.enums.BagStatus;
import org.chronopolis.rest.models.enums.ReplicationStatus;
import org.chronopolis.rest.models.page.SpringPage;
import org.chronopolis.rest.models.page.SpringPageKt;
import org.chronopolis.rest.models.update.BagUpdate;
import org.chronopolis.rest.models.update.ReplicationStatusUpdate;
import org.chronopolis.test.support.CallWrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import retrofit2.Call;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.ZonedDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;


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
public class ReplicationQueryTaskTest {

    private final int NUM_REPLICATIONS = 5;

    @Mock private Submitter submitter;
    @Mock private ReplicationService replicationService;
    @Mock private ServiceGenerator generator;
    @Mock private AceCollections aceCollections;
    @Mock private AceConfiguration aceConfiguration;
    @Mock private BagService bagService;
    @Mock private Posix posix;

    private Bag bag = null;
    private Replication replication = null;
    private ReplicationQueryTask task;
    private Call<SpringPage<Replication>> replications;
    private Call<List<GsonCollection>> removedAceColls;
    private Call<SpringPage<Replication>> removedReplications;
    private Call<Replication> removedReplication;
    private Call<Bag> deletedBag;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        IngestApiProperties properties = new IngestApiProperties();
        properties.setUsername("test");
        
        when(generator.replications()).thenReturn(replicationService);
        when(generator.bags()).thenReturn(bagService);

        // Init our RQT
        task = new ReplicationQueryTask(properties, generator, submitter, aceCollections);

        // Init our returned objects
        ArrayList<Replication> replicationList = new ArrayList<>();
        bag = new Bag(1L, 1L, 1L, null, null, now(), now(), "test-name", "repl-query-test",
                "test-depositor", BagStatus.REPLICATING, ImmutableSet.of());

        replication = new Replication(1L, now(), now(), ReplicationStatus.PENDING,
                "bag-link", "token-link", "protocol", "", "", "test", bag);
        for (int i = 0; i < NUM_REPLICATIONS; i++) {
            replicationList.add(replication);
        }

        replications = new CallWrapper<>(SpringPageKt.wrap(replicationList));

        when(posix.getPath()).thenReturn("/replications/any/path");
    	GsonCollection.Builder builder = new GsonCollection.Builder()
                .name(bag.getName())
                .digestAlgorithm("SHA-256")
                .group(replication.getBag().getDepositor())
                .auditPeriod(aceConfiguration.getAuditPeriod().toString())
                .auditTokens("true")
                .proxyData("false")
                .storage("local")
                .state("R")
                .directory(Paths.get(posix.getPath()).resolve(bag.getName()).toString());
 
    	GsonCollection gsonColl = builder.build();
        List<GsonCollection> gsonColls = Arrays.asList(gsonColl);

        removedAceColls = new CallWrapper<>(gsonColls);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckForReplications() throws IOException {
        when(replicationService.get(eq(0), eq(20), anyString(), anyList())).thenReturn(replications);
        task.checkForReplications();

        // We have 6 types of replication states for querying at once
        verify(replicationService, times(1)).get(eq(0), eq(20), anyString(), anyList());
        verify(submitter, times(NUM_REPLICATIONS)).submit(any(Replication.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckForRemovedReplications() throws IOException {
        List<Replication> removedReplicationList = Arrays.asList(replication);
        removedReplications = new CallWrapper<>(SpringPageKt.wrap(removedReplicationList));

        removedReplication = new CallWrapper<>(replication);
 
    	when(aceCollections.getRemovedCollections())
    			.thenReturn(removedAceColls);

    	Map<String, String> params = new HashMap<>();
    	params.put("bag", "test-name");

    	when(replicationService.get(params)).thenReturn(removedReplications);
    	when(replicationService.updateStatus(eq(replication.getId()), any(ReplicationStatusUpdate.class)))
    			.thenReturn(removedReplication);
        when(bagService.update(eq(bag.getId()), any(BagUpdate.class))).thenReturn(deletedBag);

        task.checkForRemovedCollections();

        // Call to ace-am server to retrieve removed collections
        verify(aceCollections, times(1)).getRemovedCollections();
        // Call to ingest server to retrieve replications of the ace collection
        verify(replicationService, times(1)).get(params);
        // Call to ingest server to update the status of the replication
        verify(replicationService, times(1)).updateStatus(replication.getId(),
        		new ReplicationStatusUpdate(ReplicationStatus.REMOVED));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAutoUpdateDeletedBagStatus() throws IOException {
    	// Create replication with REMOVED status
    	replication = new Replication(1L, now(), now(), ReplicationStatus.REMOVED,
                "bag-link", "token-link", "protocol", "", "", "test", bag);
        List<Replication> removedReplicationList = Arrays.asList(replication);
 
        removedReplications = new CallWrapper<>(SpringPageKt.wrap(removedReplicationList)); 
        deletedBag = new CallWrapper<>(bag);

        when(aceCollections.getRemovedCollections())
    			.thenReturn(removedAceColls);

    	Map<String, String> params = new HashMap<>();
    	params.put("bag", "test-name");

    	when(replicationService.get(params)).thenReturn(removedReplications);
        when(bagService.update(eq(bag.getId()), any(BagUpdate.class))).thenReturn(deletedBag);

        task.checkForRemovedCollections();

        // Call to ace-am server to retrieve removed collections
        verify(aceCollections, times(1)).getRemovedCollections();
        // Call to ingest server to retrieve replications of the ace collection
        verify(replicationService, times(1)).get(params);
        // Call to ingest server to update the status of the collection
        verify(bagService, times(1)).update(bag.getId(), new BagUpdate(null, BagStatus.DELETED));
    }
}