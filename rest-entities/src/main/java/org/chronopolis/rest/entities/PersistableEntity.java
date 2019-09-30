package org.chronopolis.rest.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Superclass for persistable entities with primary key identifier.
 *
 * If the identifier is not set (0), then we know a entity has yet to be persisted.
 *
 * @since foreverandahalfago
 * @author shake
 */
@Data
@NoArgsConstructor
@MappedSuperclass
public class PersistableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 0L;

}
