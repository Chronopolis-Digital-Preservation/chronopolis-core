package org.chronopolis.rest.lombok.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Simple converter for {@link ZonedDateTime} to {@link Timestamp}
 *
 * @since kindofrecent
 * @author shake
 */
@Converter
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            return Timestamp.valueOf(zonedDateTime.toLocalDateTime());
        }

        return Timestamp.valueOf(LocalDateTime.now());
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp timestamp) {
        if (timestamp != null) {
            return timestamp.toLocalDateTime().atZone(ZoneOffset.UTC);
        }

        return ZonedDateTime.now();
    }

}
