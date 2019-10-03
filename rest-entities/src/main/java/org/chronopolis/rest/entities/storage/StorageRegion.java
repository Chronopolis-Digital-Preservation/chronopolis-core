package org.chronopolis.rest.entities.storage;

import com.google.common.base.MoreObjects;
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
 * A {@link StorageRegion} is a filesystem at a {@link Node} where data can be staged for
 * replication. Currently only {@link StorageType#LOCAL} filesystems are supported.
 *
 * As there are two types of data which Chronopolis knows about, there are two types offered:
 * {@link DataType#BAG} and {@link DataType#TOKEN}.
 *
 * tbd determine equality
 *
 * @author shake
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class StorageRegion extends UpdatableEntity {

    /**
     * Extra information provided for the {@link StorageRegion}
     */
    private String note;

    /**
     * The total capacity of the {@link StorageRegion} in bytes
     */
    private Long capacity;

    /**
     * The {@link DataType} of the data expected in the {@link StorageRegion}
     */
    @Enumerated(value = STRING) private DataType dataType = DataType.BAG;
    @Enumerated(value = STRING) private StorageType storageType = StorageType.LOCAL;

    @ManyToOne private Node node;
    @OneToMany(mappedBy = "region", fetch = LAZY) private Set<StagingStorage> storage;
    @OneToOne(mappedBy = "region", cascade = ALL) private ReplicationConfig replicationConfig;

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("nodeId", getNode().getId())
                .add("dataType", getDataType())
                .add("storageType", getStorageType())
                .toString();
    }

}
