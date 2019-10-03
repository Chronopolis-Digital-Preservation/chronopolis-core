package org.chronopolis.rest.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A {@link TokenStore} which belongs to a {@link Bag} but is not contained within it
 *
 * @author shake
 */
@Entity
@DiscriminatorValue("TOKEN_STORE")
public class TokenStore extends DataFile {

    public String toString() {
        return super.toString();
    }

}
