package org.chronopolis.rest.entities.storage;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.entities.UpdatableEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class StagingStorage extends UpdatableEntity {

    @EqualsAndHashCode.Include private Long size;
    @EqualsAndHashCode.Include private Long totalFiles;
    @EqualsAndHashCode.Include private String path;
    @EqualsAndHashCode.Include private Boolean active;

    @ManyToOne private Bag bag;
    @ManyToOne private StorageRegion region;
    @ManyToOne @EqualsAndHashCode.Include private DataFile file;

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

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("bagId", getBag().getId())
                .add("fileId", getFile().getId())
                .add("regionId", getRegion().getId())
                .add("path", getPath())
                .add("active", getActive())
                .toString();
    }

}
