package org.chronopolis.rest.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.converters.ZonedDateTimeConverter;

import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;
import java.time.ZonedDateTime;

/**
 *
 * @since mostlyforever
 * @author shake
 */
@Data
@NoArgsConstructor
@MappedSuperclass
public class UpdatableEntity extends PersistableEntity {

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

}
