package org.chronopolis.rest.entities.repair;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.models.AceStrategy;
import org.chronopolis.rest.models.FulfillmentStrategy;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Information for fulfilling a {@link Repair} through the ACE Audit Manager
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@DiscriminatorValue("ACE")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Ace extends Strategy {

    /**
     * The URL of the ACE-AM to connect to
     */
    private String url;

    /**
     * The api key to connect with
     */
    private String apiKey;

    @OneToOne(mappedBy = "strategy")
    private Repair repair;

    public Ace(String apiKey, String url) {
        this.url = url;
        this.apiKey = apiKey;
    }

    @Override
    public FulfillmentStrategy model() {
        return new AceStrategy(apiKey, url);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("repairId", getRepair().getId())
                .add("url", getUrl())
                .add("apiKey", getApiKey())
                .toString();
    }

}
