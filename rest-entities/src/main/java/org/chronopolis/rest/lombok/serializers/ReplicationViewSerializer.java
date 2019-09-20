package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.projections.ReplicationView;
import org.chronopolis.rest.models.Replication;

import java.io.IOException;

public class ReplicationViewSerializer extends JsonSerializer<ReplicationView> {

    @Override
    public void serialize(ReplicationView replicationView,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        CompleteBagSerializer completeBagSerializer = new CompleteBagSerializer();
        Replication model = new Replication(
                replicationView.getId(),
                replicationView.getCreatedAt(),
                replicationView.getUpdatedAt(),
                replicationView.getStatus(),
                replicationView.getBagLink(),
                replicationView.getTokenLink(),
                replicationView.getProtocol(),
                replicationView.getReceivedTagFixity(),
                replicationView.getReceivedTokenFixity(),
                replicationView.getNode(),
                completeBagSerializer.modelFor(replicationView.getBag())
        );

        jsonGenerator.writeObject(model);
    }
}
