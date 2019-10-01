package org.chronopolis.rest.entities.storage;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.entities.PersistableEntity;
import org.chronopolis.rest.entities.converters.ZonedDateTimeConverter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.ZonedDateTime;

/**
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Fixity extends PersistableEntity {

    @EqualsAndHashCode.Include
    private String value;

    private String algorithm;

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
