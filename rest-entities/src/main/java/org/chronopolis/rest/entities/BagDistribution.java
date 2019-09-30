package org.chronopolis.rest.entities;

import lombok.Data;
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
public class BagDistribution extends PersistableEntity {

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Bag bag;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Node node;

    @NonNull
    @Enumerated(value = EnumType.STRING)
    private BagDistributionStatus status;

}
