package org.chronopolis.rest.entities.repair;

import lombok.EqualsAndHashCode;
import org.chronopolis.rest.entities.PersistableEntity;
import org.chronopolis.rest.models.FulfillmentStrategy;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;

/**
 * The {@link Strategy} class represents the strategy table which is used for fulfilling
 * {@link Repair}s. The underlying table uses single table inheritance and has a discriminator
 * column which currently can have two values: RSYNC or ACE.
 *
 * @author shake
 */
@Entity
@Inheritance
@Table(name = "strategy")
@DiscriminatorColumn(name = "TYPE")
@EqualsAndHashCode(callSuper = true)
public abstract class Strategy extends PersistableEntity {
    abstract public FulfillmentStrategy model();
}
