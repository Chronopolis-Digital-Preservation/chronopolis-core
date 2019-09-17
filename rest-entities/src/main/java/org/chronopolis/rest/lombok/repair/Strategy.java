package org.chronopolis.rest.lombok.repair;

import org.chronopolis.rest.lombok.PersistableEntity;
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
abstract class Strategy extends PersistableEntity {
    abstract public FulfillmentStrategy model();
}
