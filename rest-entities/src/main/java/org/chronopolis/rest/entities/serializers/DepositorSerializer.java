package org.chronopolis.rest.entities.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.depositor.Depositor;
import org.chronopolis.rest.models.DepositorContact;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class DepositorSerializer extends JsonSerializer<Depositor> {

    @Override
    public void serialize(Depositor depositor,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        Set<DepositorContact> contacts = depositor.getContacts()
                .stream()
                .map(contact -> new DepositorContact(
                        contact.getContactName(),
                        contact.getContactEmail(),
                        contact.getContactPhone()
                )).collect(Collectors.toSet());

        Set<String> replicatingNodes = depositor.getNodeDistributions()
                .stream()
                .map(Node::getUsername)
                .collect(Collectors.toSet());

        org.chronopolis.rest.models.Depositor model = new org.chronopolis.rest.models.Depositor(
                depositor.getId(),
                depositor.getNamespace(),
                depositor.getSourceOrganization(),
                depositor.getOrganizationAddress(),
                depositor.getCreatedAt(),
                depositor.getUpdatedAt(),
                replicatingNodes,
                contacts
        );

        jsonGenerator.writeObject(model);
    }
}
