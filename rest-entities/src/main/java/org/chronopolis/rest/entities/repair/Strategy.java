package org.chronopolis.rest.entities.repair;

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
public abstract class Strategy extends PersistableEntity {
    abstract public FulfillmentStrategy model();
}
