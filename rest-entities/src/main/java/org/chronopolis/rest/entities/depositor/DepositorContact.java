package org.chronopolis.rest.entities.depositor;

import com.google.common.collect.ComparisonChain;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
public class DepositorContact implements Comparable<DepositorContact> {

    @NonNull private String contactName;
    @NonNull private String contactPhone;
    @Id
    @NonNull private String contactEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depositor_id")
    private Depositor depositor;

    @Override
    public int compareTo(@NotNull DepositorContact depositorContact) {
        return ComparisonChain.start()
                .compare(contactName, depositorContact.contactName)
                .compare(contactPhone, depositorContact.contactPhone)
                .compare(contactEmail, depositorContact.contactEmail)
                .result();
    }
}
