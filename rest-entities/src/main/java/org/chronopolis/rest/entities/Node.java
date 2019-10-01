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
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Node extends PersistableEntity {

    @NaturalId
    @EqualsAndHashCode.Include
    private String username;

    private String password;
    private Boolean enabled;

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
