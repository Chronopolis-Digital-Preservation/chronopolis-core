package org.chronopolis.rest.entities.depositor;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Depositor extends UpdatableEntity implements Comparable<Depositor> {

    @NaturalId
    @EqualsAndHashCode.Include
    private String namespace;

    private String sourceOrganization;
    private String organizationAddress;

    @OneToMany(mappedBy = "depositor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DepositorContact> contacts = new HashSet<>();

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
