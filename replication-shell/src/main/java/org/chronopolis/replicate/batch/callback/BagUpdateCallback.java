package org.chronopolis.replicate.batch.callback;

import org.chronopolis.rest.models.Bag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

/**
 * Reusable callback for status updates to collections
 */
public class BagUpdateCallback implements Callback<Bag> {
    private final Logger log = LoggerFactory.getLogger(BagUpdateCallback.class);

    @Override
    public void onResponse(@NotNull Call<Bag> call,
                           @NotNull Response<Bag> response) {
        if (response.isSuccessful()) {
            log.info("Successfully updated collection {}", response.body().getId());
        } else {
            log.warn("Error updating collection: {} - {}", response.code(), response.message());
            try {
                log.warn("{}", response.errorBody().string());
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void onFailure(@NotNull Call<Bag> call, @NotNull Throwable throwable) {
        log.error("Error communicating with Ingest Server", throwable);
    }
}
