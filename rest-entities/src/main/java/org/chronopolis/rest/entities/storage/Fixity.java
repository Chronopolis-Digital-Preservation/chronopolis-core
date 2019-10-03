package org.chronopolis.rest.entities.storage;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.entities.PersistableEntity;
import org.chronopolis.rest.entities.converters.ZonedDateTimeConverter;
import org.chronopolis.rest.models.enums.FixityAlgorithm;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.ZonedDateTime;

/**
 * Tracking for values for Fixity checks. Used to validate digests.
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Fixity extends PersistableEntity {

    /**
     * The output digest of the algorithm, likely in hex
     */
    @EqualsAndHashCode.Include
    private String value;

    /**
     * The name of the algorithm used. Should map to {@link FixityAlgorithm}
     */
    private String algorithm;

    /**
     * The DateTime the {@link Fixity} was created
     */
    @Convert(converter =  ZonedDateTimeConverter.class)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @ManyToOne
    @EqualsAndHashCode.Include
    private DataFile file;

    public Fixity(ZonedDateTime createdAt, DataFile file, String value, String algorithm) {
        this.createdAt = createdAt;
        this.file = file;
        this.value = value;
        this.algorithm = algorithm;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("fileId", getFile().getId())
                .add("value", getValue())
                .add("algorithm", getAlgorithm())
                .toString();
    }

}
