package org.chronopolis.rest.entities.repair;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.UpdatableEntity;
import org.chronopolis.rest.models.enums.AuditStatus;
import org.chronopolis.rest.models.enums.FulfillmentType;
import org.chronopolis.rest.models.enums.RepairStatus;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;

/**
 * A {@link Repair} is an operation in Chronopolis for healing corrupt data at a {@link Node}. It
 * goes through multiple state changes, including {@link RepairStatus#REQUESTED} where a replicating
 * {@link Node} acknowledges they can provide the data being repaired (fulfillment).
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Repair extends UpdatableEntity {

    /**
     * The user who made the request for the {@link Repair}
     */
    private String requester;

    /**
     * Flag to show if the {@link RepairFile}s have been cleaned from staging
     */
    private Boolean cleaned = false;

    /**
     * Flag to show if the {@link RepairFile}s have replaced the corrupt data
     */
    private Boolean replaced = false;

    /**
     * Flag to show if the {@link RepairFile}s have been validated by {@link Repair#to}
     */
    private Boolean validated = false;

    /**
     * The type of {@link Strategy} the {@link Repair#from} node will be using to allow the
     * {@link Repair#to} node to transfer the {@link RepairFile}s; can be null
     */
    @Enumerated(value = STRING) private FulfillmentType type;

    /**
     * The status of the ACE-AM audit at the {@link Repair#to} node
     */
    @Enumerated(value = STRING) private AuditStatus audit = AuditStatus.PRE;

    /**
     * The status of the {@link Repair}
     */
    @Enumerated(value = STRING) private RepairStatus status = RepairStatus.REQUESTED;

    /**
     * The {@link Bag} which contains the {@link RepairFile}s which are corrupt at {@link Repair#to}
     */
    @ManyToOne private Bag bag;

    /**
     * The {@link Strategy} being used to transfer data; can be null
     */
    @OneToOne(cascade = MERGE, fetch = EAGER) private Strategy strategy;

    /**
     * The {@link RepairFile}s which are corrupt at the {@link Repair#to} node
     */
    @OneToMany(mappedBy = "repair", cascade = ALL, fetch = EAGER) private Set<RepairFile> files;

    /**
     * The {@link Node} which has corrupt files
     */
    @ManyToOne
    @JoinColumn(name = "to_node")
    private Node to;

    /**
     * The {@link Node} which is offering to fulfill the {@link Repair}; can be null
     */
    @ManyToOne
    @JoinColumn(name = "from_node")
    private Node from;

    // Constructor for compatibility

    public Repair(Bag bag,
                  Node to,
                  Node from,
                  RepairStatus status,
                  AuditStatus audit,
                  FulfillmentType type,
                  Strategy strategy,
                  String requester,
                  Boolean cleaned,
                  Boolean replaced,
                  Boolean validated) {
        this.bag = bag;
        this.to = to;
        this.from = from;
        this.status = status;
        this.audit = audit;
        this.type = type;
        this.strategy = strategy;
        this.requester = requester;
        this.cleaned = cleaned;
        this.replaced = replaced;
        this.validated = validated;
    }

    public void addFilesFromRequest(Set<String> filesToAdd) {
        filesToAdd.forEach(file -> files.add(new RepairFile(this, file)));
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("bagId", getBag().getId())
                .toString();
    }

}
