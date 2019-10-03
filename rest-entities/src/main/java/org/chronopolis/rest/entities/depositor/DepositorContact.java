package org.chronopolis.rest.entities.depositor;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.PersistableEntity;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Contact information for a {@link Depositor}
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DepositorContact extends PersistableEntity implements Comparable<DepositorContact> {

    /**
     * The name of the contact
     */
    private String contactName;

    /**
     * A phone number for the contact, previously validated (hopefully)
     */
    private String contactPhone;

    /**
     * An email address; unique on ({@link Depositor}, {@link DepositorContact})
     */
    private String contactEmail;

    /**
     * The {@link Depositor}
     */
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depositor_id")
    private Depositor depositor;

    public DepositorContact(String contactName, String contactPhone, String contactEmail) {
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
    }

    @Override
    public int compareTo(@NotNull DepositorContact depositorContact) {
        return ComparisonChain.start()
                .compare(contactName, depositorContact.contactName)
                .compare(contactPhone, depositorContact.contactPhone)
                .compare(contactEmail, depositorContact.contactEmail)
                .result();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("depositorId", getDepositor().getId())
                .toString();
    }
}
