package org.chronopolis.rest.lombok.serializers;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.chronopolis.rest.lombok.AceToken;

import java.io.IOException;

public class AceTokenSerializer extends JsonSerializer<AceToken> {

    @Override
    public void serialize(AceToken aceToken,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        org.chronopolis.rest.models.AceToken model = new org.chronopolis.rest.models.AceToken(
                aceToken.getId(),
                aceToken.getBag().getId(),
                aceToken.getRound(),
                aceToken.getProof(),
                aceToken.getImsHost(),
                aceToken.getAlgorithm(),
                aceToken.getFile().getFilename(),
                aceToken.getImsService(),
                aceToken.formatDate()
        );

        jsonGenerator.writeObject(model);
    }
}