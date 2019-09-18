package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.depositor.DepositorContact;

import java.io.IOException;

public class DepositorContactSerializer extends JsonSerializer<DepositorContact> {

    @Override
    public void serialize(DepositorContact depositorContact, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
