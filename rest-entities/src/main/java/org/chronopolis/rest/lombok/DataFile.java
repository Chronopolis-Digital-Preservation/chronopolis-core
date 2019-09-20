package org.chronopolis.rest.lombok;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.chronopolis.rest.lombok.storage.Fixity;

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
 * @author shake
 */
@Data
@Entity
@Inheritance
@Table(name = "file")
@DiscriminatorColumn(name = "dtype")
public abstract class DataFile extends UpdatableEntity {

    private Long size;
    private String filename;

    @ManyToOne(fetch = LAZY)
    private Bag bag;

    @Column(insertable = false, updatable = false)
    private String dtype;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "file", cascade = ALL, fetch = EAGER, orphanRemoval = true)
    private Set<Fixity> fixities = new HashSet<>();

    public void addFixity(Fixity fixity) {
        if (fixity != null && fixity.getFile() == null) {
            fixity.setFile(this);
            fixities.add(fixity);
        }
    }

    public void rmFixity(Fixity fixity) {
        if (fixity != null && fixity.getFile().equals(this)) {
            fixities.remove(fixity);
            fixity.setFile(null);
        }
    }

    @PrePersist
    protected void checkLeading() {
        if (!filename.startsWith("/")) {
            this.filename = "/" + filename;
        }
    }


}
