package org.chronopolis.rest.entities.depositor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.UpdatableEntity;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Depositor extends UpdatableEntity implements Comparable<Depositor> {

    @NonNull
    @NaturalId
    @EqualsAndHashCode.Include
    private String namespace;

    @NonNull private String sourceOrganization;
    @NonNull private String organizationAddress;

    @OneToMany(mappedBy = "depositor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DepositorContact> contacts = new HashSet<>();

    @JoinTable(name = "depositor_distribution",
            joinColumns = @JoinColumn(name = "depositor_id"),
            inverseJoinColumns = @JoinColumn(name = "node_id"))
    private Set<Node> nodeDistributions = new HashSet<>();

    public void addContact(DepositorContact contact) {
        if (contact.getDepositor() == null) {
            contact.setDepositor(this);
            contacts.add(contact);
        }
    }

    public void removeContact(DepositorContact contact) {
        if (contact != null && contact.getDepositor().equals(this)) {
            contact.setDepositor(null);
            contacts.remove(contact);
        }
    }

    public void addNodeDistribution(Node node) {
        nodeDistributions.add(node);
    }

    public void removeNodeDistribution(Node node) {
        nodeDistributions.remove(node);
    }

    @Override
    public int compareTo(@NotNull Depositor depositor) {
        return namespace.compareTo(depositor.namespace);
    }

}
