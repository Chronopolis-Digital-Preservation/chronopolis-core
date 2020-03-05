package org.chronopolis.ingest.models;

import org.chronopolis.rest.models.enums.BagStatus;

/**
 * Model for updating bags
 *
 * May be expanded as we allow more parts of a bag to be mutable
 *
 * Created by shake on 4/28/15.
 */
public class BagUpdate {

    private String location;
    private BagStatus status;

    public BagUpdate() {
    }

    public BagUpdate(String location, BagStatus status) {
        this.location = location;
        this.status = status;
    }

    public BagStatus getStatus() {
        return status;
    }

    public BagUpdate setStatus(BagStatus status) {
        this.status = status;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public BagUpdate setLocation(String location) {
        this.location = location;
        return this;
    }
}
