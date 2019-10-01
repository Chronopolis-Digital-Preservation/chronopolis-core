package org.chronopolis.rest.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author shake
 */
@Entity
@DiscriminatorValue("TOKEN_STORE")
public class TokenStore extends DataFile {

    public String toString() {
        return super.toString();
    }

}
