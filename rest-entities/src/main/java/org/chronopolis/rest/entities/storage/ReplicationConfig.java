package org.chronopolis.rest.entities.storage;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.PersistableEntity;
import org.chronopolis.rest.entities.Replication;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Configuration for {@link StorageRegion}s so that the ingest server can create
 * {@link Replication}s for {@link Bag}s which are staged.
 *
 * For an rsync, the final link should look something like
 * {@code username}@{@code server}/{@code path}
 *
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

    /**
     * The path on disk where replications will occur from
     */
    @NonNull private String path;

    /**
     * The fqdn of the server
     */
    @NonNull private String server;

    /**
     * The user which a {@link Node} should connect as
     */
    @NonNull private String username;

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("regionId", getRegion().getId())
                .add("path", getPath())
                .add("server", getServer())
                .add("username", getUsername())
                .toString();
    }

}