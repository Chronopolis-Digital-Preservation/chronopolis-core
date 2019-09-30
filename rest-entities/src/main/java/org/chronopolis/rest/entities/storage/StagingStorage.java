package org.chronopolis.rest.entities.storage;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.entities.UpdatableEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
public class StagingStorage extends UpdatableEntity {

    private Long size;
    private Long totalFiles;
    private String path;
    private Boolean active;

    @ManyToOne private Bag bag;
    @ManyToOne private DataFile file;
    @ManyToOne private StorageRegion region;

    public StagingStorage(StorageRegion region,
                          Bag bag,
                          Long size,
                          Long totalFiles,
                          String path,
                          Boolean active) {
        this.region = region;
        this.bag = bag;
        this.size = size;
        this.totalFiles = totalFiles;
        this.path = path;
        this.active = active;
    }

    public Boolean isActive() {
        return getActive();
    }

}
