package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.Replication;

import java.io.IOException;

/**
 *
 * @author shake
 */
public class ReplicationSerializer extends JsonSerializer<Replication> {

    @Override
    public void serialize(Replication replication,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        BagSerializer bagSerializer = new BagSerializer();
        org.chronopolis.rest.models.Replication model = new org.chronopolis.rest.models.Replication(
                replication.getId(),
                replication.getCreatedAt(),
                replication.getUpdatedAt(),
                replication.getStatus(),
                replication.getBagLink(),
                replication.getTokenLink(),
                replication.getProtocol(),
                replication.getReceivedTagFixity(),
                replication.getReceivedTokenFixity(),
                replication.getNode().getUsername(),
                bagSerializer.modelFor(replication.getBag())
        );

        jsonGenerator.writeObject(replication);
    }
}
