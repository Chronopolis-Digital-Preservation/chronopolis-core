package org.chronopolis.rest.entities;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.chronopolis.rest.entities.storage.Fixity;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.models.enums.ReplicationStatus;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.PreUpdate;

/**
 * A resource which tracks distribution of a {@link Bag} to a {@link Node}
 *
 * If the {@link Replication#receivedTagFixity} does not match the {@link Fixity} from the
 * {@link StagingStorage#file} (on the BAG {@link StagingStorage}), the {@link Replication#status}
 * should be set to {@link ReplicationStatus#FAILURE_TAG_MANIFEST}.
 *
 * Likewise if the {@link Replication#receivedTokenFixity} does not match on the TOKEN_STORE
 * {@link StagingStorage}, the {@link Replication#status} should be set to
 * {@link ReplicationStatus#FAILURE_TOKEN_STORE}.
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Replication extends UpdatableEntity {

    /**
     * The {@link ReplicationStatus} showing the current state of the {@link Replication}
     */
    @NonNull
    @Enumerated(EnumType.STRING)
    private ReplicationStatus status = ReplicationStatus.PENDING;

    /**
     * The {@link Node} which is receiving the {@link Bag}
     */
    @NonNull
    @ManyToOne
    private Node node;

    /**
     * The {@link Bag} which is being distributed
     */
    @NonNull
    @ManyToOne(cascade = CascadeType.MERGE)
    private Bag bag;

    /**
     * The uri used to transfer the contents of the {@link Bag}
     */
    @NonNull private String bagLink;

    /**
     * The uri used to transfer the contents of the {@link TokenStore}
     */
    @NonNull private String tokenLink;

    /**
     * The protocol used for transferring data.
     * Note: currently this is only rsync
     */
    @NonNull private String protocol;

    /**
     * The computed fixity (hash) of the tagmanifest associated with the {@link Bag}
     */
    private String receivedTagFixity;

    /**
     * The computed fixity (hash) of the {@link TokenStore} associated with the {@link Bag}
     */
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
            // ehhhhhh
            bag.getDistributions().stream()
                .filter(dist -> dist.getNode().equals(node))
                .findFirst().ifPresent(dist -> dist.setStatus(BagDistributionStatus.REPLICATE));

            bag.onUpdate();
        }
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("bagId", getBag().getId())
                .add("nodeId", getNode().getId())
                .add("status", getStatus())
                .toString();
    }

}
