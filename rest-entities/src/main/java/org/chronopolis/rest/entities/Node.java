package org.chronopolis.rest.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Node extends PersistableEntity {

    @NaturalId
    @ToString.Include
    @EqualsAndHashCode.Include
    private String username;

    private String password;

    @ToString.Include
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
