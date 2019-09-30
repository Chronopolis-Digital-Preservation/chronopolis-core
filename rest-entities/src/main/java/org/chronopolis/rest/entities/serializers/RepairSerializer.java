package org.chronopolis.rest.entities.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.entities.repair.Ace;
import org.chronopolis.rest.entities.repair.Repair;
import org.chronopolis.rest.entities.repair.RepairFile;
import org.chronopolis.rest.entities.repair.Rsync;
import org.chronopolis.rest.models.AceStrategy;
import org.chronopolis.rest.models.FulfillmentStrategy;
import org.chronopolis.rest.models.RsyncStrategy;
import org.chronopolis.rest.models.enums.FulfillmentType;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author shake
 */
public class RepairSerializer extends JsonSerializer<Repair> {

    @Override
    public void serialize(Repair repair,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        List<String> files = repair.getFiles().stream()
                .map(RepairFile::getPath)
                .collect(Collectors.toList());
        String from = repair.getFrom() == null ? null : repair.getFrom().getUsername();

        FulfillmentStrategy strategy = null;
        if (repair.getStrategy() != null) {
            FulfillmentType type = repair.getType();

            // this is the only supported type so maybe for now it's all we care about handling
            if (type == FulfillmentType.NODE_TO_NODE && repair.getStrategy() instanceof Rsync) {
                Rsync rsync = (Rsync) repair.getStrategy();
                strategy = new RsyncStrategy(rsync.getLink());
            } else if (type == FulfillmentType.ACE) {
                Ace ace = (Ace) repair.getStrategy();
                strategy = new AceStrategy(ace.getApiKey(), ace.getUrl());
            }
        }

        org.chronopolis.rest.models.Repair model = new org.chronopolis.rest.models.Repair(
                repair.getId(),
                repair.getCreatedAt(),
                repair.getUpdatedAt(),
                repair.getCleaned(),
                repair.getReplaced(),
                repair.getValidated(),
                repair.getAudit(),
                repair.getStatus(),
                repair.getTo().getUsername(),
                repair.getRequester(),
                repair.getBag().getDepositor().getNamespace(),
                repair.getBag().getName(),
                files,
                from,
                repair.getType(),
                strategy
        );

        jsonGenerator.writeObject(model);
    }
}
