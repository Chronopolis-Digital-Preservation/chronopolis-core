package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.projections.CompleteBag;
import org.chronopolis.rest.lombok.projections.StagingView;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.StagingStorage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

public class CompleteBagSerializer extends JsonSerializer<CompleteBag> {

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

            if (entry.getKey().equalsIgnoreCase("BAG")) {
                bagStorage = storage;
            } else {
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
