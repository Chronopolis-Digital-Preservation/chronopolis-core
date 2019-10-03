package org.chronopolis.rest.entities.storage;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.entities.Replication;
import org.chronopolis.rest.entities.TokenStore;
import org.chronopolis.rest.entities.UpdatableEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Staging information for a {@link Bag} or {@link TokenStore}
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class StagingStorage extends UpdatableEntity {

    /**
     * The size on disk of the staged data, in bytes
     */
    @EqualsAndHashCode.Include private Long size;

    /**
     * The number of files staged
     */
    @EqualsAndHashCode.Include private Long totalFiles;

    /**
     * The path to the staged data, relative to the {@link StorageRegion}
     */
    @EqualsAndHashCode.Include private String path;

    /**
     * A flag determining if the data is still available
     */
    @EqualsAndHashCode.Include private Boolean active;

    @ManyToOne private Bag bag;
    @ManyToOne private StorageRegion region;

    /**
     * File used for validation of transfer. A {@link Replication} should match one of the
     * {@link DataFile#fixities} in the file.
     */
    @ManyToOne
    @JoinColumn(name = "file_id")
    @EqualsAndHashCode.Include
    private DataFile file;

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
