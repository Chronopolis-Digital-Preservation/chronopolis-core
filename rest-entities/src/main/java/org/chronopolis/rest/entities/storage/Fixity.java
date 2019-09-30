package org.chronopolis.rest.entities.storage;

import lombok.Data;
import lombok.NoArgsConstructor;
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
public class Fixity extends PersistableEntity {

    private String value;
    private String algorithm;

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
