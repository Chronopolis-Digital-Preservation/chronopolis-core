package org.chronopolis.rest.lombok.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.depositor.Depositor;
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

        org.chronopolis.rest.models.Depositor model = new org.chronopolis.rest.models.Depositor(
                depositor.getId(),
                depositor.getNamespace(),
                depositor.getSourceOrganization(),
                depositor.getOrganizationAddress(),
                depositor.getCreatedAt(),
                depositor.getUpdatedAt(),
                null,
                contacts
        );

        jsonGenerator.writeObject(model);
    }
}
