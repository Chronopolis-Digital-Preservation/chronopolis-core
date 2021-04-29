package org.chronopolis.replicate.scheduled;

import org.chronopolis.common.ace.AceCollections;
import org.chronopolis.common.ace.AceService;
import org.chronopolis.common.ace.GsonCollection;
import org.chronopolis.replicate.batch.Submitter;
import org.chronopolis.replicate.batch.callback.UpdateCallback;
import org.chronopolis.replicate.batch.callback.BagUpdateCallback;
import org.chronopolis.rest.api.BagService;
import org.chronopolis.rest.api.IngestApiProperties;
import org.chronopolis.rest.api.ReplicationService;
import org.chronopolis.rest.api.ServiceGenerator;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.Replication;
import org.chronopolis.rest.models.enums.BagStatus;
import org.chronopolis.rest.models.enums.ReplicationStatus;
import org.chronopolis.rest.models.page.SpringPage;
import org.chronopolis.rest.models.update.BagUpdate;
import org.chronopolis.rest.models.update.ReplicationStatusUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sun.xml.ws.policy.privateutil.PolicyUtils.Collections;

import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.chronopolis.rest.models.enums.ReplicationStatus.*;

/**
 * Scheduled task for checking the ingest-server for replication requests
 * <p>
 * todo check if we can enable configuration properties here
 *
 * <p/>
 * @author shake
 * @since 12/10/14
 */
@Component
@EnableScheduling
public class ReplicationQueryTask {
    private final Logger log = LoggerFactory.getLogger(ReplicationQueryTask.class);

    private static final List<ReplicationStatus> ACTIVE_STATUS =
            Arrays.asList(ACE_AUDITING, ACE_TOKEN_LOADED, ACE_REGISTERED, TRANSFERRED, STARTED, PENDING);

    private final IngestApiProperties properties;
    private final ReplicationService replications;
    private final Submitter submitter;
    private final BagService bagService;
    private final AceCollections aceCollections;

    @Autowired
    public ReplicationQueryTask(IngestApiProperties properties,
                                ServiceGenerator generator,
                                Submitter submitter,
                                AceCollections aceCollections) {
        this.properties = properties;
        this.submitter = submitter;
        this.replications = generator.replications();
        this.bagService = generator.bags();
        this.aceCollections = aceCollections;
    }


    /**
     * Check the ace-am server for removed collections
     * <p>
     */
    @Scheduled(cron = "${replication.removed.cron:0 0 * * * *}")
    public void checkForRemovedCollections() {
        log.info("Querying for removed collections on ace ...");

        try {
	        Call<List<GsonCollection>> gsonCall = aceCollections.getCollections(null, false, false, true);
	        Response<List<GsonCollection>> gsonResponse = gsonCall.execute();
	        List<GsonCollection> colls = gsonResponse.body();
  
            log.info("Retrieved {} collection(s) with REMOVED state from ace-am.", colls.size());     

            for (GsonCollection coll : colls) {
	        	Map<String,String> params = new HashMap<>();
	        	params.put("bag", coll.getName());
		        Call<SpringPage<Replication>> replsCall = replications.get(params);
		        Response<SpringPage<Replication>> response = replsCall.execute();
		        SpringPage<Replication> repls = response.body();

		        log.debug("Found {} replication(s) in removed collection {}.", repls.getTotalElements(), coll.getName()); 

		        int removedCount = 0;
		        for (Replication repl : repls) {
		        	if (coll.getState().equals("R") && repl.getStatus() != ReplicationStatus.REMOVED) {
		        		if (repl.getNode().equals(properties.getUsername())) {
			        		// check for existence of the replication files
			        		Path collPath = Paths.get(coll.getDirectory());
			        		boolean collPathExist = Files.exists(collPath);
			        		int collFiles = collPathExist ? Files.list(collPath).map(Path::toFile).collect(Collectors.toList()).size() : -1;

			        		log.info("Remove replication {} [node {}, username {}, status {}] for collection {} [state {}, directory {}, files {}].",
			        				repl.getId(), repl.getNode(), properties.getUsername(), repl.getStatus(), coll.getName(), coll.getState(), coll.getDirectory(), collFiles); 
			        		
			        		if (!collPathExist || collFiles <= 0) {
					        	Call<Replication> replCall = replications.updateStatus(repl.getId(),
					                    new ReplicationStatusUpdate(ReplicationStatus.REMOVED));
					            replCall.enqueue(new UpdateCallback());
			        		}
			        	}
		        	} else {
		        		removedCount++;
		        	}

		        	// If all replications are removed (with REMOVED status), update Bag status to be DELETED
		        	if (removedCount == repls.getNumberOfElements() && repl.getBag().getStatus() != BagStatus.DELETED) {
			            Call<Bag> bagCall = bagService.update(repl.getBag().getId(),
			                    new BagUpdate(null, BagStatus.DELETED));
			            bagCall.enqueue(new BagUpdateCallback());

			            log.info("Update status of collection {} to be DELETED!", repl.getBag().getName());
		        	}
		        }
	        }
        } catch (IOException e) {
        	log.error("Error checking for removed replications", e);
        }
    }

    /**
     * Check the ingest-server for pending and started replications
     * <p>
     */
    @Scheduled(cron = "${replication.cron:0 0 * * * *}")
    public void checkForReplications() {
        log.info("Querying for replications");

        Query q = query(ACTIVE_STATUS);
        if (!q.success) {
            log.error("Error checking for replications", q.t);
        }
    }

    /**
     * Query the ingest-server and add requests to the {@link Submitter}
     * if they are not already being replicated
     *
     * @param status the status of the request to get
     */
    private Query query(List<ReplicationStatus> status) {
        int page = 0;
        int pageSize = 20;

        Query q = new Query(true);

        // TODO: As replications get updated, the state can change and alter the
        // amount of pages. For now we'll handle at most 1 page at a time, and maybe
        // introduce new query methods to the api later (e.g. /api/nodes/my-nodes/replications)
        try {
            List<String> actuveStatus = status.stream().map(s -> s.toString()).collect(Collectors.toList());
            Call<SpringPage<Replication>> call = replications.get(page, pageSize,
                    properties.getUsername(), actuveStatus);
            Response<SpringPage<Replication>> response = call.execute();
            SpringPage<Replication> replications = response.body();
            log.trace("[{}] On page {} with {} replications. {} total.", status,
                    replications.getNumber(),
                    replications.getNumberOfElements(),
                    replications.getTotalElements());

            ++page;

            startReplications(replications);
        } catch (IOException e) {
            q = new Query(false, e);
        }

        return q;
    }

    private void startReplications(Iterable<Replication> replications) {
        for (Replication replication : replications) {
            submitter.submit(replication);
        }
    }

    private class Query {
        private final boolean success;
        @Nullable
        private Throwable t;

        public Query(boolean success) {
            this.success = success;
        }

        public Query(boolean success, @Nullable Throwable t) {
            this.success = success;
            this.t = t;
        }
    }

}
