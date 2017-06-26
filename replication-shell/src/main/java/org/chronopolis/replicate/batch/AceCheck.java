package org.chronopolis.replicate.batch;

import org.chronopolis.common.ace.AceService;
import org.chronopolis.common.ace.GsonCollection;
import org.chronopolis.replicate.batch.callback.UpdateCallback;
import org.chronopolis.rest.api.IngestAPI;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.RStatusUpdate;
import org.chronopolis.rest.models.Replication;
import org.chronopolis.rest.models.ReplicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

/**
 * fish fish fish fish fish
 * eating fish
 *
 * Created by shake on 10/13/16.
 */
public class AceCheck implements Supplier<ReplicationStatus> {
    private final Logger log = LoggerFactory.getLogger("ace-log");

    final AceService ace;
    final IngestAPI ingest;
    final Replication replication;

    public AceCheck(AceService ace, IngestAPI ingest, Replication replication) {
        this.ace = ace;
        this.ingest = ingest;
        this.replication = replication;
    }

    @Override
    public ReplicationStatus get() {
        Bag bag = replication.getBag();
        GetCallback callback = new GetCallback();
        Call<GsonCollection> call = ace.getCollectionByName(bag.getName(), bag.getDepositor());
        call.enqueue(callback);
        Optional<GsonCollection> collection = callback.get();
        return collection.map(this::checkCollection)
                .orElse(ReplicationStatus.ACE_AUDITING);
    }

    private ReplicationStatus checkCollection(GsonCollection gsonCollection) {
        ReplicationStatus current = ReplicationStatus.ACE_AUDITING;
        log.debug("{} status is {}", gsonCollection.getName(), gsonCollection.getState());

        // TODO: Check for errors as well
        if (gsonCollection.getState().equals("A")) {
            current = ReplicationStatus.SUCCESS;
            Call<Replication> call = ingest.updateReplicationStatus(replication.getId(), new RStatusUpdate(current));
            call.enqueue(new UpdateCallback());
        }

        return current;
    }

    private void sendSuccess(Replication replication) {
        String subject = "Successful replication of" + replication.getBag().getName();
        // SimpleMailMessage message = mail.createMessage(settings.getNode(), subject, body);
        // mail.send(message);
    }

    class GetCallback implements Callback<GsonCollection> {

        GsonCollection collection;
        CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void onResponse(Call<GsonCollection> call, Response<GsonCollection> response) {
            if (response.isSuccessful()) {
                collection = response.body();
            } else {
                collection = null;
            }
            latch.countDown();
        }

        @Override
        public void onFailure(Call<GsonCollection> call, Throwable t) {
            collection = null;
            latch.countDown();
        }

        public Optional<GsonCollection> get() {
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }

            return Optional.ofNullable(collection);
        }
    }
}
