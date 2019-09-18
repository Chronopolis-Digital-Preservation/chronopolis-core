package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.projections.PartialBag;

import java.io.IOException;

public class PartialBagSerializer extends JsonSerializer<PartialBag> {

    @Override
    public void serialize(PartialBag partialBag, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
