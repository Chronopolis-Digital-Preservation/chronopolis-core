package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.depositor.Depositor;

import java.io.IOException;

public class DepositorSerializer extends JsonSerializer<Depositor> {

    @Override
    public void serialize(Depositor depositor, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
