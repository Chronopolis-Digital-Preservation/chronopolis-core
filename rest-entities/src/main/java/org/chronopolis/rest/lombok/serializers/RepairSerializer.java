package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.repair.Repair;

import java.io.IOException;

public class RepairSerializer extends JsonSerializer<Repair> {

    @Override
    public void serialize(Repair repair, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
