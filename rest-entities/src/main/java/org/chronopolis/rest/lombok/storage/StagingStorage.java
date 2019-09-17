package org.chronopolis.rest.lombok.storage;

import lombok.Data;
import org.chronopolis.rest.lombok.Bag;
import org.chronopolis.rest.lombok.DataFile;
import org.chronopolis.rest.lombok.UpdatableEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author shake
 */
@Data
@Entity
public class StagingStorage extends UpdatableEntity {

    private Long size;
    private Long totalFiles;
    private String path;
    private Boolean active;

    @ManyToOne private Bag bag;
    @ManyToOne private DataFile file;
    @ManyToOne private StorageRegion region;

    public Boolean isActive() {
        return getActive();
    }

}
