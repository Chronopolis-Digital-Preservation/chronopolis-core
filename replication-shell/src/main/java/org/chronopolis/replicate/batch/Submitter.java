package org.chronopolis.replicate.batch;

import org.chronopolis.common.ace.AceService;
import org.chronopolis.common.mail.MailUtil;
import org.chronopolis.replicate.ReplicationNotifier;
import org.chronopolis.replicate.batch.ace.AceRunner;
import org.chronopolis.replicate.batch.transfer.BagTransfer;
import org.chronopolis.replicate.batch.transfer.TokenTransfer;
import org.chronopolis.replicate.config.ReplicationSettings;
import org.chronopolis.rest.api.IngestAPI;
import org.chronopolis.rest.entities.Replication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiConsumer;

/**
 * Submit a replication for processing
 * TODO: Figure out if we want to eagerly reject replications (i.e., keep our queues bounded)
 *       This way we can simulate back pressure, and not worry about running our of memory if
 *       we receive too many replications. It just has other implications surrounding how we
 *       track which replications are ongoing, and how to handle them. In addition, we'll
 *       want to find a way to resubmit operations to our io pool, possibly through a different
 *       ExecutorService, but testing will need to be done to see how that plays with the
 *       CompletableFuture interface.
 *
 * Created by shake on 10/12/16.
 */
public class Submitter {
    private final Logger log = LoggerFactory.getLogger(Submitter.class);

    MailUtil mail;
    AceService ace;
    IngestAPI ingest;
    ReplicationSettings settings;
    ThreadPoolExecutor io;
    ThreadPoolExecutor http;

    Set<String> replicating;

    @Autowired
    public Submitter(MailUtil mail,
                     AceService ace,
                     IngestAPI ingest,
                     ReplicationSettings settings,
                     ThreadPoolExecutor io,
                     ThreadPoolExecutor http) {
        this.ace = ace;
        this.ingest = ingest;
        this.settings = settings;
        this.io = io;
        this.http = http;

        this.replicating = new ConcurrentSkipListSet<>();
    }

    /**
     * submit a replication for processing, returning the future which it is bound by
     * todo: {@link Optional<CompletableFuture<Void>>}
     *
     * @param replication the replication to work on
     * @return a {@link CompletableFuture} of the replication flow
     */
    public CompletableFuture<Void> submit(Replication replication) {
        String identifier = replication.getBag().getDepositor() + "/" + replication.getBag().getName();

        if (replicating.add(identifier)) {
            log.info("Submitting replication {}", identifier);
            CompletableFuture<Void> future;
            switch (replication.getStatus()) {
                // Run through the full flow
                case PENDING:
                case STARTED:
                    future = fromPending(replication);
                    break;
                // Do all ACE work
                case TRANSFERRED:
                case ACE_REGISTERED:
                case ACE_TOKEN_LOADED:
                    future = fromTransferred(null, replication);
                    break;
                case ACE_AUDITING:
                    future = fromAceAuditing(replication);
                    break;
                default:
                    return null;
            }

            return future.whenComplete(new Completer(replication));
        } else {
            log.debug("Replication {} is already running", identifier);
        }

        return null;
    }

    /**
     * Create a CompletableFuture with all steps needed from the PENDING state
     *
     * @param replication the replication to work on
     * @return a {@link CompletableFuture} which runs both the token and bag transfers tasks
     */
    private CompletableFuture<Void> fromPending(Replication replication) {
        BagTransfer bxfer = new BagTransfer(replication, ingest, settings);
        TokenTransfer txfer = new TokenTransfer(replication, ingest, settings);

        // todo: maybe we shouldn't chain together fromTransferred?
        return fromTransferred(CompletableFuture.runAsync(txfer, io)
                .thenRunAsync(bxfer, io),
                replication);
    }

    /**
     * Create a CompletableFuture with all steps needed after a transfer has completed
     *
     * @param future      the future to append to or null
     * @param replication the replication to work on
     * @return a completable future which runs ace registration tasks
     */
    private CompletableFuture<Void> fromTransferred(@Nullable CompletableFuture<Void> future, Replication replication) {
        ReplicationNotifier notifier = new ReplicationNotifier(replication);
        AceRunner runner = new AceRunner(ace, ingest, replication.getId(), settings, notifier);
        if (future == null) {
            return CompletableFuture.runAsync(runner, http);
        }

        return future.thenRunAsync(runner, http);
    }

    /**
     * Create a CompletableFuture for checking the status of ACE audits
     *
     * @return a {@link CompletableFuture} which runs the AceCheck runnable
     */
    private CompletableFuture<Void> fromAceAuditing(Replication replication) {
        AceCheck check = new AceCheck(ace, ingest, replication);
        return CompletableFuture.runAsync(check, http);
    }


    /**
     * Consumer which runs at the end of a replication, ensures removal from the
     * replicating set
     *
     * todo: send mail (eventually switch to a better interface for notifications
     *                  so we can have multiple outputs. i.e. email, slack, db,
     *                  etc)
     *
     */
    private class Completer implements BiConsumer<Void, Throwable> {
        private final Logger log = LoggerFactory.getLogger(Completer.class);

        final Replication replication;

        public Completer(Replication replication) {
            this.replication = replication;
        }

        @Override
        public void accept(Void aVoid, Throwable throwable) {
            String s = replication.getBag().getDepositor() + "/" + replication.getBag().getName();
            String subject = "Successful replication of " + s;
            boolean send = settings.sendOnSuccess();
            String body = "";

            // TODO: Check here for fatal exceptions, and cancel replication if necessary
            if (throwable != null) {
                subject = "Failed to replicate " + s;
                log.warn("Replication did not complete successfully, returned throwable is", throwable);
                body = throwable.getMessage();
                send = true;
            }

            if (send) {
                SimpleMailMessage message =  mail.createMessage(settings.getNode(), subject, body);
                mail.send(message);
            }

            replicating.remove(s);
        }
    }

}