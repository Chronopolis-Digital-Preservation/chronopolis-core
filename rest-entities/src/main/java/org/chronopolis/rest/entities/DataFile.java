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

    private Long size;

    @EqualsAndHashCode.Include
    private String filename;

    @ManyToOne(fetch = LAZY)
    @EqualsAndHashCode.Include
    private Bag bag;

    @Column(insertable = false, updatable = false)
    private String dtype;

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
