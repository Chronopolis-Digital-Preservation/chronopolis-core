package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.storage.StorageRegion;
import org.chronopolis.rest.models.ReplicationConfig;

import java.io.IOException;

public class StorageRegionSerializer extends JsonSerializer<StorageRegion> {

    @Override
    public void serialize(StorageRegion storageRegion,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        ReplicationConfig configModel = new ReplicationConfig(
                storageRegion.getId(),
                storageRegion.getReplicationConfig().getPath(),
                storageRegion.getReplicationConfig().getServer(),
                storageRegion.getReplicationConfig().getUsername()
        );

        org.chronopolis.rest.models.StorageRegion model =
                new org.chronopolis.rest.models.StorageRegion(
                        storageRegion.getId(),
                        storageRegion.getNode().getUsername(),
                        storageRegion.getNote(),
                        storageRegion.getCapacity(),
                        storageRegion.getDataType(),
                        storageRegion.getStorageType(),
                        configModel
                );

        jsonGenerator.writeObject(model);
    }
}
