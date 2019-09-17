package org.chronopolis.rest.lombok.depositor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.chronopolis.rest.lombok.PersistableEntity;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
public class Depositor extends PersistableEntity implements Comparable<Depositor> {

    @NonNull
    @NaturalId
    @EqualsAndHashCode.Include
    private String namespace;

    @NonNull private String sourceOrganization;
    @NonNull private String organizationAddress;

    @OneToMany(mappedBy = "depositor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DepositorContact> contacts = new HashSet<>();

    @Override
    public int compareTo(@NotNull Depositor depositor) {
        return namespace.compareTo(depositor.namespace);
    }

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
}
