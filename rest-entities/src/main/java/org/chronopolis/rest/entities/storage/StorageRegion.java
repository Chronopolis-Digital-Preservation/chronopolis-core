package org.chronopolis.rest.entities.storage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.UpdatableEntity;
import org.chronopolis.rest.models.enums.DataType;
import org.chronopolis.rest.models.enums.StorageType;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

/**
 * @author shake
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class StorageRegion extends UpdatableEntity {

    private String note;
    private Long capacity;

    @Enumerated(value = STRING) private DataType dataType = DataType.BAG;
    @Enumerated(value = STRING) private StorageType storageType = StorageType.LOCAL;

    @ManyToOne private Node node;
    @OneToMany(mappedBy = "region", fetch = LAZY) private Set<StagingStorage> storage;
    @OneToOne(mappedBy = "region", cascade = ALL) private ReplicationConfig replicationConfig;


}
