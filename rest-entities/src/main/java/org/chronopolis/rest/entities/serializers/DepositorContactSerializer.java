package org.chronopolis.rest.entities.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.entities.depositor.DepositorContact;

import java.io.IOException;

public class DepositorContactSerializer extends JsonSerializer<DepositorContact> {

    @Override
    public void serialize(DepositorContact depositorContact,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        org.chronopolis.rest.models.DepositorContact model =
                new org.chronopolis.rest.models.DepositorContact(
                        depositorContact.getContactName(),
                        depositorContact.getContactEmail(),
                        depositorContact.getContactPhone()
                );

        jsonGenerator.writeObject(model);
    }

}
