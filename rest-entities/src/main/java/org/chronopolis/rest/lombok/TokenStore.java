package org.chronopolis.rest.lombok;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author shake
 */
@Entity
@DiscriminatorValue("TOKEN_STORE")
public class TokenStore extends DataFile {
}
