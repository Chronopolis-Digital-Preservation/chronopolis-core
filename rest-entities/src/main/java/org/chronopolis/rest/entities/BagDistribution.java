package org.chronopolis.rest.entities;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.chronopolis.rest.entities.repair.Repair;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Tracking for where {@link Bag}s should be distributed to. In each {@link Bag}, a {@link Node}
 * will receive a {@link BagDistributionStatus} indicating the status of the {@link Bag} at the
 * {@link Node}.
 *
 * If the {@link BagDistributionStatus} is {@link BagDistributionStatus#DISTRIBUTE}, the
 * {@link Node} needs to transfer the contents of the {@link Bag}.
 *
 * If the {@link BagDistributionStatus} is {@link BagDistributionStatus#REPLICATE}, the {@link Node}
 * has successfully transferred the {@link Bag} into its preservation storage.
 *
 * If the {@link BagDistributionStatus} is {@link BagDistributionStatus#DEGRADED}, the {@link Node}
 * will need to create a {@link Repair} in order to heal the data in its preservation storage.
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BagDistribution extends PersistableEntity {

    /**
     * The {@link Bag} this {@link BagDistribution} is for
     */
    @NonNull
    @EqualsAndHashCode.Include
    @ManyToOne(fetch = FetchType.LAZY)
    private Bag bag;

    /**
     * The {@link Node} this {@link BagDistribution} is for
     */
    @NonNull
    @EqualsAndHashCode.Include
    @ManyToOne(fetch = FetchType.LAZY)
    private Node node;

    /**
     * The {@link BagDistributionStatus} of this {@link BagDistribution}
     */
    @NonNull
    @Enumerated(value = EnumType.STRING)
    private BagDistributionStatus status;

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bagId", getBag().getId())
                .add("nodeId", getNode().getId())
                .add("status", getStatus())
                .toString();
    }

}
