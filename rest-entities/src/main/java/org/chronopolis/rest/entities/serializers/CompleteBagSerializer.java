package org.chronopolis.rest.entities.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.entities.projections.CompleteBag;
import org.chronopolis.rest.entities.projections.StagingView;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.StagingStorage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

public class CompleteBagSerializer extends JsonSerializer<CompleteBag> {
    private final String BAG_FILE = "BAG";
    private final String TOKEN_FILE = "TOKEN_STORE";

    @Override
    public void serialize(CompleteBag completeBag,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(modelFor(completeBag));
    }

    public Bag modelFor(CompleteBag completeBag) {
        StagingStorage bagStorage = null;
        StagingStorage tokenStorage = null;
        for (Map.Entry<String, StagingView> entry : completeBag.getStorage().entrySet()) {
            StagingStorage storage = new StagingStorage(
                    entry.getValue().getActive(),
                    entry.getValue().getTotalFiles(), // TODO: SIZE
                    entry.getValue().getRegion(),
                    entry.getValue().getTotalFiles(),
                    entry.getValue().getPath(),
                    new HashSet<>()
            );

            if (BAG_FILE.equalsIgnoreCase(entry.getKey())) {
                bagStorage = storage;
            } else if (TOKEN_FILE.equalsIgnoreCase(entry.getKey())) {
                tokenStorage = storage;
            }
        }

        return new Bag(
                completeBag.getId(),
                completeBag.getSize(),
                completeBag.getTotalFiles(),
                bagStorage,
                tokenStorage,
                completeBag.getCreatedAt(),
                completeBag.getUpdatedAt(),
                completeBag.getName(),
                completeBag.getCreator(),
                completeBag.getDepositor(),
                completeBag.getStatus(),
                completeBag.getReplicatingNodes()
        );
    }

}
