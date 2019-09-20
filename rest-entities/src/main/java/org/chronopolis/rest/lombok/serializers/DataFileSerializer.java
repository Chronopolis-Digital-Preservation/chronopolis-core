package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.DataFile;
import org.chronopolis.rest.models.File;
import org.chronopolis.rest.models.Fixity;
import org.chronopolis.rest.models.enums.FixityAlgorithm;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class DataFileSerializer extends JsonSerializer<DataFile> {

    @Override
    public void serialize(DataFile dataFile,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        Set<Fixity> fixities = dataFile.getFixities().stream()
                .map(fixity -> new Fixity(fixity.getValue(),
                        FixityAlgorithm.valueOf(fixity.getAlgorithm()),
                        fixity.getCreatedAt()))
                .collect(Collectors.toSet());

        File model = new File(
                dataFile.getId(),
                dataFile.getFilename(),
                dataFile.getSize(),
                fixities,
                dataFile.getBag().getId(),
                dataFile.getCreatedAt(),
                dataFile.getUpdatedAt()
        );

        jsonGenerator.writeObject(model);
    }
}
