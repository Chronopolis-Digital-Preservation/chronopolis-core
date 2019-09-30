package org.chronopolis.rest.entities.repair;

import lombok.Data;
import org.chronopolis.rest.models.FulfillmentStrategy;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * @author shake
 */
@Data
@Entity
@DiscriminatorValue("ACE")
public class Ace extends Strategy {
    private String url;
    private String apiKey;
    @OneToOne private Repair repair;

    @Override
    public FulfillmentStrategy model() {
        return null;
    }
}
