package org.chronopolis.rest.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.chronopolis.rest.models.enums.ReplicationStatus;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.PreUpdate;

/**
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Replication extends UpdatableEntity {

    @NonNull
    @Enumerated(EnumType.STRING)
    private ReplicationStatus status = ReplicationStatus.PENDING;

    @NonNull
    @ManyToOne
    private Node node;

    @NonNull
    @ManyToOne(cascade = CascadeType.MERGE)
    private Bag bag;

    @NonNull private String bagLink;
    @NonNull private String tokenLink;
    @NonNull private String protocol;

    private String receivedTagFixity;
    private String receivedTokenFixity;

    public Replication(ReplicationStatus status,
                       Node node,
                       Bag bag,
                       String bagLink,
                       String tokenLink,
                       String protocol,
                       String receivedTagFixity,
                       String receivedTokenFixity) {
        this.status = status;
        this.node = node;
        this.bag = bag;
        this.bagLink = bagLink;
        this.tokenLink = tokenLink;
        this.protocol = protocol;
        this.receivedTagFixity = receivedTagFixity;
        this.receivedTokenFixity = receivedTokenFixity;
    }

    @PreUpdate
    protected void updateReplication() {
        if (status.isFailure()) {
            return;
        }

        if (status == ReplicationStatus.SUCCESS) {
            bag.onUpdate();
        }
    }

}
