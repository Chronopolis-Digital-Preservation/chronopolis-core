package org.chronopolis.rest.entities.repair;

import lombok.Data;
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
 * @author shake
 */
@Data
@Entity
public class Repair extends UpdatableEntity {

    private String requester;
    private Boolean cleaned = false;
    private Boolean replaced = false;
    private Boolean validated = false;

    @ManyToOne private Bag bag;
    @ManyToOne @JoinColumn(name = "to_node") private Node to;
    @ManyToOne @JoinColumn(name = "from_node") private Node from;
    @OneToOne(cascade = MERGE, fetch = EAGER) private Strategy strategy;
    @OneToMany(mappedBy = "repair", cascade = ALL, fetch = EAGER) private Set<RepairFile> files;

    @Enumerated(value = STRING) private RepairStatus status = RepairStatus.REQUESTED;
    @Enumerated(value = STRING) private AuditStatus auditStatus = AuditStatus.PRE;
    @Enumerated(value = STRING) private FulfillmentType type;

    public void addFilesFromRequest(Set<String> filesToAdd) {
        filesToAdd.forEach(file -> files.add(new RepairFile(this, file)));
    }

}
