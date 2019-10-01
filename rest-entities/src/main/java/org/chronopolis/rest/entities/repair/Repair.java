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
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Repair extends UpdatableEntity {

    private String requester;
    private Boolean cleaned = false;
    private Boolean replaced = false;
    private Boolean validated = false;

    @Enumerated(value = STRING) private FulfillmentType type;
    @Enumerated(value = STRING) private AuditStatus audit = AuditStatus.PRE;
    @Enumerated(value = STRING) private RepairStatus status = RepairStatus.REQUESTED;

    @ManyToOne private Bag bag;
    @OneToOne(cascade = MERGE, fetch = EAGER) private Strategy strategy;
    @OneToMany(mappedBy = "repair", cascade = ALL, fetch = EAGER) private Set<RepairFile> files;

    @ManyToOne
    @JoinColumn(name = "to_node")
    private Node to;

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
