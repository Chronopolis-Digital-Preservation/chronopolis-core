package org.chronopolis.rest.entities.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.BagDistributionStatus;
import org.chronopolis.rest.entities.BagFile;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.models.StagingStorage;

import java.io.IOException;
import java.util.stream.Collectors;

public class BagSerializer extends JsonSerializer<Bag> {

    @Override
    public void serialize(Bag bag,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(modelFor(bag));
    }

    public org.chronopolis.rest.models.Bag modelFor(Bag bag) {
        // First create Staging Storage for each of the potential Bag and Token staging values
        // not really ideal but this is what we got
        StagingStorage bagStorage = null;
        StagingStorage tokenStorage = null;
        StagingStorageSerializer storageSerializer = new StagingStorageSerializer();

        for (org.chronopolis.rest.entities.storage.StagingStorage storage : bag.getStorage()) {
            DataFile file = storage.getFile();
            StagingStorage fileStorage = storageSerializer.modelOf(storage);

            if (file instanceof BagFile) {
                bagStorage = fileStorage;
            } else {
                tokenStorage = fileStorage;
            }
        }

        // then create the model for the API to return
        return new org.chronopolis.rest.models.Bag(
                bag.getId(),
                bag.getSize(),
                bag.getTotalFiles(),
                bagStorage,
                tokenStorage,
                bag.getCreatedAt(),
                bag.getUpdatedAt(),
                bag.getName(),
                bag.getCreator(),
                bag.getDepositor().getNamespace(),
                bag.getStatus(),
                bag.getDistributions().stream()
                        .filter(bd -> bd.getStatus() == BagDistributionStatus.REPLICATE)
                        .map(bd -> bd.getNode().getUsername())
                        .collect(Collectors.toSet())
        );
    }

}
