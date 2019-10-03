package org.chronopolis.rest.entities;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.chronopolis.rest.entities.storage.Fixity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

/**
 * A generalized class for files which may be stored in Chronopolis. At the moment there are two
 * separate "types" - BAG and TOKEN_STORE. Each type is distinct in the function it provides, which
 * means that they are contained in two separate classes: {@link BagFile} and {@link TokenStore}.
 * The {@link DataFile} contains the common columns, which in reality is just about everything...
 *
 * @author shake
 */
@Data
@Entity
@Inheritance
@Table(name = "file")
@DiscriminatorColumn(name = "dtype")
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public abstract class DataFile extends UpdatableEntity {

    /**
     * The size in bytes of the {@link DataFile}
     */
    private Long size;

    /**
     * A logical identifier used for the {@link DataFile}. This should be a relative path to the
     * root of the {@link Bag}.
     */
    @EqualsAndHashCode.Include
    private String filename;

    /**
     * The {@link Bag} which this {@link DataFile} belongs to.
     */
    @ManyToOne(fetch = LAZY)
    @EqualsAndHashCode.Include
    private Bag bag;

    /**
     * The discriminator column used to identify if this is a file for a BAG or TOKEN_STORE. As this
     * is inserted once on persistence, we do not allow it to be set from a non-jpa source.
     */
    @Column(insertable = false, updatable = false)
    private String dtype;

    /**
     * The {@link Fixity} values associated with this {@link DataFile}
     */
    @OneToMany(mappedBy = "file", cascade = ALL, fetch = EAGER, orphanRemoval = true)
    private Set<Fixity> fixities = new HashSet<>();

    public void addFixity(Fixity fixity) {
        if (fixity != null && (fixity.getFile() == null || fixity.getFile().equals(this))) {
            fixity.setFile(this);
            fixities.add(fixity);
        }
    }

    @PrePersist
    protected void checkLeading() {
        if (!filename.startsWith("/")) {
            this.filename = "/" + filename;
        }
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("bagId", getBag().getId())
                .add("type", getDtype())
                .add("filename", getFilename())
                .toString();
    }

}
