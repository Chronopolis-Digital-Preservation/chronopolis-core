package org.chronopolis.rest.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
@EqualsAndHashCode(callSuper = true)
public class BagFile extends DataFile {

    @OneToOne(mappedBy = "file", cascade = {MERGE, PERSIST}, orphanRemoval = true)
    private AceToken token;

}
