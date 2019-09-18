package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.Replication;

import java.io.IOException;

public class ReplicationSerializer extends JsonSerializer<Replication> {

    @Override
    public void serialize(Replication replication, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
