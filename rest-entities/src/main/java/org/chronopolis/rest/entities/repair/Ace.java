package org.chronopolis.rest.entities.repair;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.models.FulfillmentStrategy;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@DiscriminatorValue("ACE")
@EqualsAndHashCode(callSuper = false)
public class Ace extends Strategy {
    private String url;
    private String apiKey;
    @OneToOne(mappedBy = "strategy") private Repair repair;

    public Ace(String apiKey, String url) {
        this.url = url;
        this.apiKey = apiKey;
    }

    @Override
    public FulfillmentStrategy model() {
        return null;
    }
}
