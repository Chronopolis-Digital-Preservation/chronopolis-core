package org.chronopolis.replicate.service;

import org.chronopolis.common.settings.IngestAPISettings;
import org.chronopolis.replicate.batch.ReplicationJobStarter;
import org.chronopolis.rest.api.IngestAPI;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.RStatusUpdate;
import org.chronopolis.rest.models.ReplicationStatus;
import org.chronopolis.rest.support.BagConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.chronopolis.rest.models.ReplicationStatus.STARTED;

/**
 * Service for running the replication-shell in development mode. Give a prompt for interacting
 * with the user, allowing them to decide when to query the ingest-server
 *
 *
 * Created by shake on 2/23/15.
 */
@Component
@Profile("development")
public class CommandLineService implements ReplicationService {
    private final Logger log = LoggerFactory.getLogger(CommandLineService.class);

    @Autowired
    ApplicationContext context;

    @Autowired
    IngestAPISettings settings;

    @Autowired
    IngestAPI ingestAPI;

    @Autowired
    ReplicationJobStarter jobStarter;

    /**
     * Main entry point for the class, display the prompt and when we receive
     * the QUIT option, tell Spring to shutdown
     *
     */
    @Override
    public void replicate() {
        boolean done = false;
        while (!done) {
            OPTION option = inputOption();
            if (option.equals(OPTION.RESTFUL_QUERY)) {
                log.info("Query for active replications");
                query(STARTED, false);

                log.info("Query for new replications");
                query(ReplicationStatus.PENDING, true);
            } else if (option.equals(OPTION.QUIT)) {
                log.info("Quitting");
                done = true;
            }

        }

        SpringApplication.exit(context);
    }

    /**
     * Create a prompt and read the input for the next option
     *
     * @return the input given
     */
    private OPTION inputOption() {
        OPTION option = OPTION.UNKNOWN;
        while (option.equals(OPTION.UNKNOWN)) {
            StringBuilder sb = new StringBuilder("Enter Option: ");
            String sep = " | ";
            for (OPTION value : OPTION.values()) {
                if (!value.equals(OPTION.UNKNOWN)) {
                    sb.append(value.name());
                    sb.append(" [");
                    sb.append(value.name().charAt(0));
                    sb.append("]");
                    sb.append(sep);
                }
            }

            //The one difference, mwahhaha
            sb.replace(sb.length() - sep.length(), sb.length(), " -> ");
            System.out.println(sb.toString());
            option = OPTION.fromString(readLine().trim());
        }
        return option;
    }

    /**
     * Send queries to the ingest-server to receive ongoing replications
     *
     * @param status - the status of replications to query for
     * @param update - whether or not to update the requests while replicating
     */
    private void query(ReplicationStatus status, boolean update) {
        Map<String, Object> params = new HashMap<>();
        Page<Replication> replications;
        params.put("status", status);
        params.put("node", settings.getIngestAPIUsername());
        Call<PageImpl<Replication>> call = ingestAPI.getReplications(params);
        try {
            Response<PageImpl<Replication>> execute = call.execute();
            replications = execute.body();
        } catch (IOException e) {
            log.error("Error getting replications from server", e);
            return;
        }

        log.debug("Found {} replications", replications.getNumberOfElements());

        for (Replication replication : replications) {
            log.info("Starting job for replication id {}", replication.getId());
            try {
                Call<Bag> bcall = ingestAPI.getBag(replication.getBagId());
                Response<Bag> execute = bcall.execute();
                Bag bag = execute.body();
                replication.setBag(BagConverter.toBagEntity(bag));
            } catch (IOException e) {
                log.error("", e);
                continue;
            }
            if (update) {
                log.info("Updating replication");
                // replication.setStatus(ReplicationStatus.STARTED);
                // ingestAPI.updateReplication(replication.getId(), replication);
                final long id = replication.getId();
                Call<Replication> statusCall = ingestAPI.updateReplicationStatus(replication.getId(), new RStatusUpdate(STARTED));
                statusCall.enqueue(new Callback<Replication>() {
                    @Override
                    public void onResponse(Response<Replication> response) {
                        log.debug("Update to replication {}: {} - {}", new Object[]{
                                id,
                                response.code(),
                                response.message()
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        log.error("Error communicating with the ingest server", throwable);
                    }
                });
            }
            jobStarter.addJobFromRestful(replication);
        }
    }

    /**
     * Read in from stdin
     *
     * @return
     */
    private String readLine() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } catch (IOException ex) {
            throw new RuntimeException("Can't read from STDIN");
        }
    }

    private enum OPTION {
        RESTFUL_QUERY, QUIT, UNKNOWN;

        private static OPTION fromString(String text) {
            switch (text) {
                case "R":
                case "r":
                    return RESTFUL_QUERY;
                case "Q":
                case "q":
                    return QUIT;
                default:
                    return UNKNOWN;
            }
        }
    }

}
