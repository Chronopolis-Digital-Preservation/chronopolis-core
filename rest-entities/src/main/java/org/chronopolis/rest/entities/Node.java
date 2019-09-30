package org.chronopolis.rest.entities;

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
@EqualsAndHashCode(callSuper = true)
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

}
