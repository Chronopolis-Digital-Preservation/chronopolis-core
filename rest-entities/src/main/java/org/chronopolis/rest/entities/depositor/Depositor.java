package org.chronopolis.rest.entities.depositor;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.UpdatableEntity;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link Depositor} is an actor who is the primary source for {@link Bag}s in Chronopolis
 *
 * They have a {@link Depositor#namespace} which acts as a unique identifier and is used when
 * checking equality
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Depositor extends UpdatableEntity implements Comparable<Depositor> {

    /**
     * The name of the {@link} Depositor; must be unique
     */
    @NaturalId
    @EqualsAndHashCode.Include
    private String namespace;

    /**
     * The source organization the {@link Depositor} belongs to; can be null
     */
    private String sourceOrganization;

    /**
     * The address of the source organization; can be null
     */
    private String organizationAddress;

    /**
     * The {@link DepositorContact}s who can be used to contact the {@link Depositor}
     */
    @OneToMany(mappedBy = "depositor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DepositorContact> contacts = new HashSet<>();

    /**
     * The {@link Node}s which will receive data for this {@link Depositor}
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "depositor_distribution",
            joinColumns = @JoinColumn(name = "depositor_id"),
            inverseJoinColumns = @JoinColumn(name = "node_id"))
    private Set<Node> nodeDistributions = new HashSet<>();

    public Depositor(String namespace, String sourceOrganization, String organizationAddress) {
        this.namespace = namespace;
        this.sourceOrganization = sourceOrganization;
        this.organizationAddress = organizationAddress;
    }

    public void addContact(DepositorContact contact) {
        if (contact != null && contact.getDepositor() == null) {
            contact.setDepositor(this);
            contacts.add(contact);
        }
    }

    public void removeContact(DepositorContact contact) {
        if (contact != null &&
                // Make sure we are removing for our contact
                (contact.getDepositor() == null || contact.getDepositor().equals(this))) {
            contact.setDepositor(null);
            contacts.remove(contact);
        }
    }

    public void addNodeDistribution(Node node) {
        if (node != null) {
            nodeDistributions.add(node);
        }
    }

    public void removeNodeDistribution(Node node) {
        if (node != null) {
            nodeDistributions.remove(node);
        }
    }

    @Override
    public int compareTo(@NotNull Depositor depositor) {
        return namespace.compareTo(depositor.namespace);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("namespace", getNamespace())
                .toString();
    }

}
