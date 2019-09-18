package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.Bag;

import java.io.IOException;

public class BagSerializer extends JsonSerializer<Bag> {

    @Override
    public void serialize(Bag bag, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
