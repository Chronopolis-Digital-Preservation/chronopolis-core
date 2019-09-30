package org.chronopolis.rest.entities.repair;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.chronopolis.rest.entities.PersistableEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
public class RepairFile extends PersistableEntity {

    @ManyToOne private Repair repair;
    @ToString.Include private String path;

    public RepairFile(Repair repair, String path) {
        this.repair = repair;
        this.path = path;
    }

}
