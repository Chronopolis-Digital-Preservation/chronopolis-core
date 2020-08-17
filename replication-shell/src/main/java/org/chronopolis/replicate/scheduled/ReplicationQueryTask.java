package org.chronopolis.replicate.scheduled;

import org.chronopolis.replicate.batch.Submitter;
import org.chronopolis.rest.api.IngestApiProperties;
import org.chronopolis.rest.api.ReplicationService;
import org.chronopolis.rest.api.ServiceGenerator;
import org.chronopolis.rest.models.Replication;
import org.chronopolis.rest.models.enums.ReplicationStatus;
import org.chronopolis.rest.models.page.SpringPage;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    public ReplicationQueryTask(IngestApiProperties properties,
                                ServiceGenerator generator,
                                Submitter submitter) {
        this.properties = properties;
        this.submitter = submitter;
        this.replications = generator.replications();
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
