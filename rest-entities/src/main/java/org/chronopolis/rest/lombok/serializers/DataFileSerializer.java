package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.DataFile;

import java.io.IOException;

public class DataFileSerializer extends JsonSerializer<DataFile> {

    @Override
    public void serialize(DataFile dataFile, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}
