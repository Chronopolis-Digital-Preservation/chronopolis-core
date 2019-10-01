package org.chronopolis.rest.entities.storage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.chronopolis.rest.entities.PersistableEntity;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * tbd: determine how equality checks should be handled. currently just on the primary key.
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ReplicationConfig extends PersistableEntity {

    @NonNull
    @OneToOne
    private StorageRegion region;

    @NonNull private String path;
    @NonNull private String server;
    @NonNull private String username;

}