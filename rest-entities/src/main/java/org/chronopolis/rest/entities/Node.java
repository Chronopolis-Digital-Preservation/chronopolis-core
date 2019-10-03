package org.chronopolis.rest.entities;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * Representation of a {@link Node} in Chronopolis
 *
 * The {@link Node#username} acts as a unique identifier and is used for equality checks
 *
 * todo: remove password and link to users
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Node extends PersistableEntity {

    /**
     * The name of the {@link Node}
     */
    @NaturalId
    @EqualsAndHashCode.Include
    private String username;

    /**
     * Vestigial column; no longer used as users log in instead of nodes
     */
    private String password;

    /**
     * Flag to mark if the {@link Node} should receive {@link Replication}s
     */
    private Boolean enabled;

    /**
     * The {@link Replication}s for a given {@link Node}
     *
     * todo: this probably isn't needed here
     */
    @OneToMany(mappedBy = "node")
    private Set<Replication> replications;

    public Node(Set<Replication> replications, String username, String password, Boolean enabled) {
        this.replications = replications;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("username", getUsername())
                .add("enabled", getEnabled())
                .toString();
    }

}
