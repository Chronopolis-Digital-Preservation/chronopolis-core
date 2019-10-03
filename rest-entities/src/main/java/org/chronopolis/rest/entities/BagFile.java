package org.chronopolis.rest.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;

/**
 * A file belonging to a {@link Bag}. Both payload and metadata files are included in this
 * representation.
 *
 * @author shake
 */
@Data
@Entity
@DiscriminatorValue("BAG")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BagFile extends DataFile {

    /**
     * The {@link AceToken} created for this {@link BagFile}
     */
    @OneToOne(mappedBy = "file", cascade = {MERGE, PERSIST}, orphanRemoval = true)
    private AceToken token;

    public String toString() {
        return super.toString();
    }

}
