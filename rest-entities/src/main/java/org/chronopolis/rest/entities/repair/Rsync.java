package org.chronopolis.rest.entities.repair;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.models.FulfillmentStrategy;
import org.chronopolis.rest.models.RsyncStrategy;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * {@link Strategy} which uses rsync as the primary mechanism for transferring files
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@DiscriminatorValue("RSYNC")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Rsync extends Strategy {

    /**
     * The uri for the rsync
     */
    private String link;

    @OneToOne(mappedBy = "strategy")
    private Repair repair;

    public Rsync(String link) {
        this.link = link;
    }

    @Override
    public FulfillmentStrategy model() {
        return new RsyncStrategy(link);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("repairId", getRepair().getId())
                .add("link", getLink())
                .toString();
    }

}
