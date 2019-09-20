package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.projections.PartialBag;
import org.chronopolis.rest.models.Bag;

import java.io.IOException;

public class PartialBagSerializer extends JsonSerializer<PartialBag> {

    @Override
    public void serialize(PartialBag partialBag,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        Bag model = new Bag(
                partialBag.getId(),
                partialBag.getSize(),
                partialBag.getTotalFiles(),
                // we don't pull storage info so both bag/token staging objects are null
                // todo: it would be nice to map to a partialbagmodel
                null, null,
                partialBag.getCreatedAt(),
                partialBag.getUpdatedAt(),
                partialBag.getName(),
                partialBag.getCreator(),
                partialBag.getDepositor(),
                partialBag.getStatus(),
                partialBag.getReplicatingNodes()
        );

        jsonGenerator.writeObject(model);
    }
}
