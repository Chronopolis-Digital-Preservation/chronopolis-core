package org.chronopolis.rest.entities;

import lombok.Data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;

/**
 * @author shake
 */
@Data
@Entity
@DiscriminatorValue("BAG")
public class BagFile extends DataFile {

    @OneToOne(mappedBy = "file", cascade = {MERGE, PERSIST}, orphanRemoval = true)
    private AceToken token;

}
