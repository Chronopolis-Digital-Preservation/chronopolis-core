package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.projections.ReplicationView;

import java.io.IOException;

public class ReplicationViewSerializer extends JsonSerializer<ReplicationView> {

    @Override
    public void serialize(ReplicationView replicationView, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
