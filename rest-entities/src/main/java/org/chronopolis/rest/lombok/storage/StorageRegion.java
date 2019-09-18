package org.chronopolis.rest.lombok.storage;

import lombok.Data;
import org.chronopolis.rest.lombok.Node;
import org.chronopolis.rest.lombok.UpdatableEntity;
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
public class StorageRegion extends UpdatableEntity {

    private String note;
    private Long capacity;

    @Enumerated(value = STRING) private DataType dataType = DataType.BAG;
    @Enumerated(value = STRING) private StorageType storageType = StorageType.LOCAL;

    @ManyToOne private Node node;
    @OneToMany(mappedBy = "region", fetch = LAZY) private Set<StagingStorage> storage;
    @OneToOne(mappedBy = "region", cascade = ALL) private ReplicationConfig replicationConfig;


}
