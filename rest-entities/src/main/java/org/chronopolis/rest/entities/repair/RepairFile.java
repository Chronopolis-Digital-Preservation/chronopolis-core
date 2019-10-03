package org.chronopolis.rest.entities.repair;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.Node;
import org.chronopolis.rest.entities.PersistableEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * A {@link RepairFile} represents a file which is corrupt at a {@link Node} and needs to be healed.
 * It only contains a path, which serves as a logical address of the file.
 *
 * todo: Join on {@link org.chronopolis.rest.entities.BagFile}
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RepairFile extends PersistableEntity {

    @ManyToOne
    @ToString.Exclude
    private Repair repair;

    /**
     * The path of the file which is being repaired, relative to the {@link Bag} which it belongs to
     */
    @ToString.Include
    @EqualsAndHashCode.Include
    private String path;

    public RepairFile(Repair repair, String path) {
        this.repair = repair;
        this.path = path;
    }

}
