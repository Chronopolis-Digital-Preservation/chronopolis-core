package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.projections.CompleteBag;

import java.io.IOException;

public class CompleteBagSerializer extends JsonSerializer<CompleteBag> {

    @Override
    public void serialize(CompleteBag completeBag, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
