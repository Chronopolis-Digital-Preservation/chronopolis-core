package org.chronopolis.rest.entities.storage;

import lombok.Data;
import org.chronopolis.rest.entities.PersistableEntity;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * @author shake
 */
@Data
@Entity
public class ReplicationConfig extends PersistableEntity {

    private String path;
    private String server;
    private String username;

    @OneToOne
    private StorageRegion region;

}