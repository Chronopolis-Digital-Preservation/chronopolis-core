package org.chronopolis.rest.lombok.repair;

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
@DiscriminatorValue("RSYNC")
public class Rsync extends Strategy {

    private String link;
    @OneToOne private Repair repair;

    @Override
    public FulfillmentStrategy model() {
        return null;
    }
}
