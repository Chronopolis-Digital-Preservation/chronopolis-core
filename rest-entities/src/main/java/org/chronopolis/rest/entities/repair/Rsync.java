package org.chronopolis.rest.entities.repair;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Rsync extends Strategy {

    @NonNull private String link;
    @OneToOne(mappedBy = "strategy") private Repair repair;

    @Override
    public FulfillmentStrategy model() {
        return null;
    }
}
