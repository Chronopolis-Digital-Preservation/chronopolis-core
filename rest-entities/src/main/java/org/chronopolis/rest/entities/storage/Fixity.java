package org.chronopolis.rest.entities.storage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.chronopolis.rest.entities.DataFile;
import org.chronopolis.rest.entities.PersistableEntity;
import org.chronopolis.rest.entities.converters.ZonedDateTimeConverter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.ZonedDateTime;

/**
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Fixity extends PersistableEntity {

    @ToString.Include private String value;
    @ToString.Include private String algorithm;

    @Convert(converter =  ZonedDateTimeConverter.class)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @ManyToOne
    private DataFile file;

    public Fixity(ZonedDateTime createdAt, DataFile file, String value, String algorithm) {
        this.createdAt = createdAt;
        this.file = file;
        this.value = value;
        this.algorithm = algorithm;
    }

}
