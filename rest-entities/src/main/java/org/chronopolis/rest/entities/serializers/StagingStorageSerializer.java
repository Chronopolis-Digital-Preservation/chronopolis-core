package org.chronopolis.rest.entities.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.models.Fixity;
import org.chronopolis.rest.models.enums.FixityAlgorithm;

import java.io.IOException;
import java.util.stream.Collectors;

public class StagingStorageSerializer extends JsonSerializer<StagingStorage> {

    @Override
    public void serialize(StagingStorage stagingStorage,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(modelOf(stagingStorage));
    }

    public org.chronopolis.rest.models.StagingStorage modelOf(StagingStorage storage) {
        return new org.chronopolis.rest.models.StagingStorage(
                storage.getActive(),
                storage.getSize(),
                storage.getRegion().getId(),
                storage.getTotalFiles(),
                storage.getPath(),
                storage.getFile().getFixities().stream().map(fixity ->
                        new Fixity(
                                fixity.getValue(),
                                algorithmFromString(fixity.getAlgorithm()),
                                fixity.getCreatedAt()))
                        .collect(Collectors.toSet())
        );
    }

    private FixityAlgorithm algorithmFromString(String algorithm) {
        switch (algorithm.toLowerCase()) {
            case "sha256":
            case "sha-256":
            case "sha_256":
                return FixityAlgorithm.SHA_256;
            default:
                return FixityAlgorithm.UNSUPPORTED;
        }
    }
}
