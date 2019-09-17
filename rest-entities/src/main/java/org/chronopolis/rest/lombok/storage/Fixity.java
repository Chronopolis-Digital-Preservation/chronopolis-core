package org.chronopolis.rest.lombok.storage;

import lombok.Data;
import org.chronopolis.rest.lombok.DataFile;
import org.chronopolis.rest.lombok.PersistableEntity;
import org.chronopolis.rest.lombok.converters.ZonedDateTimeConverter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.ZonedDateTime;

/**
 * @author shake
 */
@Data
@Entity
public class Fixity extends PersistableEntity {

    private String value;
    private String algorithm;

    @Convert(converter =  ZonedDateTimeConverter.class)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @ManyToOne
    private DataFile file;

}
