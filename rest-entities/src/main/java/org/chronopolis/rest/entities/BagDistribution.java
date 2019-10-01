package org.chronopolis.rest.entities;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BagDistribution extends PersistableEntity {

    @NonNull
    @EqualsAndHashCode.Include
    @ManyToOne(fetch = FetchType.LAZY)
    private Bag bag;

    @NonNull
    @EqualsAndHashCode.Include
    @ManyToOne(fetch = FetchType.LAZY)
    private Node node;

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
