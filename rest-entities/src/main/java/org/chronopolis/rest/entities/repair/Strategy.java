package org.chronopolis.rest.entities.repair;

import lombok.EqualsAndHashCode;
import org.chronopolis.rest.entities.PersistableEntity;
import org.chronopolis.rest.models.FulfillmentStrategy;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;

/**
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
