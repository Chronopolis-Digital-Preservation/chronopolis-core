package org.chronopolis.replicate.batch.listener;

import org.chronopolis.common.mail.MailUtil;
import org.chronopolis.replicate.ReplicationNotifier;
import org.chronopolis.replicate.config.ReplicationSettings;
import org.chronopolis.rest.api.IngestAPI;
import org.chronopolis.rest.models.FixityUpdate;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.models.ReplicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

import static org.chronopolis.replicate.batch.listener.Util.sendFailure;

/**
 * @deprecated will be removed by 1.4.0-RELEASE
 *
 * Step listener for after the token-download step when triggered from the RESTful
 * interface. If there was an error validating the token store, we want to stop
 * the job so that we do not attempt to register bad tokens with ACE.
 *
 * Created by shake on 12/4/14.
 */
@Deprecated
public class TokenRESTStepListener implements StepExecutionListener {
    private final Logger log = LoggerFactory.getLogger(TokenRESTStepListener.class);

    private MailUtil mail;
    private IngestAPI ingestAPI;
    private Replication replication;
    private ReplicationSettings settings;
    private ReplicationNotifier notifier;

    public TokenRESTStepListener(MailUtil mail,
                                 IngestAPI ingestAPI,
                                 Replication replication,
                                 ReplicationSettings settings,
                                 ReplicationNotifier notifier) {
        this.mail = mail;
        this.ingestAPI = ingestAPI;
        this.replication = replication;
        this.settings = settings;
        this.notifier = notifier;
    }

    @Override
    public void beforeStep(final StepExecution stepExecution) {

    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        // Check if we were able to download, if not let the ingest-server know
        if (notifier.isSuccess()) {
            log.trace("successful download");
            String digest = notifier.getCalculatedTokenDigest();

            replication.setReceivedTokenFixity(digest);

            // There's a chance this can fail, but we can still rsync the bag? maybe?
            Replication updated;
            // Call<Replication> call = ingestAPI.updateReplication(replication.getId(), replication);

            Call<Replication> call = ingestAPI.updateTokenStore(replication.getId(), new FixityUpdate(digest));
            try {
                updated = call.execute().body();
            } catch (IOException e) {
                log.error("Unable to update replication", e);
                return ExitStatus.FAILED;
            }

            if (updated.getStatus() == ReplicationStatus.FAILURE_TOKEN_STORE) {
                log.error("Error validating token store");
                // stop the execution
                stepExecution.upgradeStatus(BatchStatus.STOPPED);
                updated.setBag(replication.getBag());
                updated.setNodeUser(settings.getNode());
                sendFailure(mail, settings, updated, stepExecution.getFailureExceptions());
                return ExitStatus.FAILED;
            }
        } else {
            log.trace("unsuccessful download");

            // General failure
            // TODO: UpdateCallback
            Call<Replication> call = ingestAPI.failReplication(replication.getId());
            call.enqueue(new Callback<Replication>() {
                @Override
                public void onResponse(Response<Replication> response) {
                    log.debug("Update to replication {}: {} - {}", new Object[]{replication.getId(),
                            response.code(),
                            response.message()});
                }

                @Override
                public void onFailure(Throwable throwable) {
                    log.error("Error communicating with ingest server", throwable);
                }
            });
            sendFailure(mail, settings, replication, stepExecution.getFailureExceptions());
        }

        return ExitStatus.COMPLETED;
    }
}
