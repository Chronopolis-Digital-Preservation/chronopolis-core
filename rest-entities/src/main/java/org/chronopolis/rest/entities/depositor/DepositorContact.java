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
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DepositorContact extends PersistableEntity implements Comparable<DepositorContact> {

    private String contactName;
    private String contactPhone;
    private String contactEmail;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
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
