package org.chronopolis.rest.lombok;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * @author shake
 */
@Data
@Entity
public class Node extends PersistableEntity {

    @NaturalId
    @EqualsAndHashCode.Include
    private String username;

    private String password;
    private Boolean enabled;

    @OneToMany(mappedBy = "node")
    private Set<Replication> replications;

}
